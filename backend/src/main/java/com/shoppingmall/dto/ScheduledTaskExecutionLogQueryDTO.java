package com.shoppingmall.dto;

import lombok.Data;

/**
 * 定时任务执行日志查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Data
public class ScheduledTaskExecutionLogQueryDTO {
    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 开始时间（查询开始执行时间 >= startTime 的记录）
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String startTime;

    /**
     * 结束时间（查询开始执行时间 <= endTime 的记录）
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String endTime;
}


















