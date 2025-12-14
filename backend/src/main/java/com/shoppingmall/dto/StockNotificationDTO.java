package com.shoppingmall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 缺货登记DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class StockNotificationDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 通知方式（email-邮箱，sms-短信，both-两种）
     */
    private String notifyType;

    /**
     * 用户备注
     */
    private String remark;
}