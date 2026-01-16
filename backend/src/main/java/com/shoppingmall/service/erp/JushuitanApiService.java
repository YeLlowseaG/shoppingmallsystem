package com.shoppingmall.service.erp;

import com.shoppingmall.dto.JushuitanOrderDTO;
import com.shoppingmall.dto.JushuitanLogisticsDTO;
import com.shoppingmall.dto.JushuitanUploadOrderResponseDTO;

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
     * @return 聚水潭订单上传响应信息（包含完整响应JSON、ERP订单ID、ERP内部订单号等）
     */
    JushuitanUploadOrderResponseDTO uploadOrder(JushuitanOrderDTO orderDTO);

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

    /**
     * 根据内部订单号取消订单
     *
     * @param oIds 内部订单号列表
     * @param cancelType 取消类型（如"全额退款"）
     * @param remark 取消备注（如退款原因）
     * @return 响应JSON字符串
     */
    String cancelOrderByInternalId(List<Integer> oIds, String cancelType, String remark);
}
