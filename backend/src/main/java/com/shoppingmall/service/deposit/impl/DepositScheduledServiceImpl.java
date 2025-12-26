package com.shoppingmall.service.deposit.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.constant.DepositStatus;
import com.shoppingmall.entity.PreDepositDetail;
import com.shoppingmall.repository.deposit.PreDepositDetailRepository;
import com.shoppingmall.service.deposit.DepositScheduledService;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预存款定时任务服务实现类（支付中状态自动取消）
 *
 * @author ShoppingMall Team
 * @date 2025-12-26
 */
@Slf4j
@Service("depositPayingScheduledService")
@RequiredArgsConstructor
public class DepositScheduledServiceImpl implements DepositScheduledService {

    private final PreDepositDetailRepository preDepositDetailRepository;
    private final SystemConfigService systemConfigService;

    /**
     * 获取预存款记录自动取消时间（小时），从数据库配置读取，默认1小时
     * 每次执行定时任务时读取最新配置，确保配置修改后立即生效
     *
     * @return 自动取消时间（小时）
     */
    private Integer getDepositRecordTimeoutHours() {
        String timeoutStr = systemConfigService.getConfigValue("deposit.record-timeout-hours", "1");
        try {
            return Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            log.warn("预存款记录自动取消时间配置格式错误，使用默认值1小时: {}", timeoutStr);
            return 1;
        }
    }

    /**
     * 自动取消超时的支付中预存款记录
     * 每小时执行一次，检查超过指定时间仍处于支付中状态的预存款记录
     */
    @Override
    @Scheduled(fixedRate = 3600000) // 每小时执行一次（3600000毫秒 = 1小时）
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutDepositRecords() {
        try {
            // 每次执行时读取最新配置
            Integer timeoutHours = getDepositRecordTimeoutHours();
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(timeoutHours);

            // 查找超时的支付中预存款记录
            List<PreDepositDetail> timeoutRecords = preDepositDetailRepository.selectList(
                    new LambdaQueryWrapper<PreDepositDetail>()
                            .eq(PreDepositDetail::getStatus, DepositStatus.PAYING) // 支付中状态
                            .le(PreDepositDetail::getCreateTime, timeoutThreshold) // 创建时间早于超时阈值
            );

            if (timeoutRecords.isEmpty()) {
                return;
            }

            log.info("发现{}个超时的支付中预存款记录，开始自动更新为已超时", timeoutRecords.size());

            // 批量处理超时预存款记录
            for (PreDepositDetail record : timeoutRecords) {
                try {
                    // 更新预存款记录状态为已超时
                    record.setStatus(DepositStatus.TIMEOUT);
                    preDepositDetailRepository.updateById(record);

                    log.info("自动更新超时预存款记录成功: internalOrderNo={}, userId={}, amount={}, createTime={}", 
                            record.getInternalOrderNo(), record.getUserId(), record.getDepositAmount(), record.getCreateTime());
                } catch (Exception e) {
                    log.error("自动更新预存款记录失败: internalOrderNo={}", record.getInternalOrderNo(), e);
                    // 继续处理下一个记录，不中断整个任务
                }
            }

            log.info("自动更新超时预存款记录任务完成，共处理{}个记录", timeoutRecords.size());
        } catch (Exception e) {
            log.error("自动更新超时预存款记录任务执行异常", e);
            // 定时任务异常不影响系统运行
        }
    }
}

