package com.shoppingmall.service.payment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shoppingmall.entity.PaymentApiLog;
import com.shoppingmall.mapper.PaymentApiLogMapper;
import com.shoppingmall.service.payment.PaymentLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 支付接口日志服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLogServiceImpl implements PaymentLogService {

    private final PaymentApiLogMapper paymentApiLogMapper;

    /**
     * 保存支付接口日志（异步执行，避免影响支付性能）
     *
     * @param paymentLog 支付接口日志实体
     */
    @Override
    @Async
    public void savePaymentLog(PaymentApiLog paymentLog) {
        try {
            if (paymentLog == null) {
                return;
            }
            paymentApiLogMapper.insert(paymentLog);
            log.debug("支付接口日志保存成功: paymentMethod={}, apiType={}, orderNo={}", 
                    paymentLog.getPaymentMethod(), paymentLog.getApiType(), paymentLog.getOrderNo());
        } catch (Exception e) {
            // 日志记录失败不应该影响支付流程，只记录错误日志
            log.error("保存支付接口日志失败: paymentMethod={}, apiType={}, orderNo={}", 
                    paymentLog != null ? paymentLog.getPaymentMethod() : null,
                    paymentLog != null ? paymentLog.getApiType() : null,
                    paymentLog != null ? paymentLog.getOrderNo() : null, e);
        }
    }

    /**
     * 更新支付接口日志状态（异步执行，避免影响支付性能）
     *
     * @param orderNo 订单号
     * @param apiType 接口类型
     * @param paymentMethod 支付方式
     * @param apiStatus 接口调用状态（0-失败，1-成功，2-处理中）
     * @param externalTradeNo 外部交易号（可选）
     * @param responseData 响应数据（可选）
     */
    @Override
    @Async
    public void updatePaymentLogStatus(String orderNo, String apiType, String paymentMethod, Integer apiStatus, String externalTradeNo, String responseData) {
        try {
            if (orderNo == null || apiType == null || paymentMethod == null || apiStatus == null) {
                return;
            }

            // 查询最新的日志记录
            LambdaQueryWrapper<PaymentApiLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PaymentApiLog::getOrderNo, orderNo);
            queryWrapper.eq(PaymentApiLog::getApiType, apiType);
            queryWrapper.eq(PaymentApiLog::getPaymentMethod, paymentMethod.toUpperCase());
            queryWrapper.orderByDesc(PaymentApiLog::getCreateTime);
            queryWrapper.last("LIMIT 1");

            PaymentApiLog existingLog = paymentApiLogMapper.selectOne(queryWrapper);
            if (existingLog != null) {
                // 更新状态
                LambdaUpdateWrapper<PaymentApiLog> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(PaymentApiLog::getId, existingLog.getId());
                updateWrapper.set(PaymentApiLog::getApiStatus, apiStatus);
                if (externalTradeNo != null && !externalTradeNo.isEmpty()) {
                    updateWrapper.set(PaymentApiLog::getExternalTradeNo, externalTradeNo);
                }
                if (responseData != null && !responseData.isEmpty()) {
                    updateWrapper.set(PaymentApiLog::getResponseData, responseData);
                }
                paymentApiLogMapper.update(null, updateWrapper);
                log.debug("支付接口日志状态更新成功: orderNo={}, apiType={}, apiStatus={}", orderNo, apiType, apiStatus);
            } else {
                log.warn("未找到需要更新的支付接口日志: orderNo={}, apiType={}, paymentMethod={}", orderNo, apiType, paymentMethod);
            }
        } catch (Exception e) {
            // 日志记录失败不应该影响支付流程，只记录错误日志
            log.error("更新支付接口日志状态失败: orderNo={}, apiType={}, paymentMethod={}", orderNo, apiType, paymentMethod, e);
        }
    }
}

