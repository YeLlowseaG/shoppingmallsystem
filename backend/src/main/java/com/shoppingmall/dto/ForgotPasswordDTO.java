package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class ForgotPasswordDTO {

    /**
     * 用户名（必填）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
}

