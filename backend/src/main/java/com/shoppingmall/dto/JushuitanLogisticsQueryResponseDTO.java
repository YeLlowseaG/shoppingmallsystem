package com.shoppingmall.dto;

import lombok.Data;

import java.util.List;

/**
 * 聚水潭物流查询响应DTO
 *
 * @author ShoppingMall Team
 * @date 2026-01-16
 */
@Data
public class JushuitanLogisticsQueryResponseDTO {

    /**
     * 完整的响应JSON字符串
     */
    private String responseJson;

    /**
     * 解析后的物流信息列表
     */
    private List<JushuitanLogisticsDTO> logisticsList;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;
}

