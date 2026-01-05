package com.shoppingmall.dto;

import lombok.Data;

/**
 * 定时任务查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Data
public class ScheduledTaskQueryDTO {
    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;

    /**
     * 任务名称（模糊查询）
     */
    private String taskName;

    /**
     * 状态（0-已停止，1-运行中）
     */
    private Integer status;
}











