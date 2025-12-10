package com.shoppingmall.service.admin.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.dto.ShippingAddressDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.entity.OrderLogistics;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderLogisticsRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.service.admin.OrderService;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台订单服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Slf4j
@Service("adminOrderServiceImpl")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderLogisticsRepository orderLogisticsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<OrderListVO> getOrderList(OrderQueryDTO orderQueryDTO) {
        Page<Order> page = new Page<>(orderQueryDTO.getPageNum(), orderQueryDTO.getPageSize());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 订单号查询
        if (orderQueryDTO.getOrderNo() != null && !orderQueryDTO.getOrderNo().trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, orderQueryDTO.getOrderNo());
        }

        // 订单状态筛选（支持字符串和数字）
        if (orderQueryDTO.getOrderStatus() != null && !orderQueryDTO.getOrderStatus().trim().isEmpty()) {
            Integer status = convertStatusToInt(orderQueryDTO.getOrderStatus());
            if (status != null) {
                wrapper.eq(Order::getOrderStatus, status);
            }
        }

        // 日期范围筛选
        if (orderQueryDTO.getStartDate() != null) {
            wrapper.ge(Order::getCreateTime, orderQueryDTO.getStartDate().atStartOfDay());
        }
        if (orderQueryDTO.getEndDate() != null) {
            wrapper.le(Order::getCreateTime, orderQueryDTO.getEndDate().atTime(23, 59, 59));
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = orderRepository.selectPage(page, wrapper);

        // 转换为VO
        IPage<OrderListVO> voPage = orderPage.convert(order -> convertToListVO(order));

        // 收货人姓名查询（需要解析shippingAddress JSON字段）
        if (orderQueryDTO.getRecipientName() != null && !orderQueryDTO.getRecipientName().trim().isEmpty()) {
            String searchName = orderQueryDTO.getRecipientName().trim().toLowerCase();
            List<OrderListVO> filteredList = voPage.getRecords().stream()
                .filter(vo -> {
                    String recipientName = vo.getRecipientName();
                    return recipientName != null && recipientName.toLowerCase().contains(searchName);
                })
                .collect(Collectors.toList());
            
            // 重新构建分页结果
            Page<OrderListVO> filteredPage = new Page<>(orderQueryDTO.getPageNum(), orderQueryDTO.getPageSize());
            filteredPage.setRecords(filteredList);
            filteredPage.setTotal(filteredList.size());
            filteredPage.setCurrent(orderQueryDTO.getPageNum());
            filteredPage.setSize(orderQueryDTO.getPageSize());
            
            // 注意：由于在内存中过滤，总数可能不准确，这里使用过滤后的数量
            // 如果需要精确的分页，需要使用MySQL的JSON函数在数据库层面查询
            return filteredPage;
        }

        return voPage;
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);

        Order order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        return convertToDetailVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);

        Order order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有待付款订单可以确认
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getOrderStatus())) {
            throw new BusinessException(400, "只有待付款订单可以确认");
        }

        // 确认订单，更新为已付款未发货状态
        order.setOrderStatus(OrderStatus.PAID_UNSHIPPED);
        order.setPaymentStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        orderRepository.updateById(order);

        log.info("确认订单成功: orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo, String logisticsCompany, String logisticsNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);

        Order order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有已付款未发货订单可以发货
        if (!OrderStatus.PAID_UNSHIPPED.equals(order.getOrderStatus())) {
            throw new BusinessException(400, "只有已付款未发货订单可以发货");
        }

        // 更新订单状态为已发货
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now());
        orderRepository.updateById(order);

        // 创建或更新物流信息
        LambdaQueryWrapper<OrderLogistics> logisticsWrapper = new LambdaQueryWrapper<>();
        logisticsWrapper.eq(OrderLogistics::getOrderId, order.getId());
        OrderLogistics logistics = orderLogisticsRepository.selectOne(logisticsWrapper);

        if (logistics == null) {
            logistics = new OrderLogistics();
            logistics.setOrderId(order.getId());
        }

        logistics.setLogisticsCompany(logisticsCompany);
        logistics.setLogisticsNo(logisticsNo);
        logistics.setShippingTime(LocalDateTime.now());

        if (logistics.getId() == null) {
            orderLogisticsRepository.insert(logistics);
        } else {
            orderLogisticsRepository.updateById(logistics);
        }

        log.info("订单发货成功: orderNo={}, logisticsCompany={}, logisticsNo={}", orderNo, logisticsCompany, logisticsNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrderRemark(String orderNo, String remark) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);

        Order order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        order.setOrderRemark(remark);
        orderRepository.updateById(order);

        log.info("添加订单备注成功: orderNo={}", orderNo);
    }

    /**
     * 订单实体转列表VO
     */
    private OrderListVO convertToListVO(Order order) {
        OrderListVO vo = new OrderListVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderDate(order.getCreateTime());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getOrderStatus());
        vo.setStatusText(getStatusText(order.getOrderStatus()));

        // 解析收货地址JSON获取收货人信息
        ShippingAddressDTO shippingAddress = parseShippingAddressJson(order.getShippingAddress());
        if (shippingAddress != null) {
            vo.setRecipientName(shippingAddress.getName());
            vo.setRecipientAddress(shippingAddress.getFullAddress() != null ? 
                shippingAddress.getFullAddress() : shippingAddress.getAddress());
        } else {
            vo.setRecipientName("未知");
            vo.setRecipientAddress("未知");
        }
        
        // 构建商品描述（取前几个商品名称）
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        itemWrapper.last("LIMIT 3");
        List<OrderItem> items = orderItemRepository.selectList(itemWrapper);
        if (!items.isEmpty()) {
            String description = items.stream()
                .map(OrderItem::getProductName)
                .collect(Collectors.joining("、"));
            if (items.size() == 3) {
                // 查询总数量
                LambdaQueryWrapper<OrderItem> countWrapper = new LambdaQueryWrapper<>();
                countWrapper.eq(OrderItem::getOrderId, order.getId());
                long totalCount = orderItemRepository.selectCount(countWrapper);
                if (totalCount > 3) {
                    description += "等" + totalCount + "件商品";
                }
            }
            vo.setDescription(description);
        } else {
            vo.setDescription("商品信息（待实现）");
        }

        // 查询物流信息
        LambdaQueryWrapper<OrderLogistics> logisticsWrapper = new LambdaQueryWrapper<>();
        logisticsWrapper.eq(OrderLogistics::getOrderId, order.getId());
        OrderLogistics logistics = orderLogisticsRepository.selectOne(logisticsWrapper);

        if (logistics != null) {
            OrderListVO.LogisticsInfo logisticsInfo = new OrderListVO.LogisticsInfo();
            if (logistics.getShippingTime() != null) {
                logisticsInfo.setShipDate(logistics.getShippingTime().toLocalDate().toString());
                logisticsInfo.setShipTime(logistics.getShippingTime().toLocalTime().toString());
            }
            logisticsInfo.setCarrier(logistics.getLogisticsCompany());
            logisticsInfo.setTrackingNo(logistics.getLogisticsNo());
            vo.setLogistics(logisticsInfo);
        }

        return vo;
    }

    /**
     * 订单实体转详情VO
     */
    private OrderDetailVO convertToDetailVO(Order order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderDate(order.getCreateTime());
        vo.setStatus(order.getOrderStatus());
        vo.setStatusText(getStatusText(order.getOrderStatus()));
        vo.setOrderNotes(order.getOrderRemark());

        // 查询订单商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemRepository.selectList(itemWrapper);

        List<OrderDetailVO.OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderDetailVO.OrderItemVO itemVO = new OrderDetailVO.OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setProductCode(item.getProductCode());
            itemVO.setName(item.getProductName());
            itemVO.setImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotal(item.getSubtotal());
            return itemVO;
        }).collect(Collectors.toList());

        vo.setItems(itemVOs);
        vo.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
        vo.setTotalProductAmount(items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setShippingFee(order.getShippingFee());
        vo.setTotalAmount(order.getActualAmount());

        // 解析收货地址JSON并设置收货人信息
        ShippingAddressDTO shippingAddress = parseShippingAddressJson(order.getShippingAddress());
        OrderDetailVO.RecipientInfo recipientInfo = new OrderDetailVO.RecipientInfo();
        if (shippingAddress != null) {
            recipientInfo.setName(shippingAddress.getName());
            recipientInfo.setPhone(shippingAddress.getMobile() != null ? 
                shippingAddress.getMobile() : shippingAddress.getPhone());
            recipientInfo.setEmail(""); // TODO: 从用户表获取邮箱
            recipientInfo.setRegion(shippingAddress.getProvince() + "-" + 
                shippingAddress.getCity() + "-" + shippingAddress.getDistrict());
            recipientInfo.setZipCode(shippingAddress.getZipCode());
            recipientInfo.setAddress(shippingAddress.getFullAddress() != null ? 
                shippingAddress.getFullAddress() : shippingAddress.getAddress());
        } else {
            recipientInfo.setName("未知");
            recipientInfo.setPhone("");
            recipientInfo.setEmail("");
            recipientInfo.setRegion("");
            recipientInfo.setAddress("未知");
        }
        recipientInfo.setShippingMethod("包邮订单"); // TODO: 从订单或配送方式表获取
        recipientInfo.setWeight(items.stream()
            .map(OrderItem::getWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .multiply(BigDecimal.valueOf(1000))); // 转换为克
        recipientInfo.setDeliveryTime(order.getDeliveryTime());
        recipientInfo.setPaymentMethod(order.getPaymentMethod() != null ? 
            getPaymentMethodText(order.getPaymentMethod()) : "未知");
        recipientInfo.setPaymentCurrency("人民币");
        vo.setRecipientInfo(recipientInfo);

        // TODO: 查询订单历史记录
        vo.setOrderHistory(new ArrayList<>());

        return vo;
    }

    /**
     * 将状态字符串或数字字符串转换为Integer
     */
    private Integer convertStatusToInt(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        
        // 先尝试作为数字解析
        try {
            return Integer.parseInt(statusStr.trim());
        } catch (NumberFormatException e) {
            // 如果不是数字，尝试作为状态字符串解析
            return convertStatusStringToInt(statusStr.trim());
        }
    }

    /**
     * 将前端状态字符串转换为后端状态数字
     */
    private Integer convertStatusStringToInt(String statusStr) {
        switch (statusStr) {
            case "pending_payment":
                return OrderStatus.PENDING_PAYMENT;
            case "paid_not_shipped":
                return OrderStatus.PAID_UNSHIPPED;
            case "shipped":
                return OrderStatus.SHIPPED;
            case "completed":
                return OrderStatus.COMPLETED;
            case "cancelled":
                return OrderStatus.CANCELLED;
            case "refunded":
                return OrderStatus.REFUNDED;
            case "returned":
                return OrderStatus.RETURNED;
            default:
                return null;
        }
    }

    /**
     * 解析收货地址JSON
     */
    private ShippingAddressDTO parseShippingAddressJson(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, ShippingAddressDTO.class);
        } catch (Exception e) {
            log.error("解析收货地址JSON失败: {}", json, e);
            return null;
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
            case "PRE_DEPOSIT":
                return "预存款";
            case "OFFLINE":
                return "线下支付";
            default:
                return paymentMethod;
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "等待付款";
            case 1:
                return "已付款未发货";
            case 2:
                return "已发货";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            case 5:
                return "已退款";
            case 6:
                return "已退货";
            default:
                return "未知";
        }
    }
}

