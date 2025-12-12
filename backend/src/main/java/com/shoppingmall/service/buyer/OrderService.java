package com.shoppingmall.service.buyer;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.CreateOrderDTO;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;

/**
 * 订单服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param userId        用户ID
     * @param createOrderDTO 创建订单DTO
     * @return 订单号
     */
    String createOrder(Long userId, CreateOrderDTO createOrderDTO);

    /**
     * 获取订单列表
     *
     * @param userId        用户ID
     * @param orderQueryDTO 查询条件
     * @return 订单列表（分页）
     */
    IPage<OrderListVO> getOrderList(Long userId, OrderQueryDTO orderQueryDTO);

    /**
     * 获取订单详情
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(String orderNo, Long userId);

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     */
    void cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     */
    void confirmReceipt(String orderNo, Long userId);

    /**
     * 订单支付
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @param paymentDTO 支付信息
     * @return 支付响应（包含支付URL等）
     */
    com.shoppingmall.dto.PaymentResponseDTO payOrder(String orderNo, Long userId, com.shoppingmall.dto.OrderPaymentDTO paymentDTO);
}



