package com.shoppingmall.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.ShippingAddressDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.entity.User;
import com.shoppingmall.notification.service.NotificationService;
import com.shoppingmall.notification.util.WeChatWorkUtil;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 通知服务实现类
 * 支持企业微信群机器人通知
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SystemConfigService systemConfigService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 发送订单创建通知
     * 异步执行，不影响订单创建的主流程
     *
     * @param orderNo 订单号
     */
    @Override
    @Async("taskExecutor")
    public void sendOrderCreatedNotification(String orderNo) {
        try {
            log.info("准备发送订单创建通知，订单号：{}", orderNo);

            // 检查是否启用企业微信通知
            if (!isNotificationEnabled()) {
                log.info("企业微信通知未启用，跳过发送");
                return;
            }

            // 获取Webhook URL
            String webhookUrl = getWebhookUrl();
            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                log.warn("企业微信Webhook URL未配置，跳过发送");
                return;
            }

            // 查询订单信息
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderNo, orderNo);
            Order order = orderRepository.selectOne(orderWrapper);

            if (order == null) {
                log.error("订单不存在，无法发送通知，订单号：{}", orderNo);
                return;
            }

            // 查询用户信息
            User user = userRepository.selectById(order.getUserId());
            if (user == null) {
                log.warn("用户不存在，订单号：{}, 用户ID：{}", orderNo, order.getUserId());
            }

            // 查询订单商品
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

            // 构造通知消息
            String message = buildOrderCreatedMessage(order, user, orderItems);

            // 发送通知
            boolean success = WeChatWorkUtil.sendTextMessage(webhookUrl, message);
            if (success) {
                log.info("订单创建通知发送成功，订单号：{}", orderNo);
            } else {
                log.error("订单创建通知发送失败，订单号：{}", orderNo);
            }
        } catch (Exception e) {
            // 捕获所有异常，不影响主业务流程
            log.error("发送订单创建通知异常，订单号：{}", orderNo, e);
        }
    }

    /**
     * 发送支付成功通知
     * 异步执行，不影响支付回调的主流程
     *
     * @param orderNo 订单号
     */
    @Override
    @Async("taskExecutor")
    public void sendPaymentSuccessNotification(String orderNo) {
        try {
            log.info("准备发送支付成功通知，订单号：{}", orderNo);

            // 检查是否启用企业微信通知
            if (!isNotificationEnabled()) {
                log.info("企业微信通知未启用，跳过发送");
                return;
            }

            // 获取Webhook URL
            String webhookUrl = getWebhookUrl();
            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                log.warn("企业微信Webhook URL未配置，跳过发送");
                return;
            }

            // 查询订单信息
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderNo, orderNo);
            Order order = orderRepository.selectOne(orderWrapper);

            if (order == null) {
                log.error("订单不存在，无法发送通知，订单号：{}", orderNo);
                return;
            }

            // 查询用户信息
            User user = userRepository.selectById(order.getUserId());
            if (user == null) {
                log.warn("用户不存在，订单号：{}, 用户ID：{}", orderNo, order.getUserId());
            }

            // 查询订单商品
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

            // 构造通知消息
            String message = buildPaymentSuccessMessage(order, user, orderItems);

            // 发送通知
            boolean success = WeChatWorkUtil.sendTextMessage(webhookUrl, message);
            if (success) {
                log.info("支付成功通知发送成功，订单号：{}", orderNo);
            } else {
                log.error("支付成功通知发送失败，订单号：{}", orderNo);
            }
        } catch (Exception e) {
            // 捕获所有异常，不影响主业务流程
            log.error("发送支付成功通知异常，订单号：{}", orderNo, e);
        }
    }

    /**
     * 检查是否启用企业微信通知
     */
    private boolean isNotificationEnabled() {
        try {
            String enabled = systemConfigService.getConfigValue("wechat.work.notification.enabled");
            return "1".equals(enabled);
        } catch (Exception e) {
            log.error("读取企业微信通知开关配置失败", e);
            return false;
        }
    }

    /**
     * 获取企业微信Webhook URL
     */
    private String getWebhookUrl() {
        try {
            return systemConfigService.getConfigValue("wechat.work.webhook.url");
        } catch (Exception e) {
            log.error("读取企业微信Webhook URL配置失败", e);
            return null;
        }
    }

    /**
     * 构造订单创建通知消息
     */
    private String buildOrderCreatedMessage(Order order, User user, List<OrderItem> orderItems) {
        StringBuilder sb = new StringBuilder();
        sb.append("【新订单通知】\n\n");
        sb.append("订单号：").append(order.getOrderNo()).append("\n");
        sb.append("下单时间：").append(order.getCreateTime().format(DATE_TIME_FORMATTER)).append("\n");

        if (user != null) {
            String displayName = user.getRealName() != null ? user.getRealName() : user.getUsername();
            sb.append("客户名称：").append(displayName != null ? displayName : "未设置").append("\n");
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                // 手机号明文显示
                sb.append("手机号：").append(user.getPhone()).append("\n");
            }
        }

        sb.append("订单金额：¥").append(order.getTotalAmount()).append("\n");
        sb.append("订单状态：待支付\n\n");

        // 商品清单
        if (orderItems != null && !orderItems.isEmpty()) {
            sb.append("商品清单：\n");
            int index = 1;
            for (OrderItem item : orderItems) {
                sb.append(index++).append(". ");
                sb.append(item.getProductName());
                if (item.getSpecCombination() != null && !item.getSpecCombination().isEmpty()) {
                    sb.append("（").append(item.getSpecCombination()).append("）");
                }
                sb.append(" × ").append(item.getQuantity());
                sb.append("  ¥").append(item.getSubtotal()).append("\n");
            }
        }

        // 收货信息
        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
            sb.append("\n收货信息：\n");
            String addressText = formatShippingAddress(order.getShippingAddress());
            sb.append(addressText);
        }

        return sb.toString();
    }

    /**
     * 构造支付成功通知消息
     */
    private String buildPaymentSuccessMessage(Order order, User user, List<OrderItem> orderItems) {
        StringBuilder sb = new StringBuilder();
        sb.append("【支付成功通知】\n\n");
        sb.append("订单号：").append(order.getOrderNo()).append("\n");
        sb.append("下单时间：").append(order.getCreateTime().format(DATE_TIME_FORMATTER)).append("\n");

        if (user != null) {
            String displayName = user.getRealName() != null ? user.getRealName() : user.getUsername();
            sb.append("客户名称：").append(displayName != null ? displayName : "未设置").append("\n");
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                // 手机号明文显示
                sb.append("手机号：").append(user.getPhone()).append("\n");
            }
        }

        sb.append("订单金额：¥").append(order.getTotalAmount()).append("\n");
        sb.append("订单状态：已支付，待发货\n\n");

        // 商品清单
        if (orderItems != null && !orderItems.isEmpty()) {
            sb.append("商品清单：\n");
            int index = 1;
            for (OrderItem item : orderItems) {
                sb.append(index++).append(". ");
                sb.append(item.getProductName());
                if (item.getSpecCombination() != null && !item.getSpecCombination().isEmpty()) {
                    sb.append("（").append(item.getSpecCombination()).append("）");
                }
                sb.append(" × ").append(item.getQuantity());
                sb.append("  ¥").append(item.getSubtotal()).append("\n");
            }
        }

        // 收货信息
        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
            sb.append("\n收货信息：\n");
            String addressText = formatShippingAddress(order.getShippingAddress());
            sb.append(addressText);
        }

        return sb.toString();
    }

    /**
     * 格式化收货地址为易读文本
     * 将JSON格式的收货地址转换为文本格式
     */
    private String formatShippingAddress(String shippingAddressJson) {
        try {
            if (shippingAddressJson == null || shippingAddressJson.trim().isEmpty()) {
                return "收货地址未设置";
            }

            // 尝试解析JSON
            ShippingAddressDTO address = objectMapper.readValue(shippingAddressJson, ShippingAddressDTO.class);
            
            StringBuilder sb = new StringBuilder();
            
            // 收货人姓名
            if (address.getName() != null && !address.getName().trim().isEmpty()) {
                sb.append("收货人：").append(address.getName()).append("\n");
            }
            
            // 联系电话（优先使用mobile，其次phone）
            String phone = address.getMobile() != null && !address.getMobile().trim().isEmpty() 
                ? address.getMobile() 
                : address.getPhone();
            if (phone != null && !phone.trim().isEmpty()) {
                sb.append("联系电话：").append(phone).append("\n");
            }
            
            // 收货地址
            if (address.getFullAddress() != null && !address.getFullAddress().trim().isEmpty()) {
                sb.append("收货地址：").append(address.getFullAddress());
            } else {
                // 如果没有完整地址，组合省市区和详细地址
                StringBuilder addressBuilder = new StringBuilder();
                if (address.getProvince() != null) {
                    addressBuilder.append(address.getProvince());
                }
                if (address.getCity() != null) {
                    addressBuilder.append(address.getCity());
                }
                if (address.getDistrict() != null) {
                    addressBuilder.append(address.getDistrict());
                }
                if (address.getAddress() != null) {
                    addressBuilder.append(address.getAddress());
                }
                if (addressBuilder.length() > 0) {
                    sb.append("收货地址：").append(addressBuilder.toString());
                } else {
                    sb.append("收货地址：未设置");
                }
            }
            
            return sb.toString();
        } catch (Exception e) {
            // 如果解析失败，返回原始JSON（作为后备方案）
            log.warn("解析收货地址JSON失败，返回原始内容: {}", shippingAddressJson, e);
            return shippingAddressJson;
        }
    }

    /**
     * 获取支付方式文本
     */
    private String getPaymentMethodText(String paymentMethod) {
        if (paymentMethod == null) {
            return "未知";
        }
        switch (paymentMethod) {
            case "ALIPAY":
                return "支付宝";
            case "WECHAT":
                return "微信支付";
            case "DEPOSIT":
            case "PRE_DEPOSIT":
                return "预存款";
            case "OFFLINE":
                return "线下支付";
            default:
                return paymentMethod;
        }
    }
}
