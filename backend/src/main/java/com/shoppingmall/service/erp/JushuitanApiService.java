package com.shoppingmall.service.erp;

import com.shoppingmall.dto.JushuitanOrderDTO;
import com.shoppingmall.dto.JushuitanLogisticsDTO;

import java.util.List;

/**
 * 聚水潭API基础服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public interface JushuitanApiService {

    /**
     * 上传订单到聚水潭
     *
     * @param orderDTO 订单数据
     * @return 聚水潭订单ID
     */
    String uploadOrder(JushuitanOrderDTO orderDTO);

    /**
     * 查询订单状态
     *
     * @param soId 订单号
     * @return 订单状态信息
     */
    String queryOrderStatus(String soId);

    /**
     * 查询物流信息
     *
     * @param soId 订单号
     * @return 物流信息列表
     */
    List<JushuitanLogisticsDTO> queryLogistics(String soId);

    /**
     * 批量查询物流信息
     *
     * @param soIds 订单号列表
     * @return 物流信息列表
     */
    List<JushuitanLogisticsDTO> batchQueryLogistics(List<String> soIds);

    /**
     * 检查配置是否启用
     *
     * @return 是否启用
     */
    boolean isEnabled();
}
