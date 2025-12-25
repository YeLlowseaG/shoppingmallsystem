package com.shoppingmall.payment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.payment.service.PaymentGatewayService;
import com.shoppingmall.payment.util.AlipayUtil;
import com.shoppingmall.payment.util.WeChatPayUtil;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanOrderService;
import com.shoppingmall.vo.JushuitanConfigVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@RestController
@RequestMapping("/api/buyer/payment")
@RequiredArgsConstructor
public class PaymentNotifyController {

    private final PaymentGatewayService paymentGatewayService;
    private final OrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ObjectMapper objectMapper;
    private final JushuitanOrderService jushuitanOrderService;
    private final JushuitanConfigService jushuitanConfigService;

    /**
     * 微信支付回调
     */
    @PostMapping("/wechat/notify")
    @Transactional(rollbackFor = Exception.class)
    public String wechatNotify(HttpServletRequest request) {
        try {
            log.info("收到微信支付回调");

            // 读取回调数据（微信支付回调可能是XML格式或JSON格式）
            // TODO: 根据微信支付实际回调格式解析数据
            // 这里先使用通用的Map接收
            Map<String, Object> notifyData = parseWeChatNotifyData(request);

            // 验证回调（在handlePaymentNotify中会调用策略的verifyCallback方法）
            paymentGatewayService.handlePaymentNotify(PaymentMethod.WECHAT, notifyData);

            // 处理业务逻辑
            processPaymentNotify(PaymentMethod.WECHAT, notifyData);

            // 微信支付需要返回特定格式的响应
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + e.getMessage() + "]]></return_msg></xml>";
        }
    }

    /**
     * 支付宝支付回调
     */
    @PostMapping("/alipay/notify")
    @Transactional(rollbackFor = Exception.class)
    public String alipayNotify(HttpServletRequest request) {
        try {
            log.info("收到支付宝支付回调");

            // 读取回调数据（支付宝回调是表单参数格式）
            Map<String, Object> notifyData = parseAlipayNotifyData(request);

            // 验证回调
            paymentGatewayService.handlePaymentNotify(PaymentMethod.ALIPAY, notifyData);

            // 处理业务逻辑
            processPaymentNotify(PaymentMethod.ALIPAY, notifyData);

            // 支付宝需要返回"success"
            return "success";
        } catch (Exception e) {
            log.error("处理支付宝支付回调失败", e);
            return "fail";
        }
    }

    /**
     * 处理支付回调业务逻辑
     */
    private void processPaymentNotify(String paymentMethod, Map<String, Object> notifyData) {
        // 从回调数据中提取订单号
        String orderNo = extractOrderNo(notifyData);
        if (orderNo == null) {
            throw new RuntimeException("回调数据缺少订单号");
        }

        // 提取交易状态
        String tradeStatus = extractTradeStatus(notifyData);
        boolean success = isPaymentSuccess(tradeStatus);

        // 查找订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderRepository.selectOne(orderWrapper);

        if (order == null) {
            log.warn("支付回调：订单不存在，orderNo={}", orderNo);
            throw new RuntimeException("订单不存在：" + orderNo);
        }

        // 查找支付记录
        LambdaQueryWrapper<PaymentRecord> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(PaymentRecord::getOrderId, order.getId());
        paymentWrapper.eq(PaymentRecord::getPaymentMethod, paymentMethod);
        paymentWrapper.orderByDesc(PaymentRecord::getCreateTime);
        paymentWrapper.last("LIMIT 1");
        PaymentRecord paymentRecord = paymentRecordRepository.selectOne(paymentWrapper);

        if (paymentRecord == null) {
            log.warn("支付回调：支付记录不存在，orderNo={}", orderNo);
            throw new RuntimeException("支付记录不存在：" + orderNo);
        }

        // 如果已经处理过，直接返回
        if (PaymentStatus.PAID.equals(paymentRecord.getPaymentStatus())
                || PaymentStatus.REFUNDED.equals(paymentRecord.getPaymentStatus())) {
            log.info("支付回调：订单已处理，忽略重复回调，orderNo={}, status={}", orderNo, paymentRecord.getPaymentStatus());
            return;
        }

        if (success) {
            // 支付成功
            paymentRecord.setPaymentStatus(PaymentStatus.PAID);
            paymentRecord.setPaymentTime(LocalDateTime.now());
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

            log.info("支付回调处理成功: orderNo={}", orderNo);
        } else {
            // 支付失败或关闭
            boolean isClosed = isPaymentClosed(tradeStatus);
            if (isClosed) {
                paymentRecord.setPaymentStatus(PaymentStatus.CLOSED);
                log.info("支付回调：订单已关闭，orderNo={}, tradeStatus={}", orderNo, tradeStatus);
            } else {
                paymentRecord.setPaymentStatus(PaymentStatus.FAILED);
                log.warn("支付回调：支付失败，orderNo={}, tradeStatus={}", orderNo, tradeStatus);
            }
            paymentRecordRepository.updateById(paymentRecord);
        }
    }

