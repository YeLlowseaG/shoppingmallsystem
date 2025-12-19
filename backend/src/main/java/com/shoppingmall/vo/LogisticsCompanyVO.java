package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流公司VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class LogisticsCompanyVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 物流公司编码
     */
    private String companyCode;

    /**
     * 物流公司名称
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}



































