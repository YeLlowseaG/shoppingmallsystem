package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.service.admin.OrderService;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台订单控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@RestController("adminOrderController")
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取订单列表
     */
    @GetMapping
    public Result<IPage<OrderListVO>> getOrderList(OrderQueryDTO orderQueryDTO) {
        IPage<OrderListVO> orderList = orderService.getOrderList(orderQueryDTO);
        return Result.success(orderList);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        OrderDetailVO orderDetail = orderService.getOrderDetail(orderNo);
        return Result.success(orderDetail);
    }

    /**
     * 确认订单
     */
    @PutMapping("/{orderNo}/confirm")
    public Result<?> confirmOrder(@PathVariable String orderNo) {
        orderService.confirmOrder(orderNo);
        return Result.success("订单确认成功");
    }

    /**
     * 发货
     */
    @PutMapping("/{orderNo}/ship")
    public Result<?> shipOrder(
        @PathVariable String orderNo,
        @RequestParam @NotBlank(message = "物流公司不能为空") String logisticsCompany,
        @RequestParam @NotBlank(message = "物流单号不能为空") String logisticsNo
    ) {
        orderService.shipOrder(orderNo, logisticsCompany, logisticsNo);
        return Result.success("发货成功");
    }

    /**
     * 添加订单备注
     */
    @PutMapping("/{orderNo}/remark")
    public Result<?> addOrderRemark(
        @PathVariable String orderNo,
        @RequestParam String remark
    ) {
        orderService.addOrderRemark(orderNo, remark);
        return Result.success("备注添加成功");
    }
}

