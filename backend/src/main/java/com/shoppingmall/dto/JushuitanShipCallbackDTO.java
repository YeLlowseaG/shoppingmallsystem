package com.shoppingmall.dto;

import lombok.Data;

/**
 * 聚水潭发货回调DTO（用于接收ERP发送的发货信息）
 *
 * @author ShoppingMall Team
 * @date 2025-12-31
 */
@Data
public class JushuitanShipCallbackDTO {

    /**
     * ERP订单号（聚水潭订单号，可选）
     */
    private String erpOrderNo;

    /**
     * 系统订单号（so_id，必填）
     */
    private String orderNo;

    /**
     * 物流公司编码（可选）
     */
    private String logisticsCode;

    /**
     * 物流公司名称（必填）
     */
    private String logisticsCompany;

    /**
     * 物流单号（必填）
     */
    private String logisticsNo;

    /**
     * 发货时间（格式：yyyy-MM-dd HH:mm:ss，可选）
     */
    private String shipTime;

    /**
     * 签名（用于验证，可选）
     */
    private String sign;
}

