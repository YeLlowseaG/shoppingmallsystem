package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Data
@TableName("scheduled_task")
public class ScheduledTask {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * Cron表达式（固定频率任务可为空）
     */
    private String cronExpression;

    /**
     * Bean名称（Spring Bean名称）
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}














