package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.PaymentApiLogQueryDTO;
import com.shoppingmall.dto.ScheduledTaskExecutionLogQueryDTO;
import com.shoppingmall.dto.ScheduledTaskQueryDTO;
import com.shoppingmall.vo.PaymentApiLogVO;
import com.shoppingmall.vo.ScheduledTaskExecutionLogVO;
import com.shoppingmall.vo.ScheduledTaskVO;

/**
 * 系统管理服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
public interface SystemService {

    /**
     * 分页查询支付接口日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<PaymentApiLogVO> getPaymentApiLogList(PaymentApiLogQueryDTO queryDTO);

    /**
     * 分页查询定时任务列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<ScheduledTaskVO> getScheduledTaskList(ScheduledTaskQueryDTO queryDTO);

    /**
     * 立即执行定时任务
     *
     * @param taskId 任务ID
     */
    void executeTaskNow(Long taskId);

    /**
     * 切换任务状态
     *
     * @param taskId 任务ID
     * @param status 状态（0-已停止，1-运行中）
     */
    void toggleTaskStatus(Long taskId, Integer status);

    /**
     * 分页查询任务执行日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<ScheduledTaskExecutionLogVO> getTaskExecutionLogList(ScheduledTaskExecutionLogQueryDTO queryDTO);
}

