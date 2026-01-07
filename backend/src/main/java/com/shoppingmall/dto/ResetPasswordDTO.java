package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置密码DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class ResetPasswordDTO {

    /**
     * 重置验证码（必填）
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 新密码（必填）
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /**
     * 确认密码（必填）
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
















































