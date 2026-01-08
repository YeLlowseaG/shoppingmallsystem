package com.shoppingmall.service.payment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.constant.DepositStatus;
import com.shoppingmall.common.constant.DepositType;
import com.shoppingmall.entity.PaymentApiLog;
import com.shoppingmall.entity.PreDeposit;
import com.shoppingmall.entity.PreDepositDetail;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.config.WeChatPayConfig;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.util.AlipayUtil;
import com.shoppingmall.payment.util.WeChatPayUtil;
import com.shoppingmall.repository.deposit.PreDepositDetailRepository;
import com.shoppingmall.repository.deposit.PreDepositRepository;
import com.shoppingmall.service.payment.PaymentLogService;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预存款充值支付状态同步定时任务服务实现类
 * 用于自动查询支付中状态的预存款充值记录，如果已支付则自动更新状态
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Slf4j
@Service("depositPaymentSyncScheduledService")
@RequiredArgsConstructor
public class DepositPaymentSyncScheduledServiceImpl {

    private final PreDepositDetailRepository preDepositDetailRepository;
    private final PreDepositRepository preDepositRepository;
    private final PaymentConfigService paymentConfigService;
    private final ObjectMapper objectMapper;
    private final SystemConfigService systemConfigService;
    private final PaymentLogService paymentLogService;

    /**
     * 获取支付结果查询时间窗口（分钟），只查询最近N分钟内的充值记录，默认30分钟
     */
    private Integer getPaymentSyncTimeWindowMinutes() {
        String windowStr = systemConfigService.getConfigValue("deposit.payment-sync-time-window-minutes", "30");
        try {
            return Integer.parseInt(windowStr);
        } catch (NumberFormatException e) {
            log.warn("预存款充值支付结果查询时间窗口配置格式错误，使用默认值30分钟: {}", windowStr);
            return 30;
        }
    }

