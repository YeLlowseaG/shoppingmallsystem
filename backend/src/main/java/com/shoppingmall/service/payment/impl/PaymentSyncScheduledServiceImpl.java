package com.shoppingmall.service.payment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.util.AlipayUtil;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanOrderService;
import com.shoppingmall.service.system.SystemConfigService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付结果查询补单定时任务服务实现类
 * 用于自动查询支付中状态的支付记录，如果支付宝已支付则自动补单
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
@Slf4j
@Service("paymentSyncScheduledService")
@RequiredArgsConstructor
public class PaymentSyncScheduledServiceImpl {

    private final PaymentRecordRepository paymentRecordRepository;
    private final OrderRepository orderRepository;
    private final PaymentConfigService paymentConfigService;
    private final ObjectMapper objectMapper;
    private final JushuitanOrderService jushuitanOrderService;
    private final JushuitanConfigService jushuitanConfigService;
    private final SystemConfigService systemConfigService;

    /**
     * 获取支付结果查询时间窗口（分钟），只查询最近N分钟内的支付记录，默认30分钟
     */
    private Integer getPaymentSyncTimeWindowMinutes() {
        String windowStr = systemConfigService.getConfigValue("payment.sync-time-window-minutes", "30");
        try {
            return Integer.parseInt(windowStr);
        } catch (NumberFormatException e) {
            log.warn("支付结果查询时间窗口配置格式错误，使用默认值30分钟: {}", windowStr);
            return 30;
        }
    }

