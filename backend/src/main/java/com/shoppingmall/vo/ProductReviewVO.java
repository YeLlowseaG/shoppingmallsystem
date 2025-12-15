package com.shoppingmall.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评价视图对象
 */
@Data
public class ProductReviewVO {
    /**
     * 评价ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 评分：1-5星
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String reviewContent;

    /**
     * 评价图片列表
     */
    private List<String> reviewImages;

    /**
     * 管理员回复
     */
    private String adminReply;

    /**
     * 状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}