    /**
     * 解析微信支付回调数据
     */
    private Map<String, Object> parseWeChatNotifyData(HttpServletRequest request) {
        try {
            // 读取请求体（XML格式）
            String xmlBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            log.info("微信支付回调原始数据: {}", xmlBody);

            // 解析XML
            Map<String, String> xmlMap = parseXmlToMap(xmlBody);
            
            // 转换为Object类型Map
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<String, String> entry : xmlMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }

            return result;

        } catch (IOException e) {
            log.error("解析微信支付回调数据失败", e);
            throw new RuntimeException("解析回调数据失败", e);
        }
    }

    /**
     * 解析XML为Map（简单实现）
     */
    private Map<String, String> parseXmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        try {
            if (xml == null || xml.isEmpty()) {
                return map;
            }

            // 移除XML声明和根标签
            xml = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            xml = xml.replace("<xml>", "").replace("</xml>", "");

            // 解析每个字段
            String[] fields = xml.split("><");
            for (String field : fields) {
                field = field.replace("<", "").replace(">", "");
                if (field.contains("CDATA")) {
                    int start = field.indexOf("CDATA[") + 6;
                    int end = field.indexOf("]]");
                    if (start > 5 && end > start) {
                        String key = field.substring(0, field.indexOf("<"));
                        String value = field.substring(start, end);
                        map.put(key, value);
                    }
                } else if (field.contains("</")) {
                    String[] parts = field.split("</");
                    if (parts.length == 2) {
                        String key = parts[0];
                        String value = parts[1];
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析XML失败: {}", xml, e);
        }
        return map;
    }

    /**
     * 解析支付宝回调数据
     */
    private Map<String, Object> parseAlipayNotifyData(HttpServletRequest request) {
        try {
            // 支付宝回调是表单参数格式
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, Object> result = new HashMap<>();

            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                if (values != null && values.length > 0) {
                    result.put(key, values[0]); // 取第一个值
                }
            }

            log.info("支付宝回调参数: {}", result);
            return result;

        } catch (Exception e) {
            log.error("解析支付宝回调数据失败", e);
            throw new RuntimeException("解析回调数据失败", e);
        }
    }

    /**
     * 从回调数据中提取订单号
     */
    private String extractOrderNo(Map<String, Object> notifyData) {
        // 微信支付使用 out_trade_no
        // 支付宝也使用 out_trade_no
        String orderNo = (String) notifyData.get("out_trade_no");
        if (orderNo == null) {
            orderNo = (String) notifyData.get("outTradeNo");
        }
        if (orderNo == null) {
            orderNo = (String) notifyData.get("orderNo");
        }
        return orderNo;
    }

    /**
     * 从回调数据中提取交易状态
     */
    private String extractTradeStatus(Map<String, Object> notifyData) {
        // 微信支付使用 trade_state
        String status = (String) notifyData.get("trade_state");
        // 支付宝使用 trade_status
        if (status == null) {
            status = (String) notifyData.get("trade_status");
        }
        if (status == null) {
            status = (String) notifyData.get("tradeStatus");
        }
        // 微信支付也可能使用 result_code
        if (status == null) {
            String resultCode = (String) notifyData.get("result_code");
            if ("SUCCESS".equals(resultCode)) {
                status = "SUCCESS";
            }
        }
        return status;
    }

    /**
     * 判断支付是否成功
     */
    private boolean isPaymentSuccess(String tradeStatus) {
        if (tradeStatus == null) {
            return false;
        }
        // 微信支付：SUCCESS
        // 支付宝：TRADE_SUCCESS, TRADE_FINISHED
        return "SUCCESS".equalsIgnoreCase(tradeStatus)
                || "TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)
                || "TRADE_FINISHED".equalsIgnoreCase(tradeStatus)
                || "PAID".equalsIgnoreCase(tradeStatus);
    }

    /**
     * 判断支付是否关闭
     */
    private boolean isPaymentClosed(String tradeStatus) {
        if (tradeStatus == null) {
            return false;
        }
        return "CLOSED".equalsIgnoreCase(tradeStatus)
                || "TRADE_CLOSED".equalsIgnoreCase(tradeStatus)
                || "CANCEL".equalsIgnoreCase(tradeStatus);
    }
}

