package com.shoppingmall.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 聚水潭物流DTO（用于接收物流信息）
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
public class JushuitanLogisticsDTO {

    /**
     * 订单号（平台订单号）
     */
    private String soId;

    /**
     * ERP内部订单号
     */
    private Integer oId;

    /**
     * 店铺编号
     */
    private Integer shopId;

    /**
     * 物流公司编码
     */
    private String logisticsCode;

    /**
     * 物流公司名称
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 发货时间（yyyy-MM-dd HH:mm:ss）
     */
    private String sendDate;

    /**
     * 运费
     */
    private Double freight;

    /**
     * 包裹重量
     */
    private Double weight;

    /**
     * 发货仓编码（0表示主仓发货）
     */
    private Integer wmsCoId;

    /**
     * 售后单号（如果是补发或换货订单）
     */
    private Long asId;

    /**
     * 订单商品明细列表
     */
    private List<Map<String, Object>> items;
}
