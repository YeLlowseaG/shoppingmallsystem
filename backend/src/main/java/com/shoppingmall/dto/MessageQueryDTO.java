package com.shoppingmall.dto;

import lombok.Data;

/**
 * 消息查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class MessageQueryDTO {

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 是否已读
     */
    private Boolean isRead;
}


