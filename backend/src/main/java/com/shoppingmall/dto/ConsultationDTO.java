package com.shoppingmall.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 咨询DTO
 */
@Data
public class ConsultationDTO {
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 联系人姓名
     */
    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 50, message = "联系人姓名不能超过50个字符")
    private String contactName;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String contactEmail;

    /**
     * 咨询内容
     */
    @NotBlank(message = "咨询内容不能为空")
    @Size(max = 1000, message = "咨询内容不能超过1000个字符")
    private String consultationContent;
}