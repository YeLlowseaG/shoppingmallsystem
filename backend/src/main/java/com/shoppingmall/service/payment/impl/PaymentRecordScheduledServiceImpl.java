package com.shoppingmall.service.payment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.service.payment.PaymentRecordScheduledService;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付记录定时任务服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecordScheduledServiceImpl implements PaymentRecordScheduledService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final SystemConfigService systemConfigService;

    /**
     * 获取支付记录自动取消时间（小时），从数据库配置读取，默认2小时
     * 每次执行定时任务时读取最新配置，确保配置修改后立即生效
     *
     * @return 自动取消时间（小时）
     */
    private Integer getPaymentRecordTimeoutHours() {
        String timeoutStr = systemConfigService.getConfigValue("payment.record-timeout-hours", "2");
        try {
            return Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            log.warn("支付记录自动取消时间配置格式错误，使用默认值2小时: {}", timeoutStr);
            return 2;
        }
    }

    /**
     * 自动取消超时的支付中支付记录
     * 每小时执行一次，检查超过指定时间仍处于支付中状态的支付记录
     */
    @Override
    @Scheduled(fixedRate = 3600000) // 每小时执行一次（3600000毫秒 = 1小时）
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutPaymentRecords() {
        try {
            // 每次执行时读取最新配置
            Integer timeoutHours = getPaymentRecordTimeoutHours();
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(timeoutHours);

            // 查找超时的支付中支付记录
            List<PaymentRecord> timeoutRecords = paymentRecordRepository.selectList(
                    new LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getPaymentStatus, PaymentStatus.PAYING) // 支付中状态
                            .le(PaymentRecord::getCreateTime, timeoutThreshold) // 创建时间早于超时阈值
            );

            if (timeoutRecords.isEmpty()) {
                return;
            }

            log.info("发现{}个超时的支付中支付记录，开始自动关闭", timeoutRecords.size());

            // 批量处理超时支付记录
            for (PaymentRecord record : timeoutRecords) {
                try {
                    // 更新支付记录状态为已关闭
                    record.setPaymentStatus(PaymentStatus.CLOSED);
                    paymentRecordRepository.updateById(record);

                    log.info("自动关闭超时支付记录成功: paymentNo={}, orderId={}, createTime={}", 
                            record.getPaymentNo(), record.getOrderId(), record.getCreateTime());
                } catch (Exception e) {
                    log.error("自动关闭支付记录失败: paymentNo={}", record.getPaymentNo(), e);
                    // 继续处理下一个记录，不中断整个任务
                }
            }

            log.info("自动关闭超时支付记录任务完成，共处理{}个记录", timeoutRecords.size());
        } catch (Exception e) {
            log.error("自动关闭超时支付记录任务执行异常", e);
            // 定时任务异常不影响系统运行
        }
    }
}