    /**
     * 自动查询预存款充值支付结果并同步状态
     * 每5分钟执行一次，查询支付中状态的预存款充值记录，如果已支付则自动更新状态
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次（300000毫秒 = 5分钟）
    @Transactional(rollbackFor = Exception.class)
    public void syncDepositPaymentStatus() {
        try {
            // 每次执行时读取最新配置
            Integer timeWindowMinutes = getPaymentSyncTimeWindowMinutes();
            LocalDateTime timeWindowStart = LocalDateTime.now().minusMinutes(timeWindowMinutes);

            // 查找支付中状态的预存款充值记录（只查询最近N分钟内的记录）
            List<PreDepositDetail> payingRecords = preDepositDetailRepository.selectList(
                    new LambdaQueryWrapper<PreDepositDetail>()
                            .eq(PreDepositDetail::getType, DepositType.RECHARGE) // 充值类型
                            .eq(PreDepositDetail::getStatus, DepositStatus.PAYING) // 支付中状态
                            .in(PreDepositDetail::getPaymentMethod, "alipay", "wechat") // 只处理支付宝和微信
                            .ge(PreDepositDetail::getCreateTime, timeWindowStart) // 创建时间在时间窗口内
                            .orderByAsc(PreDepositDetail::getCreateTime) // 按创建时间升序
                            .last("LIMIT 50") // 每次最多处理50条
            );

            if (payingRecords.isEmpty()) {
                return;
            }

            log.info("发现{}个支付中状态的预存款充值记录，开始查询支付结果并同步状态", payingRecords.size());

            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;

            // 批量处理充值记录
            for (PreDepositDetail detail : payingRecords) {
                try {
                    String paymentMethod = detail.getPaymentMethod();
                    String internalOrderNo = detail.getInternalOrderNo();

                    if (internalOrderNo == null || internalOrderNo.isEmpty()) {
                        log.warn("充值记录内部订单号为空，跳过: detailId={}", detail.getId());
                        skipCount++;
                        continue;
                    }

                    Map<String, String> orderStatus = null;
                    String tradeStatus = null;
                    String tradeNo = null;
                    long startTime = System.currentTimeMillis();
                    String apiUrl = null;

                    // 根据支付方式查询订单状态
                    if ("alipay".equals(paymentMethod)) {
                        // 支付宝支付查询
                        AlipayConfig alipayConfig = paymentConfigService.getAlipayConfig();
                        if (alipayConfig == null || !alipayConfig.getEnabled()) {
                            log.warn("支付宝配置不存在或未启用，跳过: detailId={}", detail.getId());
                            skipCount++;
                            continue;
                        }

                        AlipayConfig.AlipayEnvConfig envConfig = "production".equals(alipayConfig.getEnv())
                                ? alipayConfig.getProduction()
                                : alipayConfig.getSandbox();

                        if (envConfig == null) {
                            log.warn("支付宝环境配置不存在，跳过: detailId={}", detail.getId());
                            skipCount++;
                            continue;
                        }

                        orderStatus = AlipayUtil.queryOrder(envConfig, internalOrderNo);
                        tradeStatus = orderStatus.get("trade_status");
                        tradeNo = orderStatus.get("trade_no");
                        apiUrl = envConfig.getGateway() != null ? envConfig.getGateway() : 
                            ("sandbox".equals(alipayConfig.getEnv()) ? "https://openapi.alipaydev.com/gateway.do" : "https://openapi.alipay.com/gateway.do");

                    } else if ("wechat".equals(paymentMethod)) {
                        // 微信支付查询
                        WeChatPayConfig wechatConfig = paymentConfigService.getWeChatPayConfig();
                        if (wechatConfig == null || !wechatConfig.getEnabled()) {
                            log.warn("微信支付配置不存在或未启用，跳过: detailId={}", detail.getId());
                            skipCount++;
                            continue;
                        }

                        WeChatPayConfig.WeChatPayEnvConfig envConfig = "production".equals(wechatConfig.getEnv())
                                ? wechatConfig.getProduction()
                                : wechatConfig.getSandbox();

                        if (envConfig == null) {
                            log.warn("微信支付环境配置不存在，跳过: detailId={}", detail.getId());
                            skipCount++;
                            continue;
                        }

                        orderStatus = WeChatPayUtil.queryOrder(envConfig, internalOrderNo);
                        tradeStatus = orderStatus.get("trade_state"); // 微信使用 trade_state
                        tradeNo = orderStatus.get("transaction_id"); // 微信使用 transaction_id
                        apiUrl = "sandbox".equals(wechatConfig.getEnv()) 
                            ? "https://api.mch.weixin.qq.com/sandboxnew/pay/orderquery" 
                            : "https://api.mch.weixin.qq.com/pay/orderquery";
                    } else {
                        skipCount++;
                        continue;
                    }

                    long executionTime = System.currentTimeMillis() - startTime;

                    // 记录查询订单日志
                    try {
                        PaymentApiLog apiLog = new PaymentApiLog();
                        apiLog.setPaymentMethod("alipay".equals(paymentMethod) ? "ALIPAY" : "WECHAT");
                        apiLog.setApiType("QUERY_ORDER");
                        apiLog.setBusinessType("DEPOSIT");
                        apiLog.setOrderNo(internalOrderNo);
                        apiLog.setPaymentNo(internalOrderNo);
                        apiLog.setExternalTradeNo(tradeNo);
                        apiLog.setApiUrl(apiUrl);
                        apiLog.setRequestMethod("POST");
                        apiLog.setExecutionTime((int) executionTime);
                        
                        // 判断查询结果
                        boolean querySuccess = false;
                        if ("alipay".equals(paymentMethod)) {
                            if ("UNKNOWN".equals(tradeStatus)) {
                                apiLog.setApiStatus(0); // 失败
                                apiLog.setErrorMessage("订单不存在或查询失败");
                            } else {
                                apiLog.setApiStatus(1); // 成功
                                querySuccess = true;
                            }
                        } else {
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
                            requestData.put("internalOrderNo", internalOrderNo);
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
                    if ("alipay".equals(paymentMethod)) {
                        if ("UNKNOWN".equals(tradeStatus)) {
                            log.debug("无法查询到订单状态，跳过同步: internalOrderNo={}, detailId={}", internalOrderNo, detail.getId());
                            continue;
                        }
                        isPaid = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
                    } else {
                        String returnCode = orderStatus.get("return_code");
                        String resultCode = orderStatus.get("result_code");
                        if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                            log.debug("微信支付查询失败，跳过同步: internalOrderNo={}, detailId={}", internalOrderNo, detail.getId());
                            continue;
                        }
                        // 微信支付：SUCCESS 表示支付成功
                        isPaid = "SUCCESS".equals(tradeStatus);
                    }

                    if (isPaid) {
                        // 支付成功，更新充值记录状态和余额
                        detail.setStatus(DepositStatus.APPROVED); // 已通过
                        detail.setAuditTime(LocalDateTime.now());
                        detail.setExternalTradeNo(tradeNo);
                        detail.setRemark("预存款充值:外部交易号(" + tradeNo + ")");

                        // 保存回调数据
                        try {
                            Map<String, Object> notifyData = new HashMap<>();
                            notifyData.put("out_trade_no", internalOrderNo);
                            if ("alipay".equals(paymentMethod)) {
                                notifyData.put("trade_status", tradeStatus);
                                notifyData.put("trade_no", tradeNo);
                            } else {
                                notifyData.put("trade_state", tradeStatus);
                                notifyData.put("transaction_id", tradeNo);
                            }
                            detail.setCallbackData(objectMapper.writeValueAsString(notifyData));
                        } catch (Exception e) {
                            log.warn("保存回调数据失败", e);
                        }

                        // 更新预存款余额（使用悲观锁，防止并发充值导致余额不一致）
                        PreDeposit preDeposit = preDepositRepository.selectOne(
                                new LambdaQueryWrapper<PreDeposit>()
                                        .eq(PreDeposit::getUserId, detail.getUserId())
                                        .last("FOR UPDATE")
                        );

                        if (preDeposit != null) {
                            BigDecimal currentBalance = preDeposit.getBalance() != null ? preDeposit.getBalance() : BigDecimal.ZERO;
                            BigDecimal currentAvailableBalance = preDeposit.getAvailableBalance() != null ? preDeposit.getAvailableBalance() : BigDecimal.ZERO;
                            BigDecimal newBalance = currentBalance.add(detail.getDepositAmount());
                            BigDecimal newAvailableBalance = currentAvailableBalance.add(detail.getDepositAmount());

                            preDeposit.setBalance(newBalance);
                            preDeposit.setAvailableBalance(newAvailableBalance);
                            preDepositRepository.updateById(preDeposit);

                            // 更新明细中的余额
                            detail.setCurrentBalance(newBalance);
                            detail.setAvailableBalance(newAvailableBalance);
                        }

                        preDepositDetailRepository.updateById(detail);

                        successCount++;
                        log.info("预存款充值支付状态同步成功: internalOrderNo={}, detailId={}, tradeNo={}, tradeStatus={}", 
                                internalOrderNo, detail.getId(), tradeNo, tradeStatus);
                    } else {
                        // 订单未支付，记录日志
                        log.debug("预存款充值未支付，跳过同步: internalOrderNo={}, detailId={}, tradeStatus={}", 
                                internalOrderNo, detail.getId(), tradeStatus);
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("查询预存款充值支付结果并同步状态失败: detailId={}, internalOrderNo={}", 
                            detail.getId(), detail.getInternalOrderNo(), e);
                    // 继续处理下一个记录，不中断整个任务
                }
            }

            log.info("预存款充值支付状态同步任务完成，成功：{}，失败：{}，跳过：{}，总计：{}", 
                    successCount, failCount, skipCount, payingRecords.size());

        } catch (Exception e) {
            log.error("预存款充值支付状态同步任务执行异常", e);
            // 定时任务异常不影响系统运行
        }
    }
}

















