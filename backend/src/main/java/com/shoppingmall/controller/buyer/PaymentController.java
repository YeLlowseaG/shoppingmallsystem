package com.shoppingmall.controller.buyer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 支付控制器（处理支付回调）
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Slf4j
@RestController
@RequestMapping("/api/buyer/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ObjectMapper objectMapper;

    /**
     * 支付回调接口（模拟支付宝/微信支付回调）
     */
    @PostMapping("/callback")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> paymentCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            log.info("收到支付回调: {}", callbackData);
            
            // 从回调数据中提取信息
            String orderNo = (String) callbackData.get("outTradeNo"); // 订单号
            if (orderNo == null) {
                orderNo = (String) callbackData.get("out_trade_no");
            }
            
            String tradeNo = (String) callbackData.get("tradeNo"); // 外部交易号
            if (tradeNo == null) {
                tradeNo = (String) callbackData.get("trade_no");
            }
            
            String tradeStatus = (String) callbackData.get("tradeStatus"); // 交易状态
            if (tradeStatus == null) {
                tradeStatus = (String) callbackData.get("trade_status");
            }
            
            String paymentMethod = (String) callbackData.get("paymentMethod"); // 支付方式
            if (paymentMethod == null) {
                paymentMethod = (String) callbackData.get("payment_method");
            }
            
            if (orderNo == null) {
                return Result.error(400, "回调数据缺少订单号");
            }
            
            // 判断支付是否成功
            boolean success = "TRADE_SUCCESS".equals(tradeStatus) 
                    || "SUCCESS".equals(tradeStatus)
                    || "PAID".equals(tradeStatus);
            
            // 查找订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderNo, orderNo);
            Order order = orderRepository.selectOne(orderWrapper);
            
            if (order == null) {
                log.warn("支付回调：订单不存在，orderNo={}", orderNo);
                return Result.error(404, "订单不存在");
            }
            
            // 查找支付记录
            LambdaQueryWrapper<PaymentRecord> paymentWrapper = new LambdaQueryWrapper<>();
            paymentWrapper.eq(PaymentRecord::getOrderId, order.getId());
            paymentWrapper.eq(PaymentRecord::getPaymentMethod, paymentMethod != null ? paymentMethod : "ALIPAY");
            paymentWrapper.orderByDesc(PaymentRecord::getCreateTime);
            paymentWrapper.last("LIMIT 1");
            PaymentRecord paymentRecord = paymentRecordRepository.selectOne(paymentWrapper);
            
            if (paymentRecord == null) {
                log.warn("支付回调：支付记录不存在，orderNo={}", orderNo);
                return Result.error(404, "支付记录不存在");
            }
            
            // 如果已经处理过，直接返回成功
            if (PaymentStatus.PAID.equals(paymentRecord.getPaymentStatus())) {
                log.info("支付回调：订单已支付，忽略重复回调，orderNo={}", orderNo);
                return Result.success("订单已支付");
            }
            
            // 验证回调数据（模拟支付模式下跳过验证）
            boolean verified = paymentService.verifyCallback(
                    paymentMethod != null ? paymentMethod.toLowerCase() : "alipay", 
                    callbackData
            );
            
            if (!verified) {
                log.warn("支付回调：验证失败，orderNo={}", orderNo);
                return Result.error(400, "回调验证失败");
            }
            
            if (success) {
                // 支付成功
                // 更新支付记录
                paymentRecord.setPaymentStatus(PaymentStatus.PAID); // 已支付
                paymentRecord.setPaymentTime(LocalDateTime.now());
                try {
                    paymentRecord.setCallbackData(objectMapper.writeValueAsString(callbackData));
                } catch (Exception e) {
                    log.warn("保存回调数据失败", e);
                }
                if (tradeNo != null) {
                    // 可以将外部交易号保存到备注或其他字段
                }
                paymentRecordRepository.updateById(paymentRecord);
                
                // 更新订单状态
                order.setPaymentStatus(PaymentStatus.PAID); // 已支付
                order.setOrderStatus(OrderStatus.PAID_UNSHIPPED);
                order.setPayTime(LocalDateTime.now());
                orderRepository.updateById(order);
                
                log.info("支付回调处理成功: orderNo={}, tradeNo={}", orderNo, tradeNo);
            } else {
                // 支付失败
                paymentRecord.setPaymentStatus(PaymentStatus.FAILED); // 已失败
                paymentRecordRepository.updateById(paymentRecord);
                
                log.warn("支付回调：支付失败，orderNo={}, tradeStatus={}", orderNo, tradeStatus);
            }
            
            return Result.success("回调处理成功");
        } catch (Exception e) {
            log.error("支付回调处理异常", e);
            return Result.error(500, "回调处理失败：" + e.getMessage());
        }
    }

    /**
     * 模拟支付成功（用于测试）
     */
    @PostMapping("/mock/success")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> mockPaymentSuccess(@RequestParam String orderNo, @RequestParam(required = false) String paymentMethod) {
        try {
            if (paymentMethod == null) {
                paymentMethod = "ALIPAY";
            }
            
            // 生成模拟交易号
            String tradeNo = "MOCK_" + paymentMethod + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // 构建回调数据
            Map<String, Object> callbackData = Map.of(
                    "outTradeNo", orderNo,
                    "tradeNo", tradeNo,
                    "tradeStatus", "TRADE_SUCCESS",
                    "paymentMethod", paymentMethod
            );
            
            return paymentCallback(callbackData);
        } catch (Exception e) {
            log.error("模拟支付成功处理异常", e);
            return Result.error(500, "处理失败：" + e.getMessage());
        }
    }
}

