package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送方式VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class ShippingMethodVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 配送方式编码
     */
    private String methodCode;

    /**
     * 配送方式名称
     */
    private String methodName;

    /**
     * 关联物流公司ID
     */
    private Long logisticsCompanyId;

    /**
     * 物流公司名称
     */
    private String logisticsCompanyName;

    /**
     * 配送方式描述
     */
    private String description;

    /**
     * 关联运费模板ID
     */
    private Long shippingTemplateId;

    /**
     * 基础运费
     */
    private BigDecimal basePrice;

    /**
     * 运费计算方式（1-固定运费，2-按重量，3-按件数，4-按金额，5-运费模板）
     */
    private Integer calculationType;

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



















































