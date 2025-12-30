package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Data
public class ScheduledTaskExecutionLogVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务组
     */
    private String taskGroup;

    /**
     * Bean名称
     */
    private String beanName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 执行状态（0-失败，1-成功）
     */
    private Integer executeStatus;

    /**
     * 执行状态描述
     */
    private String executeStatusDesc;

    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;

    /**
     * 结束执行时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行类型（固定为2-手动执行，不记录自动执行的日志）
     */
    private Integer executeType;

    /**
     * 执行类型描述
     */
    private String executeTypeDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