    /**
     * 自动查询支付结果并补单
     * 每5分钟执行一次，查询支付中状态的支付记录，如果支付宝已支付则自动补单
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次（300000毫秒 = 5分钟）
    @Transactional(rollbackFor = Exception.class)
    public void syncPaymentStatus() {
        try {
            // 每次执行时读取最新配置
            Integer timeWindowMinutes = getPaymentSyncTimeWindowMinutes();
            LocalDateTime timeWindowStart = LocalDateTime.now().minusMinutes(timeWindowMinutes);

            // 查找支付中状态的支付记录（只查询最近N分钟内的记录）
            List<PaymentRecord> payingRecords = paymentRecordRepository.selectList(
                    new LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getPaymentStatus, PaymentStatus.PAYING) // 支付中状态
                            .ge(PaymentRecord::getCreateTime, timeWindowStart) // 创建时间在时间窗口内
                            .orderByAsc(PaymentRecord::getCreateTime) // 按创建时间升序，优先处理较早的记录
                            .last("LIMIT 50") // 每次最多处理50条，避免一次性处理太多
            );

            if (payingRecords.isEmpty()) {
                return;
            }

            log.info("发现{}个支付中状态的支付记录，开始查询支付结果并补单", payingRecords.size());

            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;

            // 批量处理支付记录
            for (PaymentRecord paymentRecord : payingRecords) {
                try {
                    // 只处理支付宝支付
                    if (!PaymentMethod.ALIPAY.equals(paymentRecord.getPaymentMethod())) {
                        skipCount++;
                        continue;
                    }

                    // 查询订单
                    Order order = orderRepository.selectById(paymentRecord.getOrderId());
                    if (order == null) {
                        log.warn("订单不存在，跳过补单: paymentNo={}, orderId={}", 
                                paymentRecord.getPaymentNo(), paymentRecord.getOrderId());
                        skipCount++;
                        continue;
                    }

                    // 如果订单已经支付，更新支付记录状态并跳过
                    if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
                        // 更新支付记录状态
                        paymentRecord.setPaymentStatus(PaymentStatus.PAID);
                        paymentRecordRepository.updateById(paymentRecord);
                        skipCount++;
                        continue;
                    }

                    // 获取支付宝配置
                    AlipayConfig alipayConfig = paymentConfigService.getAlipayConfig();
                    if (alipayConfig == null) {
                        log.warn("支付宝配置不存在，跳过补单: paymentNo={}", paymentRecord.getPaymentNo());
                        skipCount++;
                        continue;
                    }

                    AlipayConfig.AlipayEnvConfig envConfig = "production".equals(alipayConfig.getEnv())
                            ? alipayConfig.getProduction()
                            : alipayConfig.getSandbox();

                    if (envConfig == null) {
                        log.warn("支付宝环境配置不存在，跳过补单: paymentNo={}", paymentRecord.getPaymentNo());
                        skipCount++;
                        continue;
                    }

                    // 查询支付宝订单状态（使用订单号）
                    String orderNo = order.getOrderNo();
                    Map<String, String> orderStatus = AlipayUtil.queryOrder(envConfig, orderNo);
                    String tradeStatus = orderStatus.get("trade_status");
                    String tradeNo = orderStatus.get("trade_no");

                    if ("UNKNOWN".equals(tradeStatus)) {
                        // 无法查询到订单状态，可能是订单不存在或查询失败
                        log.debug("无法查询到订单状态，跳过补单: orderNo={}, paymentNo={}", orderNo, paymentRecord.getPaymentNo());
                        continue;
                    }

                    // 判断订单是否已支付
                    boolean isPaid = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);

                    if (isPaid) {
                        // 补单：更新支付记录和订单状态
                        paymentRecord.setPaymentStatus(PaymentStatus.PAID);
                        paymentRecord.setPaymentTime(LocalDateTime.now());
                        if (tradeNo != null && !tradeNo.isEmpty()) {
                            paymentRecord.setExternalTradeNo(tradeNo);
                        }

                        // 构建回调数据（模拟回调数据）
                        Map<String, Object> notifyData = new HashMap<>();
                        notifyData.put("out_trade_no", orderNo);
                        notifyData.put("trade_status", tradeStatus);
                        notifyData.put("trade_no", tradeNo);

                        try {
                            paymentRecord.setCallbackData(objectMapper.writeValueAsString(notifyData));
                        } catch (Exception e) {
                            log.warn("保存回调数据失败", e);
                        }

                        paymentRecordRepository.updateById(paymentRecord);

                        // 更新订单状态
                        order.setPaymentStatus(PaymentStatus.PAID);
                        order.setOrderStatus(OrderStatus.PAID_UNSHIPPED);
                        order.setPayTime(LocalDateTime.now());
                        orderRepository.updateById(order);

                        // 自动推送订单到聚水潭ERP
                        try {
                            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
                            if (config != null && config.getAutoPushOrder() == 1) {
                                log.info("自动推送订单到聚水潭ERP: orderId={}, orderNo={}", order.getId(), orderNo);
                                jushuitanOrderService.pushOrder(order.getId());
                            }
                        } catch (Exception e) {
                            log.error("自动推送订单到ERP失败: orderId={}, orderNo={}", order.getId(), orderNo, e);
                        }

                        successCount++;
                        log.info("自动补单成功: orderNo={}, paymentNo={}, tradeNo={}, tradeStatus={}", 
                                orderNo, paymentRecord.getPaymentNo(), tradeNo, tradeStatus);
                    } else {
                        // 订单未支付，记录日志
                        log.debug("订单未支付，跳过补单: orderNo={}, paymentNo={}, tradeStatus={}", 
                                orderNo, paymentRecord.getPaymentNo(), tradeStatus);
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("查询支付结果并补单失败: paymentNo={}, orderId={}", 
                            paymentRecord.getPaymentNo(), paymentRecord.getOrderId(), e);
                    // 继续处理下一个记录，不中断整个任务
                }
            }

            log.info("支付结果查询补单任务完成，成功：{}，失败：{}，跳过：{}，总计：{}", 
                    successCount, failCount, skipCount, payingRecords.size());

        } catch (Exception e) {
            log.error("支付结果查询补单任务执行异常", e);
            // 定时任务异常不影响系统运行
        }
    }
}

