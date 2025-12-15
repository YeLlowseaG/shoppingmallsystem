package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class MessageVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 发送人ID
     */
    private Long senderId;

    /**
     * 发送人姓名
     */
    private String senderName;

    /**
     * 接收人ID
     */
    private Long receiverId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（0-普通，1-系统，2-订单，3-其他）
     */
    private Integer messageType;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}


