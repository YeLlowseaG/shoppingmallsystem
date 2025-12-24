package com.shoppingmall.dto;

import lombok.Data;

/**
 * 聚水潭物流DTO（用于接收物流信息）
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
public class JushuitanLogisticsDTO {

    /**
     * 订单号
     */
    private String soId;

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
}
