package com.shoppingmall.dto;

import lombok.Data;

/**
 * 聚水潭订单上传响应DTO
 *
 * @author ShoppingMall Team
 * @date 2026-01-15
 */
@Data
public class JushuitanUploadOrderResponseDTO {

    /**
     * 完整的响应JSON字符串
     */
    private String responseJson;

    /**
     * ERP订单ID（so_id）
     */
    private String erpOrderId;

    /**
     * ERP内部订单号（o_id）
     */
    private String erpInternalOrderId;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 是否成功
     */
    private Boolean success;
}










