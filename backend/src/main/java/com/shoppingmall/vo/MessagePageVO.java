package com.shoppingmall.vo;

import lombok.Data;

import java.util.List;

/**
 * 消息分页响应VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class MessagePageVO {

    /**
     * 消息列表
     */
    private List<MessageVO> records;

    /**
     * 总数
     */
    private Long total;

    /**
     * 未读数量
     */
    private Integer unreadCount;
}


