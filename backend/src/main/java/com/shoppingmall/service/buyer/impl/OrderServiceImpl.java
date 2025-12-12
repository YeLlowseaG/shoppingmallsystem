package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.CreateOrderDTO;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.dto.ShippingAddressDTO;
import com.shoppingmall.common.constant.UserLevel;
import com.shoppingmall.entity.*;
import com.shoppingmall.repository.cart.CartRepository;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderLogisticsRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.product.ProductPriceRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.user.UserAddressRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.buyer.OrderService;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Slf4j
@Service("buyerOrderServiceImpl")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderLogisticsRepository orderLogisticsRepository;
    private final UserAddressRepository userAddressRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, CreateOrderDTO createOrderDTO) {
        // 1. 验证收货地址
        UserAddress address = userAddressRepository.selectById(createOrderDTO.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(400, "收货地址不存在或无权使用");
        }

        // 2. 获取订单商品列表
        List<CreateOrderDTO.OrderItemDTO> orderItems = new ArrayList<>();
        if (createOrderDTO.getCartIds() != null && !createOrderDTO.getCartIds().isEmpty()) {
            // 从购物车获取商品
            LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
            cartWrapper.eq(Cart::getUserId, userId);
            cartWrapper.in(Cart::getId, createOrderDTO.getCartIds());
            List<Cart> carts = cartRepository.selectList(cartWrapper);
            
            if (carts.isEmpty()) {
                throw new BusinessException(400, "购物车商品不存在");
            }

            for (Cart cart : carts) {
                CreateOrderDTO.OrderItemDTO item = new CreateOrderDTO.OrderItemDTO();
                item.setProductId(cart.getProductId());
                item.setQuantity(cart.getQuantity());
                orderItems.add(item);
            }
        } else if (createOrderDTO.getItems() != null && !createOrderDTO.getItems().isEmpty()) {
            // 直接购买
            orderItems = createOrderDTO.getItems();
        } else {
            throw new BusinessException(400, "订单商品不能为空");
        }

        // 3. 查询用户等级
        User user = userRepository.selectById(userId);
        Integer userLevel = (user != null && user.getUserLevel() != null) ? user.getUserLevel() : UserLevel.NORMAL;
        String userLevelName = convertUserLevelToString(userLevel);

        // 4. 验证库存和计算价格
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItemList = new ArrayList<>();
        
        for (CreateOrderDTO.OrderItemDTO itemDTO : orderItems) {
            // 查询商品信息
            Product product = productRepository.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException(404, "商品不存在: productId=" + itemDTO.getProductId());
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(400, "商品已下架: " + product.getProductName());
            }
            
            // 验证库存
            if (product.getStock() == null || product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException(400, "商品库存不足: " + product.getProductName());
            }
            
            // 查询价格（根据用户等级）
            BigDecimal salesPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
            BigDecimal memberPrice = getPriceByUserLevel(itemDTO.getProductId(), userLevelName, itemDTO.getQuantity());
            if (memberPrice == null) {
                memberPrice = salesPrice != null ? salesPrice : product.getBasePrice();
            }
            if (memberPrice == null) {
                throw new BusinessException(400, "商品价格未设置: " + product.getProductName());
            }
            
            // 使用会员价计算订单金额
            BigDecimal itemSubtotal = memberPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);
            
            // 创建订单商品快照
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductCode(product.getProductCode());
            orderItem.setProductImage(product.getMainImage() != null && !product.getMainImage().trim().isEmpty() 
                ? product.getMainImage() : "");
            orderItem.setPrice(memberPrice);
            orderItem.setSubtotal(itemSubtotal);
            orderItem.setWeight(product.getWeight() != null ? BigDecimal.valueOf(product.getWeight()) : BigDecimal.ZERO);
            
            orderItemList.add(orderItem);
        }

        // 5. 生成订单号
        String orderNo = generateOrderNo();

        // 6. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setPaymentMethod(createOrderDTO.getPaymentMethod());
        order.setDeliveryDate(createOrderDTO.getDeliveryDate());
        order.setDeliveryTime(createOrderDTO.getDeliveryTime());
        order.setOrderRemark(createOrderDTO.getOrderRemark());

        // 构建收货地址JSON
        String shippingAddressJson = buildShippingAddressJson(address);
        order.setShippingAddress(shippingAddressJson);

        // 设置订单金额
        BigDecimal shippingFee = BigDecimal.ZERO; // 运费暂时为0，后续根据配送方式计算
        BigDecimal tax = BigDecimal.ZERO; // 税金为0
        order.setTotalAmount(totalAmount);
        order.setShippingFee(shippingFee);
        order.setTax(tax);
        order.setActualAmount(totalAmount.add(shippingFee).add(tax));

        orderRepository.insert(order);

        // 7. 创建订单商品
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
            orderItemRepository.insert(orderItem);
        }

        // 7. 从购物车删除已下单的商品
        if (createOrderDTO.getCartIds() != null && !createOrderDTO.getCartIds().isEmpty()) {
            cartRepository.deleteBatchIds(createOrderDTO.getCartIds());
        }

        log.info("创建订单成功: orderNo={}, userId={}", orderNo, userId);
        return orderNo;
    }

    @Override
    public IPage<OrderListVO> getOrderList(Long userId, OrderQueryDTO orderQueryDTO) {
        Page<Order> page = new Page<>(orderQueryDTO.getPageNum(), orderQueryDTO.getPageSize());
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        
        // 订单号查询
        if (orderQueryDTO.getOrderNo() != null && !orderQueryDTO.getOrderNo().trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, orderQueryDTO.getOrderNo());
        }
        
        // 订单状态筛选（前端传入的是字符串，需要转换为数字）
        if (orderQueryDTO.getOrderStatus() != null && !orderQueryDTO.getOrderStatus().trim().isEmpty()) {
            Integer status = convertStatusStringToInt(orderQueryDTO.getOrderStatus());
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
        
        return voPage;
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderNo, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);
        
        Order order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        return convertToDetailVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);
        
        Order order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        // 只有待付款订单可以取消
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getOrderStatus())) {
            throw new BusinessException(400, "只有待付款订单可以取消");
        }
        
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.updateById(order);
        
        log.info("取消订单成功: orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(String orderNo, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);
        
        Order order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        // 只有已发货订单可以确认收货
        if (!OrderStatus.SHIPPED.equals(order.getOrderStatus())) {
            throw new BusinessException(400, "只有已发货订单可以确认收货");
        }
        
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        orderRepository.updateById(order);
        
        log.info("确认收货成功: orderNo={}, userId={}", orderNo, userId);
    }

    /**
     * 生成订单号（格式：yyyyMMddHHmmss + 6位随机数）
     */
    private String generateOrderNo() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 1000000);
        return dateTime + String.format("%06d", random);
    }

    /**
     * 构建收货地址JSON
     */
    private String buildShippingAddressJson(UserAddress address) {
        try {
            ShippingAddressDTO dto = new ShippingAddressDTO();
            dto.setName(address.getReceiverName());
            dto.setPhone(address.getReceiverPhone());
            dto.setMobile(address.getReceiverMobile());
            dto.setProvince(address.getProvince());
            dto.setCity(address.getCity());
            dto.setDistrict(address.getDistrict());
            dto.setAddress(address.getDetailAddress());
            dto.setZipCode(address.getZipCode());
            
            // 构建完整地址
            StringBuilder fullAddress = new StringBuilder();
            if (address.getProvince() != null) {
                fullAddress.append(address.getProvince());
            }
            if (address.getCity() != null) {
                fullAddress.append(address.getCity());
            }
            if (address.getDistrict() != null) {
                fullAddress.append(address.getDistrict());
            }
            if (address.getDetailAddress() != null) {
                fullAddress.append(address.getDetailAddress());
            }
            dto.setFullAddress(fullAddress.toString());
            
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("构建收货地址JSON失败", e);
            throw new BusinessException(500, "构建收货地址失败");
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
     * 根据用户等级获取价格
     * 
     * @param productId 商品ID
     * @param userLevelName 用户等级名称（普通/VIP/金牌）
     * @param quantity 数量（用于阶梯价格）
     * @return 价格
     */
    private BigDecimal getPriceByUserLevel(Long productId, String userLevelName, Integer quantity) {
        LambdaQueryWrapper<ProductPrice> priceWrapper = new LambdaQueryWrapper<>();
        priceWrapper.eq(ProductPrice::getProductId, productId);
        priceWrapper.eq(ProductPrice::getUserLevel, userLevelName);
        
        // 如果有数量，查询符合数量范围的阶梯价格
        if (quantity != null && quantity > 0) {
            priceWrapper.le(ProductPrice::getMinQuantity, quantity);
            priceWrapper.and(w -> w.isNull(ProductPrice::getMaxQuantity)
                    .or().ge(ProductPrice::getMaxQuantity, quantity));
        }
        
        priceWrapper.orderByDesc(ProductPrice::getMinQuantity); // 按最小数量降序，优先匹配高阶梯价格
        priceWrapper.last("LIMIT 1");
        
        ProductPrice productPrice = productPriceRepository.selectOne(priceWrapper);
        return productPrice != null ? productPrice.getPrice() : null;
    }

    /**
     * 将用户等级数字转换为字符串
     * 
     * @param userLevel 用户等级（0-普通，1-VIP，2-金牌）
     * @return 用户等级名称
     */
    private String convertUserLevelToString(Integer userLevel) {
        if (userLevel == null) {
            return "普通";
        }
        switch (userLevel) {
            case 0:
                return "普通";
            case 1:
                return "VIP";
            case 2:
                return "金牌";
            default:
                return "普通";
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

