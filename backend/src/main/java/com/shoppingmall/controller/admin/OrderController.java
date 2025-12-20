package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.dto.OrderRefundRequestDTO;
import com.shoppingmall.entity.AdminUser;
import com.shoppingmall.repository.permission.AdminUserRepository;
import com.shoppingmall.service.admin.OrderService;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import com.shoppingmall.vo.OrderRefundVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final AdminUserRepository adminUserRepository;
    private final HttpServletRequest request;

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

    /**
     * 取消订单（管理员）
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<?> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success("订单已取消");
    }

    /**
     * 订单退款（管理员）
     * 支持选择部分SKU/商品进行退款
     */
    @PostMapping("/{orderNo}/refund")
    public Result<String> refundOrder(
            @PathVariable String orderNo,
            @Valid @RequestBody OrderRefundRequestDTO refundDTO) {
        // 获取当前管理员信息
        Long adminId = (Long) request.getAttribute("adminId");
        AdminUser adminUser = adminUserRepository.selectById(adminId);
        String adminName = adminUser != null ? adminUser.getUsername() : "系统";
        
        String refundNo = orderService.refundOrder(orderNo, refundDTO, adminId, adminName);
        return Result.success("退款成功", refundNo);
    }

    /**
     * 获取订单的退款列表
     */
    @GetMapping("/{orderNo}/refunds")
    public Result<java.util.List<OrderRefundVO>> getOrderRefundList(@PathVariable String orderNo) {
        java.util.List<OrderRefundVO> refundList = orderService.getOrderRefundList(orderNo);
        return Result.success(refundList);
    }

    /**
     * 查询退款记录列表（分页）
     */
    @GetMapping("/refunds")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<OrderRefundVO>> getRefundList(
            com.shoppingmall.dto.OrderRefundQueryDTO queryDTO) {
        com.baomidou.mybatisplus.core.metadata.IPage<OrderRefundVO> refundList = orderService.getRefundList(queryDTO);
        return Result.success(refundList);
    }

    /**
     * 获取退款记录详情
     */
    @GetMapping("/refunds/{refundId}")
    public Result<OrderRefundVO> getRefundDetail(@PathVariable Long refundId) {
        OrderRefundVO refundDetail = orderService.getRefundDetail(refundId);
        return Result.success(refundDetail);
    }
}

