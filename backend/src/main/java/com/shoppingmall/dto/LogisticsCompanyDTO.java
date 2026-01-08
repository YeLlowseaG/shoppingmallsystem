package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 物流公司DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class LogisticsCompanyDTO {

    /**
     * 物流公司编码
     */
    @NotBlank(message = "物流公司编码不能为空")
    private String companyCode;

    /**
     * 物流公司名称
     */
    @NotBlank(message = "物流公司名称不能为空")
    private String companyName;

    /**
     * 物流公司简称
     */
    private String companyShortName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 官网地址
     */
    private String website;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
}








































































