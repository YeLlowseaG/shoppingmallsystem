package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购买咨询实体
 */
@Data
@TableName("consultation")
public class Consultation {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 用户ID（可为空，支持匿名咨询）
     */
    private Long userId;

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
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 回复管理员ID
     */
    private Long replyAdminId;

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