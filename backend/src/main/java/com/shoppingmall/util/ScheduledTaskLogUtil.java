package com.shoppingmall.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.entity.ScheduledTask;
import com.shoppingmall.entity.ScheduledTaskExecutionLog;
import com.shoppingmall.mapper.ScheduledTaskExecutionLogMapper;
import com.shoppingmall.mapper.ScheduledTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 定时任务执行日志工具类
 * 用于记录定时任务自动执行的日志
 *
 * @author ShoppingMall Team
 * @date 2026-01-16
 */
@Slf4j
@Component
public class ScheduledTaskLogUtil {

    @Resource
    private ScheduledTaskExecutionLogMapper scheduledTaskExecutionLogMapper;

    @Resource
    private ScheduledTaskMapper scheduledTaskMapper;

    /**
     * 根据Bean名称和方法名称查询任务ID
     *
     * @param beanName Bean名称
     * @param methodName 方法名称
     * @return 任务ID，如果找不到则返回null
     */
    private Long findTaskId(String beanName, String methodName) {
        try {
            LambdaQueryWrapper<ScheduledTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ScheduledTask::getBeanName, beanName);
            wrapper.eq(ScheduledTask::getMethodName, methodName);
            wrapper.last("LIMIT 1");
            ScheduledTask task = scheduledTaskMapper.selectOne(wrapper);
            return task != null ? task.getId() : null;
        } catch (Exception e) {
            log.warn("查询任务ID失败: beanName={}, methodName={}", beanName, methodName, e);
            return null;
        }
    }

    /**
     * 记录定时任务自动执行日志
     *
     * @param taskName 任务名称
     * @param taskGroup 任务组（可为null）
     * @param beanName Bean名称
     * @param methodName 方法名称
     * @param startTime 开始执行时间
     * @param success 是否成功
     * @param errorMessage 错误信息（失败时提供）
     */
    public void logAutoExecution(String taskName, String taskGroup, 
                                  String beanName, String methodName,
                                  LocalDateTime startTime, boolean success, String errorMessage) {
        try {
            // 尝试查询任务ID
            Long taskId = findTaskId(beanName, methodName);

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();

            ScheduledTaskExecutionLog executionLog = new ScheduledTaskExecutionLog();
            executionLog.setTaskId(taskId);
            executionLog.setTaskName(taskName);
            executionLog.setTaskGroup(taskGroup);
            executionLog.setBeanName(beanName);
            executionLog.setMethodName(methodName);
            executionLog.setStartTime(startTime);
            executionLog.setEndTime(endTime);
            executionLog.setDuration(duration);
            executionLog.setExecuteType(1); // 1-定时执行（自动执行）
            executionLog.setExecuteStatus(success ? 1 : 0); // 1-成功，0-失败
            if (errorMessage != null && !errorMessage.isEmpty()) {
                executionLog.setErrorMessage(errorMessage);
            }

            scheduledTaskExecutionLogMapper.insert(executionLog);
        } catch (Exception e) {
            // 记录日志失败不影响任务执行
            log.error("记录定时任务执行日志失败: taskName={}, beanName={}, methodName={}", 
                    taskName, beanName, methodName, e);
        }
    }
}

