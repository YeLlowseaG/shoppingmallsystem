package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Data
public class ScheduledTaskVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务组
     */
    private String taskGroup;

    /**
     * Cron表达式
     */
    private String cronExpression;

    /**
     * Bean名称
     */
    private String beanName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 状态（0-已停止，1-运行中）
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}


