package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shoppingmall.common.constant.DepositType;
import com.shoppingmall.entity.PreDepositDetail;
import com.shoppingmall.repository.deposit.PreDepositDetailRepository;
import com.shoppingmall.service.buyer.DepositScheduledService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预存款定时任务服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositScheduledServiceImpl implements DepositScheduledService {

    private final PreDepositDetailRepository preDepositDetailRepository;

    /**
     * 支付超时时间（分钟）
     */
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    /**
     * 处理超时的充值记录
     * 每分钟执行一次，检查超过30分钟未支付的充值记录
     */
    @Override
    @Scheduled(fixedRate = 60000) // 每分钟执行一次（60000毫秒）
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutRecharges() {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);

            // 查找超时的待审核充值记录
            List<PreDepositDetail> timeoutRecords = preDepositDetailRepository.selectList(
                    new LambdaQueryWrapper<PreDepositDetail>()
                            .eq(PreDepositDetail::getType, DepositType.RECHARGE)
                            .eq(PreDepositDetail::getStatus, 0) // 待审核状态
                            .le(PreDepositDetail::getCreateTime, timeoutThreshold)
            );

            if (timeoutRecords.isEmpty()) {
                return;
            }

            log.info("发现{}条超时的充值记录，开始处理", timeoutRecords.size());

            // 批量更新超时记录
            for (PreDepositDetail detail : timeoutRecords) {
                String originalRemark = detail.getRemark();
                String updatedRemark = originalRemark;
                
                // 如果备注中还没有超时标记，则添加
                if (originalRemark == null || !originalRemark.contains("[支付超时]")) {
                    updatedRemark = (originalRemark == null ? "" : originalRemark) + " [支付超时]";
                }

                // 更新状态和备注
                preDepositDetailRepository.update(
                        new LambdaUpdateWrapper<PreDepositDetail>()
                                .eq(PreDepositDetail::getId, detail.getId())
                                .set(PreDepositDetail::getStatus, 2) // 已拒绝
                                .set(PreDepositDetail::getRemark, updatedRemark)
                );

                log.info("充值记录超时，已标记为已拒绝，内部订单号：{}，创建时间：{}", 
                        detail.getInternalOrderNo(), detail.getCreateTime());
            }

            log.info("超时充值记录处理完成，共处理{}条", timeoutRecords.size());
        } catch (Exception e) {
            log.error("处理超时充值记录时发生错误", e);
            // 定时任务中的异常不应该影响系统运行，只记录日志
        }
    }
}



