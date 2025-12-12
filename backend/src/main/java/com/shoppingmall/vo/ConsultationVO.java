package com.shoppingmall.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 咨询视图对象
 */
@Data
public class ConsultationVO {
    /**
     * 咨询ID
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
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 咨询内容
     */
    private String consultationContent;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 状态：0-待回复，1-已回复，2-已关闭
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 回复管理员ID
     */
    private Long replyAdminId;

    /**
     * 回复管理员名称
     */
    private String replyAdminName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}