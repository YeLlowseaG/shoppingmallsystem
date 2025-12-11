package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;

/**
 * 管理后台订单服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
public interface OrderService {

    /**
     * 获取订单列表（管理员）
     *
     * @param orderQueryDTO 查询条件
     * @return 订单列表（分页）
     */
    IPage<OrderListVO> getOrderList(OrderQueryDTO orderQueryDTO);

    /**
     * 获取订单详情（管理员）
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(String orderNo);

    /**
     * 确认订单（管理员）
     *
     * @param orderNo 订单号
     */
    void confirmOrder(String orderNo);

    /**
     * 发货（管理员）
     *
     * @param orderNo          订单号
     * @param logisticsCompany 物流公司
     * @param logisticsNo      物流单号
     */
    void shipOrder(String orderNo, String logisticsCompany, String logisticsNo);

    /**
     * 添加订单备注（管理员）
     *
     * @param orderNo 订单号
     * @param remark  备注
     */
    void addOrderRemark(String orderNo, String remark);

    /**
     * 导出订单
     *
     * @param orderQueryDTO 查询条件
     * @return 订单数据列表
     */
    // List<OrderExportVO> exportOrders(OrderQueryDTO orderQueryDTO);
}



