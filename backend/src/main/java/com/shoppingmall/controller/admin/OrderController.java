package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.dto.OrderRefundRequestDTO;
import com.shoppingmall.entity.AdminUser;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.util.AlipayUtil;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.repository.permission.AdminUserRepository;
import com.shoppingmall.service.admin.OrderService;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanOrderService;
import com.shoppingmall.vo.JushuitanConfigVO;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import com.shoppingmall.vo.OrderRefundVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台订单控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Slf4j
@RestController("adminOrderController")
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AdminUserRepository adminUserRepository;
    private final HttpServletRequest request;
    private final OrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentConfigService paymentConfigService;
    private final ObjectMapper objectMapper;
    private final JushuitanOrderService jushuitanOrderService;
    private final JushuitanConfigService jushuitanConfigService;

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

    /**
     * 手动查询支付结果并补单
     * 用于处理支付回调丢失的情况
     */
    @PostMapping("/{orderNo}/sync-payment")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> syncPaymentStatus(@PathVariable @NotBlank String orderNo) {
        try {
            // 1. 查询订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderNo, orderNo);
            Order order = orderRepository.selectOne(orderWrapper);
            
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            
            // 2. 如果订单已经支付，直接返回
            if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
                return Result.success("订单已支付，无需补单");
            }
            
            // 3. 查询支付记录
            LambdaQueryWrapper<PaymentRecord> paymentWrapper = new LambdaQueryWrapper<>();
            paymentWrapper.eq(PaymentRecord::getOrderId, order.getId());
            paymentWrapper.orderByDesc(PaymentRecord::getCreateTime);
            paymentWrapper.last("LIMIT 1");
            PaymentRecord paymentRecord = paymentRecordRepository.selectOne(paymentWrapper);
            
            if (paymentRecord == null) {
                return Result.error(404, "支付记录不存在");
            }
            
            // 4. 如果支付记录已经处理，直接返回
            if (PaymentStatus.PAID.equals(paymentRecord.getPaymentStatus()) 
                    || PaymentStatus.REFUNDED.equals(paymentRecord.getPaymentStatus())) {
                return Result.success("支付记录已处理，无需补单");
            }
            
            // 5. 查询支付宝订单状态
            String paymentMethod = paymentRecord.getPaymentMethod();
            if (!PaymentMethod.ALIPAY.equals(paymentMethod)) {
                return Result.error(400, "当前只支持支付宝订单补单");
            }
            
            // 获取支付宝配置
            AlipayConfig alipayConfig = paymentConfigService.getAlipayConfig();
            if (alipayConfig == null) {
                return Result.error(400, "支付宝配置不存在");
            }
            
            AlipayConfig.AlipayEnvConfig envConfig = "production".equals(alipayConfig.getEnv())
                    ? alipayConfig.getProduction()
                    : alipayConfig.getSandbox();
            
            if (envConfig == null) {
                return Result.error(400, "支付宝环境配置不存在");
            }
            
            // 查询订单状态（使用订单号）
            Map<String, String> orderStatus = AlipayUtil.queryOrder(envConfig, orderNo);
            String tradeStatus = orderStatus.get("trade_status");
            String tradeNo = orderStatus.get("trade_no");
            
            if ("UNKNOWN".equals(tradeStatus)) {
                return Result.error(400, "无法查询到订单状态，请确认订单号是否正确");
            }
            
            // 6. 判断订单是否已支付
            boolean isPaid = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            
            if (!isPaid) {
                return Result.error(400, "订单未支付，当前状态：" + tradeStatus);
            }
            
            // 7. 补单：更新支付记录和订单状态
            paymentRecord.setPaymentStatus(PaymentStatus.PAID);
            paymentRecord.setPaymentTime(LocalDateTime.now());
            if (tradeNo != null && !tradeNo.isEmpty()) {
                paymentRecord.setExternalTradeNo(tradeNo);
            }
            
            // 构建回调数据（模拟回调数据）
            Map<String, Object> notifyData = new HashMap<>();
            notifyData.put("out_trade_no", orderNo);
            notifyData.put("trade_status", tradeStatus);
            notifyData.put("trade_no", tradeNo);
            
            try {
                paymentRecord.setCallbackData(objectMapper.writeValueAsString(notifyData));
            } catch (Exception e) {
                log.warn("保存回调数据失败", e);
            }
            
            paymentRecordRepository.updateById(paymentRecord);
            
            // 更新订单状态
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.PAID_UNSHIPPED);
            order.setPayTime(LocalDateTime.now());
            orderRepository.updateById(order);
            
            // 自动推送订单到聚水潭ERP
            try {
                JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
                if (config != null && config.getAutoPushOrder() == 1) {
                    log.info("自动推送订单到聚水潭ERP: orderId={}, orderNo={}", order.getId(), orderNo);
                    jushuitanOrderService.pushOrder(order.getId());
                }
            } catch (Exception e) {
                log.error("自动推送订单到ERP失败: orderId={}, orderNo={}", order.getId(), orderNo, e);
            }
            
            log.info("手动补单成功: orderNo={}, tradeNo={}, tradeStatus={}", orderNo, tradeNo, tradeStatus);
            return Result.success("补单成功，订单已更新为已支付状态");
            
        } catch (Exception e) {
            log.error("手动补单失败: orderNo={}", orderNo, e);
            return Result.error(500, "补单失败：" + e.getMessage());
        }
    }
}

