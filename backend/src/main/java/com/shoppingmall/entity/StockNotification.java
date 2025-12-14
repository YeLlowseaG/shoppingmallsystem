package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缺货登记实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
@TableName("stock_notification")
public class StockNotification {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（冗余字段）
     */
    private String productName;

    /**
     * 商品编码（冗余字段）
     */
    private String productCode;

    /**
     * 商品主图（冗余字段）
     */
    private String mainImage;

    /**
     * 商品价格（冗余字段）
     */
    private BigDecimal basePrice;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 状态（0-待通知，1-已通知，2-已取消）
     */
    private Integer status;

    /**
     * 通知方式（email-邮箱，sms-短信，both-两种）
     */
    private String notifyType;

    /**
     * 通知时间
     */
    private LocalDateTime notifiedAt;

    /**
     * 登记过期时间
     */
    private LocalDateTime expiredAt;

    /**
     * 用户备注
     */
    private String remark;

    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

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