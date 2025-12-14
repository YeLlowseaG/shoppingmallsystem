package com.shoppingmall.controller.buyer;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.CreateOrderDTO;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.dto.OrderPaymentDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.service.buyer.OrderService;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import com.shoppingmall.vo.OrderStatisticsVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@RestController("buyerOrderController")
@RequestMapping("/api/buyer/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final HttpServletRequest request;

    /**
     * 创建订单
     */
    @PostMapping
    public Result<String> createOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        String orderNo = orderService.createOrder(userId, createOrderDTO);
        return Result.success("订单创建成功", orderNo);
    }

    /**
     * 获取订单列表
     */
    @GetMapping
    public Result<IPage<OrderListVO>> getOrderList(OrderQueryDTO orderQueryDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        IPage<OrderListVO> orderList = orderService.getOrderList(userId, orderQueryDTO);
        return Result.success(orderList);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        OrderDetailVO orderDetail = orderService.getOrderDetail(orderNo, userId);
        return Result.success(orderDetail);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<?> cancelOrder(@PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        orderService.cancelOrder(orderNo, userId);
        return Result.success("订单已取消");
    }

    /**
     * 确认收货
     */
    @PutMapping("/{orderNo}/confirm")
    public Result<?> confirmReceipt(@PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        orderService.confirmReceipt(orderNo, userId);
        return Result.success("确认收货成功");
    }

    /**
     * 订单支付
     */
    @PostMapping("/{orderNo}/pay")
    public Result<PaymentResponseDTO> payOrder(@PathVariable String orderNo, @Valid @RequestBody OrderPaymentDTO paymentDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        
        // 设置订单号
        paymentDTO.setOrderNo(orderNo);
        
        PaymentResponseDTO response = orderService.payOrder(orderNo, userId, paymentDTO);
        return Result.success("支付成功", response);
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        OrderStatisticsVO statistics = orderService.getOrderStatistics(userId);
        return Result.success("获取成功", statistics);
    }
}

