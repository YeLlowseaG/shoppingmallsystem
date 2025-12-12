package com.shoppingmall.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 评价审核DTO
 */
@Data
public class ReviewAuditDTO {
    
    /**
     * 审核状态：1-通过，2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态值不正确")
    @Max(value = 2, message = "审核状态值不正确")
    private Integer status;
}