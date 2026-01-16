package com.shoppingmall.service.payment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.config.WeChatPayConfig;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.util.AlipayUtil;
import com.shoppingmall.payment.util.WeChatPayUtil;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.entity.PaymentApiLog;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanOrderService;
import com.shoppingmall.service.payment.PaymentLogService;
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
 * 用于自动查询支付中状态的支付记录，如果支付宝或微信已支付则自动补单
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
    private final PaymentLogService paymentLogService;

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
     * 每5分钟执行一次，查询支付中状态的支付记录，如果支付宝或微信已支付则自动补单
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
                    String paymentMethod = paymentRecord.getPaymentMethod();
                    
                    // 只处理支付宝和微信支付
                    if (!PaymentMethod.ALIPAY.equals(paymentMethod) && !PaymentMethod.WECHAT.equals(paymentMethod)) {
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

                    String orderNo = order.getOrderNo();
                    Map<String, String> orderStatus = null;
                    String tradeStatus = null;
                    String tradeNo = null;
                    long startTime = System.currentTimeMillis();
                    String apiUrl = null;

                    // 根据支付方式查询订单状态
                    if (PaymentMethod.ALIPAY.equals(paymentMethod)) {
                        // 获取支付宝配置
                        AlipayConfig alipayConfig = paymentConfigService.getAlipayConfig();
                        if (alipayConfig == null || !alipayConfig.getEnabled()) {
                            log.warn("支付宝配置不存在或未启用，跳过补单: paymentNo={}", paymentRecord.getPaymentNo());
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

                        // 查询支付宝订单状态
                        orderStatus = AlipayUtil.queryOrder(envConfig, orderNo);
                        tradeStatus = orderStatus.get("trade_status");
                        tradeNo = orderStatus.get("trade_no");
                        apiUrl = envConfig.getGateway() != null ? envConfig.getGateway() : 
                            ("sandbox".equals(alipayConfig.getEnv()) ? "https://openapi.alipaydev.com/gateway.do" : "https://openapi.alipay.com/gateway.do");

                    } else if (PaymentMethod.WECHAT.equals(paymentMethod)) {
                        // 获取微信支付配置
                        WeChatPayConfig wechatConfig = paymentConfigService.getWeChatPayConfig();
                        if (wechatConfig == null || !wechatConfig.getEnabled()) {
                            log.warn("微信支付配置不存在或未启用，跳过补单: paymentNo={}", paymentRecord.getPaymentNo());
                            skipCount++;
                            continue;
                        }

                        WeChatPayConfig.WeChatPayEnvConfig envConfig = "production".equals(wechatConfig.getEnv())
                                ? wechatConfig.getProduction()
                                : wechatConfig.getSandbox();

                        if (envConfig == null) {
                            log.warn("微信支付环境配置不存在，跳过补单: paymentNo={}", paymentRecord.getPaymentNo());
                            skipCount++;
                            continue;
                        }

                        // 查询微信支付订单状态
                        orderStatus = WeChatPayUtil.queryOrder(envConfig, orderNo);
                        tradeStatus = orderStatus.get("trade_state"); // 微信使用 trade_state
                        tradeNo = orderStatus.get("transaction_id"); // 微信使用 transaction_id
                        apiUrl = "sandbox".equals(wechatConfig.getEnv()) 
                            ? "https://api.mch.weixin.qq.com/sandboxnew/pay/orderquery" 
                            : "https://api.mch.weixin.qq.com/pay/orderquery";
                    }

                    long executionTime = System.currentTimeMillis() - startTime;

                    // 记录查询订单日志
                    try {
                        PaymentApiLog apiLog = new PaymentApiLog();
                        apiLog.setPaymentMethod(paymentMethod);
                        apiLog.setApiType("QUERY_ORDER");
                        apiLog.setBusinessType("ORDER");
                        apiLog.setOrderNo(orderNo);
                        apiLog.setPaymentNo(paymentRecord.getPaymentNo());
                        apiLog.setExternalTradeNo(tradeNo);
                        apiLog.setApiUrl(apiUrl);
                        apiLog.setRequestMethod("POST");
                        apiLog.setExecutionTime((int) executionTime);
                        
                        // 判断查询结果
                        boolean querySuccess = false;
                        if (PaymentMethod.ALIPAY.equals(paymentMethod)) {
                            if ("UNKNOWN".equals(tradeStatus)) {
                                apiLog.setApiStatus(0); // 失败
                                apiLog.setErrorMessage("订单不存在或查询失败");
                            } else {
                                apiLog.setApiStatus(1); // 成功
                                querySuccess = true;
                            }
                        } else {
                            // 微信支付状态判断
                            String returnCode = orderStatus.get("return_code");
                            String resultCode = orderStatus.get("result_code");
                            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                                apiLog.setApiStatus(1); // 成功
                                querySuccess = true;
                            } else {
                                apiLog.setApiStatus(0); // 失败
                                apiLog.setErrorMessage(orderStatus.get("err_code_des") != null ? orderStatus.get("err_code_des") : "查询失败");
                            }
                        }
                        
                        try {
                            Map<String, Object> requestData = new HashMap<>();
                            requestData.put("orderNo", orderNo);
                            apiLog.setRequestData(objectMapper.writeValueAsString(requestData));
                            apiLog.setResponseData(objectMapper.writeValueAsString(orderStatus));
                        } catch (Exception e) {
                            log.warn("序列化查询数据失败", e);
                        }
                        paymentLogService.savePaymentLog(apiLog);
                    } catch (Exception e) {
                        log.warn("记录查询订单日志失败", e);
                    }

                    // 判断订单是否已支付
                    boolean isPaid = false;
                    if (PaymentMethod.ALIPAY.equals(paymentMethod)) {
                        if ("UNKNOWN".equals(tradeStatus)) {
                            // 无法查询到订单状态，可能是订单不存在或查询失败
                            log.debug("无法查询到订单状态，跳过补单: orderNo={}, paymentNo={}", orderNo, paymentRecord.getPaymentNo());
                            continue;
                        }
                        isPaid = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
                    } else {
                        // 微信支付状态判断
                        String returnCode = orderStatus.get("return_code");
                        String resultCode = orderStatus.get("result_code");
                        if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                            log.debug("微信支付查询失败，跳过补单: orderNo={}, paymentNo={}", orderNo, paymentRecord.getPaymentNo());
                            continue;
                        }
                        // 微信支付：SUCCESS 表示支付成功
                        isPaid = "SUCCESS".equals(tradeStatus);
                    }

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
                        if (PaymentMethod.ALIPAY.equals(paymentMethod)) {
                            notifyData.put("trade_status", tradeStatus);
                            notifyData.put("trade_no", tradeNo);
                        } else {
                            notifyData.put("trade_state", tradeStatus);
                            notifyData.put("transaction_id", tradeNo);
                        }

                        try {
                            paymentRecord.setCallbackData(objectMapper.writeValueAsString(notifyData));
                        } catch (Exception e) {
                            log.warn("保存回调数据失败", e);
                        }

                        paymentRecordRepository.updateById(paymentRecord);

                        // 更新订单状态
                        order.setPaymentStatus(PaymentStatus.PAID);
                        order.setOrderStatus(OrderStatus.PAID_UNSHIPPED);
                        order.setPaymentMethod(paymentMethod); // 更新支付方式
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

