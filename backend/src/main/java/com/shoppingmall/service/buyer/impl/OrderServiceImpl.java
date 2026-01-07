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
import com.shoppingmall.repository.order.OrderRefundRepository;
import com.shoppingmall.repository.order.OrderRefundItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.product.ProductPriceRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.repository.user.UserAddressRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.service.buyer.OrderService;
import com.shoppingmall.service.buyer.DepositService;
import com.shoppingmall.payment.service.PaymentGatewayService;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.vo.MemberLevelVO;
import com.shoppingmall.dto.OrderPaymentDTO;
import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.common.util.EncryptUtil;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import com.shoppingmall.vo.OrderStatisticsVO;
import com.shoppingmall.vo.OrderRefundVO;
import com.shoppingmall.entity.OrderRefund;
import com.shoppingmall.entity.OrderRefundItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.shoppingmall.dto.ShippingFeeCalculateDTO;

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
    private final OrderRefundRepository orderRefundRepository;
    private final OrderRefundItemRepository orderRefundItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductStockRepository productStockRepository;
    private final ProductSkuRepository productSkuRepository;
    private final UserRepository userRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final DepositService depositService;
    private final PaymentGatewayService paymentGatewayService;
    private final MemberLevelService memberLevelService;
    private final ObjectMapper objectMapper;
    private final com.shoppingmall.service.logistics.ShippingService shippingService;
    private final com.shoppingmall.notification.service.NotificationService notificationService;

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
                item.setSkuId(cart.getSkuId());
                orderItems.add(item);
            }
        } else if (createOrderDTO.getItems() != null && !createOrderDTO.getItems().isEmpty()) {
            // 直接购买
            orderItems = createOrderDTO.getItems();
        } else {
            throw new BusinessException(400, "订单商品不能为空");
        }

        // 3. 验证库存和计算价格
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
            
            // 查询价格信息
            // 判断是否有SKU
            ProductSku sku = null;
            if (itemDTO.getSkuId() != null) {
                // 从购物车创建订单时，需要查询购物车的SKU信息
                if (createOrderDTO.getCartIds() != null && !createOrderDTO.getCartIds().isEmpty()) {
                    LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
                    cartWrapper.eq(Cart::getUserId, userId);
                    cartWrapper.eq(Cart::getProductId, itemDTO.getProductId());
                    cartWrapper.eq(Cart::getSkuId, itemDTO.getSkuId());
                    Cart cart = cartRepository.selectOne(cartWrapper);
                    if (cart != null && cart.getSkuId() != null) {
                        sku = productSkuRepository.selectById(cart.getSkuId());
                    }
                } else {
                    // 直接购买时，需要查询SKU信息
                    LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
                    skuWrapper.eq(ProductSku::getId, itemDTO.getSkuId());
                    skuWrapper.eq(ProductSku::getProductId, itemDTO.getProductId());
                    sku = productSkuRepository.selectOne(skuWrapper);
                }
            }
            
            BigDecimal salesPrice;
            BigDecimal memberPrice;
            BigDecimal weight;
            
            // 设置商品重量（根据是否启用多个规格SKU来决定使用哪个表的重量）
            // 如果商品没有启用多个规格sku（enableSpec == 0），则获取商品表重量字段的数据
            // 如果启用sku（enableSpec == 1），则读取product_sku表的重量字段来计算运费
            boolean enableSpec = product.getEnableSpec() != null && product.getEnableSpec() == 1;
            if (enableSpec && sku != null) {
                // 启用SKU，使用SKU表的重量
                if (sku.getWeight() != null) {
                    weight = sku.getWeight();
                } else {
                    // SKU没有重量，使用商品表的重量作为兜底
                    weight = product.getWeight() != null ? BigDecimal.valueOf(product.getWeight().longValue()) : BigDecimal.ZERO;
                }
            } else {
                // 没有启用SKU，使用商品表的重量
                weight = product.getWeight() != null ? BigDecimal.valueOf(product.getWeight().longValue()) : BigDecimal.ZERO;
            }
            
            if (sku != null) {
                // 有SKU，使用SKU的价格
                salesPrice = sku.getPrice();
                if (salesPrice == null) {
                    throw new BusinessException(400, "SKU价格未设置: productId=" + itemDTO.getProductId() + ", skuId=" + itemDTO.getSkuId());
                }
                
                // 计算会员价格：优先使用SKU配置的会员价
                memberPrice = calculateMemberPriceForSku(sku, salesPrice, userId);
            } else {
                // 没有SKU，使用商品的价格
                salesPrice = product.getBasePrice();
                if (salesPrice == null) {
                    throw new BusinessException(400, "商品价格未设置: " + product.getProductName());
                }
                
                // 计算会员价格：优先使用商品配置的会员价
                memberPrice = calculateMemberPriceForProduct(product, salesPrice, userId);
            }
            
            // 保留两位小数
            memberPrice = memberPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
            
            // 使用会员价计算订单金额
            BigDecimal itemSubtotal = memberPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            // 保留两位小数
            itemSubtotal = itemSubtotal.setScale(2, BigDecimal.ROUND_HALF_UP);
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
            orderItem.setWeight(weight);
            // 保存运费模板ID（历史快照）
            orderItem.setShippingTemplateId(product.getShippingTemplateId());

            // 设置SKU信息
            if (sku != null) {
                orderItem.setSkuId(sku.getId());
                orderItem.setSpecCombination(sku.getSpecCombination());
            }
            
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
        BigDecimal shippingFee = calculateOrderShippingFee(orderItemList, address); // 根据商品运费模板计算运费
        BigDecimal tax = BigDecimal.ZERO; // 税金为0
        order.setTotalAmount(totalAmount);
        order.setShippingFee(shippingFee);
        order.setTax(tax);
        order.setActualAmount(totalAmount.add(shippingFee).add(tax));
        
        log.info("订单金额计算: 商品总金额={}, 运费={}, 税金={}, 实付金额={}", 
            totalAmount, shippingFee, tax, order.getActualAmount());

        orderRepository.insert(order);

        // 7. 创建订单商品
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
            orderItemRepository.insert(orderItem);
        }

        // 8. 扣减库存
        for (CreateOrderDTO.OrderItemDTO itemDTO : orderItems) {
            // 扣减product表的库存
            Product product = productRepository.selectById(itemDTO.getProductId());
            if (product != null && product.getStock() != null) {
                int newStock = product.getStock() - itemDTO.getQuantity();
                if (newStock < 0) {
                    throw new BusinessException(400, "库存不足: " + product.getProductName());
                }
                product.setStock(newStock);
                productRepository.updateById(product);
            }
            
            // 扣减product_stock表的库存
            LambdaQueryWrapper<ProductStock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(ProductStock::getProductId, itemDTO.getProductId());
            ProductStock productStock = productStockRepository.selectOne(stockWrapper);
            
            if (productStock != null) {
                // 增加锁定库存
                int newLockedStock = (productStock.getLockedStock() != null ? productStock.getLockedStock() : 0) + itemDTO.getQuantity();
                productStock.setLockedStock(newLockedStock);
                
                // 减少可用库存
                int newAvailableStock = (productStock.getAvailableStock() != null ? productStock.getAvailableStock() : 0) - itemDTO.getQuantity();
                if (newAvailableStock < 0) {
                    throw new BusinessException(400, "可用库存不足: " + product.getProductName());
                }
                productStock.setAvailableStock(newAvailableStock);
                productStockRepository.updateById(productStock);
            } else {
                // 如果product_stock记录不存在，创建新记录
                productStock = new ProductStock();
                productStock.setProductId(itemDTO.getProductId());
                productStock.setTotalStock(product != null && product.getStock() != null ? product.getStock() : 0);
                productStock.setLockedStock(itemDTO.getQuantity());
                productStock.setAvailableStock((productStock.getTotalStock() - productStock.getLockedStock()));
                productStock.setWarningThreshold(10);
                productStockRepository.insert(productStock);
            }
        }

        // 9. 从购物车删除已下单的商品
        if (createOrderDTO.getCartIds() != null && !createOrderDTO.getCartIds().isEmpty()) {
            cartRepository.deleteBatchIds(createOrderDTO.getCartIds());
        }

        log.info("创建订单成功: orderNo={}, userId={}", orderNo, userId);

        // 10. 发送订单创建通知到企业微信
        notificationService.sendOrderCreatedNotification(orderNo);

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
        
        // 收货人信息查询（需要在内存中过滤，因为信息存储在JSON字段中）
        final String searchRecipientName = (orderQueryDTO.getRecipientName() != null && !orderQueryDTO.getRecipientName().trim().isEmpty()) 
            ? orderQueryDTO.getRecipientName().trim().toLowerCase() : null;
        final String searchContactPhone = (orderQueryDTO.getContactPhone() != null && !orderQueryDTO.getContactPhone().trim().isEmpty()) 
            ? orderQueryDTO.getContactPhone().trim() : null;
        final String searchContactMobile = (orderQueryDTO.getContactMobile() != null && !orderQueryDTO.getContactMobile().trim().isEmpty()) 
            ? orderQueryDTO.getContactMobile().trim() : null;
        final String searchRecipientAddress = (orderQueryDTO.getRecipientAddress() != null && !orderQueryDTO.getRecipientAddress().trim().isEmpty()) 
            ? orderQueryDTO.getRecipientAddress().trim().toLowerCase() : null;
        
        boolean needFilter = searchRecipientName != null || searchContactPhone != null 
            || searchContactMobile != null || searchRecipientAddress != null;
        
        // 如果需要过滤收货人信息，先查询所有符合条件的订单，然后进行内存过滤
        if (needFilter) {
            // 查询所有符合条件的订单（不进行分页）
            List<Order> allOrders = orderRepository.selectList(wrapper);
            
            // 转换为VO并进行内存过滤
            List<OrderListVO> filteredRecords = allOrders.stream()
                .map(order -> convertToListVO(order))
                .filter(vo -> {
                    boolean match = true;
                    
                    if (searchRecipientName != null) {
                        String name = vo.getRecipientName() != null ? vo.getRecipientName().toLowerCase() : "";
                        match = match && name.contains(searchRecipientName);
                    }
                    
                    if (searchRecipientAddress != null) {
                        String address = vo.getRecipientAddress() != null ? vo.getRecipientAddress().toLowerCase() : "";
                        match = match && address.contains(searchRecipientAddress);
                    }
                    
                    // 对于联系电话和联系手机，直接从VO中获取（已在convertToListVO中设置）
                    if (searchContactPhone != null) {
                        String phone = vo.getContactPhone() != null ? vo.getContactPhone() : "";
                        match = match && phone.contains(searchContactPhone);
                    }
                    if (searchContactMobile != null) {
                        String mobile = vo.getContactMobile() != null ? vo.getContactMobile() : "";
                        match = match && mobile.contains(searchContactMobile);
                    }
                    
                    return match;
                })
                .collect(Collectors.toList());
            
            // 进行内存分页
            int total = filteredRecords.size();
            int pageNum = orderQueryDTO.getPageNum();
            int pageSize = orderQueryDTO.getPageSize();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            
            List<OrderListVO> pagedRecords = start < total ? filteredRecords.subList(start, end) : new ArrayList<>();
            
            // 创建分页结果
            Page<OrderListVO> resultPage = new Page<>(pageNum, pageSize);
            resultPage.setRecords(pagedRecords);
            resultPage.setTotal(total);
            resultPage.setPages((int) Math.ceil((double) total / pageSize));
            
            return resultPage;
        }
        
        // 如果不需要过滤收货人信息，直接进行数据库分页查询
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
        
        // 恢复库存
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);
        
        for (OrderItem orderItem : orderItems) {
            // 恢复product表的库存
            Product product = productRepository.selectById(orderItem.getProductId());
            if (product != null && product.getStock() != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                productRepository.updateById(product);
            }
            
            // 恢复product_stock表的库存
            LambdaQueryWrapper<ProductStock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(ProductStock::getProductId, orderItem.getProductId());
            ProductStock productStock = productStockRepository.selectOne(stockWrapper);
            
            if (productStock != null) {
                // 减少锁定库存
                int newLockedStock = (productStock.getLockedStock() != null ? productStock.getLockedStock() : 0) - orderItem.getQuantity();
                if (newLockedStock < 0) {
                    newLockedStock = 0;
                }
                productStock.setLockedStock(newLockedStock);
                
                // 增加可用库存
                int newAvailableStock = (productStock.getAvailableStock() != null ? productStock.getAvailableStock() : 0) + orderItem.getQuantity();
                productStock.setAvailableStock(newAvailableStock);
                productStockRepository.updateById(productStock);
            }
        }
        
        // 如果订单之前是已完成状态，需要扣减销量
        if (OrderStatus.COMPLETED.equals(order.getOrderStatus())) {
            updateProductSalesCount(order.getId(), false);
        }
        
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.updateById(order);

        // 联动取消支付记录状态
        cancelPaymentRecords(order.getId());

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
        
        // 增加商品销量（订单完成时）
        updateProductSalesCount(order.getId(), true);
        
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
     * 格式化规格组合文本
     * @param specCombination 规格组合JSON字符串，格式如：{"颜色":"蓝色","尺码":"XXL"}
     * @return 格式化后的规格文本，格式如：颜色:蓝色 / 尺码:XXL
     */
    private String formatSpecText(String specCombination) {
        if (specCombination == null || specCombination.trim().isEmpty()) {
            return null;
        }
        try {
            Map<String, String> specMap = objectMapper.readValue(
                specCombination,
                new TypeReference<Map<String, String>>() {}
            );
            if (specMap == null || specMap.isEmpty()) {
                return null;
            }
            return specMap.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(" / "));
        } catch (Exception e) {
            log.warn("解析规格组合失败: specCombination={}", specCombination, e);
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
            vo.setContactPhone(shippingAddress.getPhone());
            vo.setContactMobile(shippingAddress.getMobile());
        } else {
            vo.setRecipientName("未知");
            vo.setRecipientAddress("未知");
            vo.setContactPhone("");
            vo.setContactMobile("");
        }
        
        // 构建商品描述（取前几个商品名称，包含规格信息）
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        itemWrapper.last("LIMIT 3");
        List<OrderItem> items = orderItemRepository.selectList(itemWrapper);
        if (!items.isEmpty()) {
            List<String> itemDescriptions = items.stream()
                .map(item -> {
                    String productName = item.getProductName();
                    String specText = formatSpecText(item.getSpecCombination());
                    if (specText != null && !specText.isEmpty()) {
                        return productName + " 规格：" + specText;
                    } else {
                        return productName;
                    }
                })
                .collect(Collectors.toList());
            
            String description = String.join("、", itemDescriptions);
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
            itemVO.setProductId(item.getProductId());
            itemVO.setProductCode(item.getProductCode());
            itemVO.setName(item.getProductName());
            itemVO.setImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotal(item.getSubtotal());
            itemVO.setSpecCombination(item.getSpecCombination());
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
            recipientInfo.setPhone(shippingAddress.getPhone());
            recipientInfo.setMobile(shippingAddress.getMobile());
            recipientInfo.setEmail(""); // 屏蔽邮箱字段
            recipientInfo.setRegion(shippingAddress.getProvince() + "-" + 
                shippingAddress.getCity() + "-" + shippingAddress.getDistrict());
            recipientInfo.setZipCode(shippingAddress.getZipCode());
            recipientInfo.setAddress(shippingAddress.getFullAddress() != null ? 
                shippingAddress.getFullAddress() : shippingAddress.getAddress());
        } else {
            recipientInfo.setName("未知");
            recipientInfo.setPhone("");
            recipientInfo.setMobile("");
            recipientInfo.setEmail("");
            recipientInfo.setRegion("");
            recipientInfo.setAddress("未知");
        }
        recipientInfo.setShippingMethod("包邮订单"); // TODO: 从订单或配送方式表获取
        // 计算商品总重量（OrderItem.weight 是单个商品重量（克），需要乘以数量）
        recipientInfo.setWeight(items.stream()
            .map(item -> item.getWeight().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)); // 总重量单位是克
        recipientInfo.setDeliveryTime(order.getDeliveryTime());
        
        // 获取支付方式：如果订单已支付，优先从支付记录中获取（更准确）
        String paymentMethod = null;
        if (PaymentStatus.PAID.equals(order.getPaymentStatus()) || PaymentStatus.PAYING.equals(order.getPaymentStatus())) {
            // 查询支付记录，获取实际使用的支付方式
            LambdaQueryWrapper<PaymentRecord> paymentWrapper = new LambdaQueryWrapper<>();
            paymentWrapper.eq(PaymentRecord::getOrderId, order.getId());
            paymentWrapper.orderByDesc(PaymentRecord::getCreateTime);
            paymentWrapper.last("LIMIT 1");
            PaymentRecord paymentRecord = paymentRecordRepository.selectOne(paymentWrapper);
            if (paymentRecord != null && paymentRecord.getPaymentMethod() != null) {
                paymentMethod = paymentRecord.getPaymentMethod();
            }
        }
        // 如果支付记录中没有，则使用订单表中的支付方式
        if (paymentMethod == null) {
            paymentMethod = order.getPaymentMethod();
        }
        recipientInfo.setPaymentMethod(paymentMethod != null ? 
            getPaymentMethodText(paymentMethod) : "未知");
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
     * 计算商品的会员价格
     * 优先使用商品配置的会员价，如果没有则根据会员等级折扣率计算
     * 注意：非会员不能享受会员价，直接返回原价
     * 
     * @param product 商品实体
     * @param salesPrice 销售价格
     * @param userId 用户ID
     * @return 会员价格（非会员返回原价）
     */
    private BigDecimal calculateMemberPriceForProduct(Product product, BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 首先检查用户是否是会员（大前提条件）
        User user = userRepository.selectById(userId);
        if (user == null || user.getIsMember() == null || user.getIsMember() != 1) {
            // 非会员不能享受会员价，直接返回原价
            return salesPrice;
        }

        // 如果是会员，检查商品是否启用了会员价且有配置会员价
        if (product.getEnableMemberPrice() != null && product.getEnableMemberPrice() == 1
                && product.getMemberPrice() != null && product.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
            return product.getMemberPrice();
        }

        // 否则根据会员等级折扣率计算
        return calculateMemberPriceByDiscount(salesPrice, userId);
    }

    /**
     * 计算SKU的会员价格
     * 优先使用SKU配置的会员价，如果没有则根据会员等级折扣率计算
     * 注意：非会员不能享受会员价，直接返回原价
     * 
     * @param sku SKU实体
     * @param salesPrice 销售价格
     * @param userId 用户ID
     * @return 会员价格（非会员返回原价）
     */
    private BigDecimal calculateMemberPriceForSku(ProductSku sku, BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 首先检查用户是否是会员（大前提条件）
        User user = userRepository.selectById(userId);
        if (user == null || user.getIsMember() == null || user.getIsMember() != 1) {
            // 非会员不能享受会员价，直接返回原价
            return salesPrice;
        }

        // 如果是会员，检查SKU是否启用了会员价且有配置会员价
        if (sku.getEnableMemberPrice() != null && sku.getEnableMemberPrice() == 1
                && sku.getMemberPrice() != null && sku.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
            return sku.getMemberPrice();
        }

        // 否则根据会员等级折扣率计算
        return calculateMemberPriceByDiscount(salesPrice, userId);
    }

    /**
     * 根据会员等级折扣率计算会员价格
     * 
     * @param salesPrice 销售价格
     * @param userId 用户ID
     * @return 会员价格
     */
    private BigDecimal calculateMemberPriceByDiscount(BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        try {
            // 获取用户的会员等级
            User user = userRepository.selectById(userId);
            
            // 如果不是会员，返回原价
            if (user == null || user.getIsMember() == null || user.getIsMember() != 1) {
                return salesPrice;
            }

            // 获取所有启用的会员等级（按排序号排序）
            List<MemberLevelVO> memberLevels = memberLevelService.getAllEnabledMemberLevels();
            if (memberLevels == null || memberLevels.isEmpty()) {
                // 如果没有会员等级，返回原价
                return salesPrice;
            }

            // 查找用户的会员等级
            Long memberLevelId = user.getMemberLevelId();
            MemberLevelVO memberLevel = null;
            
            if (memberLevelId != null) {
                // 根据 member_level.id 查找
                for (MemberLevelVO level : memberLevels) {
                    if (level.getId() != null && level.getId().equals(memberLevelId)) {
                        memberLevel = level;
                        break;
                    }
                }
            }
            
            // 如果找不到匹配的等级，返回原价
            if (memberLevel == null) {
                return salesPrice;
            }
            
            BigDecimal discountRate = memberLevel.getDiscountRate();
            if (discountRate == null) {
                // 如果没有折扣率，返回原价
                return salesPrice;
            }

            // 计算会员价格：销售价格 * (折扣率 / 100.00)
            // 例如：100.00 * (95.00 / 100.00) = 95.00
            BigDecimal memberPrice = salesPrice.multiply(discountRate).divide(new BigDecimal("100.00"), 2, BigDecimal.ROUND_HALF_UP);
            return memberPrice;
        } catch (Exception e) {
            log.error("计算会员价格失败: userId={}, salesPrice={}", userId, salesPrice, e);
            // 计算失败时返回原价
            return salesPrice;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponseDTO payOrder(String orderNo, Long userId, OrderPaymentDTO paymentDTO) {
        // 1. 验证订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, orderNo);
        orderWrapper.eq(Order::getUserId, userId);
        Order order = orderRepository.selectOne(orderWrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getOrderStatus())) {
            throw new BusinessException(400, "订单状态不允许支付");
        }
        
        if (order.getPaymentStatus() != null && PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new BusinessException(400, "订单已支付");
        }
        
        // 2. 根据支付方式处理
        String paymentMethod = paymentDTO.getPaymentMethod();
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setInternalOrderNo(orderNo);
        
        if ("PRE_DEPOSIT".equals(paymentMethod)) {
            // 预存款支付
            // 2.1 验证支付密码
            User user = userRepository.selectById(userId);
            if (user == null) {
                throw new BusinessException(404, "用户不存在");
            }
            
            if (paymentDTO.getPaymentPassword() == null || paymentDTO.getPaymentPassword().isEmpty()) {
                throw new BusinessException(400, "支付密码不能为空");
            }
            
            // 验证支付密码
            // 如果用户没有设置过支付密码，则使用登录密码作为默认支付密码
            boolean passwordValid = false;
            if (user.getPaymentPassword() == null || user.getPaymentPassword().isEmpty()) {
                // 未设置过支付密码，使用登录密码验证
                passwordValid = EncryptUtil.bcryptMatches(paymentDTO.getPaymentPassword(), user.getPassword());
            } else {
                // 已设置过支付密码，使用支付密码验证
                passwordValid = EncryptUtil.bcryptMatches(paymentDTO.getPaymentPassword(), user.getPaymentPassword());
            }
            
            if (!passwordValid) {
                throw new BusinessException(400, "支付密码错误");
            }
            
            // 2.2 使用预存款支付
            depositService.depositPayment(userId, order.getId(), orderNo, order.getActualAmount());
            
            // 2.3 更新订单状态
            order.setPaymentStatus(PaymentStatus.PAID); // 已支付
            order.setOrderStatus(OrderStatus.PAID_UNSHIPPED);
            order.setPayTime(LocalDateTime.now());
            orderRepository.updateById(order);
            
            // 2.4 创建或更新支付记录（防止重复插入）
            String paymentNo = "DEPOSIT_" + orderNo;
            LambdaQueryWrapper<PaymentRecord> existingRecordWrapper = new LambdaQueryWrapper<>();
            existingRecordWrapper.eq(PaymentRecord::getPaymentNo, paymentNo);
            PaymentRecord existingRecord = paymentRecordRepository.selectOne(existingRecordWrapper);
            
            PaymentRecord paymentRecord;
            if (existingRecord != null) {
                // 如果已存在，更新记录
                paymentRecord = existingRecord;
                paymentRecord.setPaymentStatus(PaymentStatus.PAID); // 已支付
                paymentRecord.setPaymentTime(LocalDateTime.now());
                paymentRecordRepository.updateById(paymentRecord);
                log.info("更新支付记录: orderNo={}, paymentNo={}", orderNo, paymentNo);
            } else {
                // 如果不存在，创建新记录
                paymentRecord = new PaymentRecord();
                paymentRecord.setOrderId(order.getId());
                paymentRecord.setPaymentNo(paymentNo);
                paymentRecord.setPaymentMethod("PRE_DEPOSIT");
                paymentRecord.setAmount(order.getActualAmount());
                paymentRecord.setPaymentStatus(PaymentStatus.PAID); // 已支付
                paymentRecord.setPaymentTime(LocalDateTime.now());
                paymentRecordRepository.insert(paymentRecord);
                log.info("创建支付记录: orderNo={}, paymentNo={}", orderNo, paymentNo);
            }
            
            log.info("预存款支付成功: orderNo={}, userId={}, amount={}", orderNo, userId, order.getActualAmount());
            
            // 发送支付成功通知到企业微信
            notificationService.sendPaymentSuccessNotification(orderNo);
            
        } else if ("ALIPAY".equals(paymentMethod) || "WECHAT".equals(paymentMethod)) {
            // 支付宝/微信支付
            // 2.1 创建支付订单
            log.info("创建支付订单: orderNo={}, 商品总金额={}, 运费={}, 税金={}, 实付金额={}", 
                orderNo, order.getTotalAmount(), order.getShippingFee(), order.getTax(), order.getActualAmount());
            
            PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
            paymentRequest.setInternalOrderNo(orderNo);
            paymentRequest.setAmount(order.getActualAmount());
            paymentRequest.setPaymentMethod("alipay".equals(paymentMethod.toLowerCase()) ? "alipay" : "wechat");
            paymentRequest.setCurrency("CNY");
            paymentRequest.setDescription("订单支付：" + orderNo);
            paymentRequest.setUserId(userId);
            
            log.info("推送给第三方支付的金额: {}", paymentRequest.getAmount());
            
            // 设置前端地址（用于构建return_url）
            // 如果PaymentRequestDTO中有frontendUrl字段，可以从paymentDTO中获取
            // 否则在AlipayPayStrategy中会从notifyUrl提取
            if (paymentDTO.getFrontendUrl() != null && !paymentDTO.getFrontendUrl().isEmpty()) {
                paymentRequest.setFrontendUrl(paymentDTO.getFrontendUrl());
            }
            
            PaymentResponseDTO paymentResponse = paymentGatewayService.pay(paymentRequest);
            
            // 2.2 创建或更新支付记录（支付中状态，防止重复插入）
            String paymentNo = "PAY_" + orderNo;
            LambdaQueryWrapper<PaymentRecord> existingRecordWrapper = new LambdaQueryWrapper<>();
            existingRecordWrapper.eq(PaymentRecord::getPaymentNo, paymentNo);
            PaymentRecord existingRecord = paymentRecordRepository.selectOne(existingRecordWrapper);
            
            PaymentRecord paymentRecord;
            if (existingRecord != null) {
                // 如果已存在，更新记录（保持支付中状态）
                paymentRecord = existingRecord;
                paymentRecord.setPaymentMethod(paymentMethod);
                paymentRecord.setAmount(order.getActualAmount());
                paymentRecord.setPaymentStatus(PaymentStatus.PAYING); // 支付中（用户已发起支付）
                paymentRecordRepository.updateById(paymentRecord);
                log.info("更新支付记录: orderNo={}, paymentMethod={}, paymentNo={}", 
                        orderNo, paymentMethod, paymentNo);
            } else {
                // 如果不存在，创建新记录
                paymentRecord = new PaymentRecord();
                paymentRecord.setOrderId(order.getId());
                paymentRecord.setPaymentNo(paymentNo);
                paymentRecord.setPaymentMethod(paymentMethod);
                paymentRecord.setAmount(order.getActualAmount());
                paymentRecord.setPaymentStatus(PaymentStatus.PAYING); // 支付中（用户已发起支付）
                paymentRecordRepository.insert(paymentRecord);
                log.info("创建支付记录: orderNo={}, paymentMethod={}, paymentNo={}", 
                        orderNo, paymentMethod, paymentNo);
            }
            
            log.info("创建支付订单成功: orderNo={}, paymentMethod={}, paymentNo={}", 
                    orderNo, paymentMethod, paymentRecord.getPaymentNo());
            
            return paymentResponse;
        } else {
            throw new BusinessException(400, "不支持的支付方式");
        }
        
        return response;
    }

    @Override
    public OrderStatisticsVO getOrderStatistics(Long userId) {
        OrderStatisticsVO statistics = new OrderStatisticsVO();

        // 统计未付款订单数量（status = 0）
        LambdaQueryWrapper<Order> unpaidWrapper = new LambdaQueryWrapper<>();
        unpaidWrapper.eq(Order::getUserId, userId)
                .eq(Order::getOrderStatus, OrderStatus.PENDING_PAYMENT);
        Long unpaidCount = orderRepository.selectCount(unpaidWrapper);
        statistics.setUnpaidOrderCount(unpaidCount);

        // 统计已发货订单数量（status = 2）
        LambdaQueryWrapper<Order> shippedWrapper = new LambdaQueryWrapper<>();
        shippedWrapper.eq(Order::getUserId, userId)
                .eq(Order::getOrderStatus, OrderStatus.SHIPPED);
        Long shippedCount = orderRepository.selectCount(shippedWrapper);
        statistics.setShippedOrderCount(shippedCount);

        // 统计已作废订单数量（status = 4）
        LambdaQueryWrapper<Order> cancelledWrapper = new LambdaQueryWrapper<>();
        cancelledWrapper.eq(Order::getUserId, userId)
                .eq(Order::getOrderStatus, OrderStatus.CANCELLED);
        Long cancelledCount = orderRepository.selectCount(cancelledWrapper);
        statistics.setCancelledOrderCount(cancelledCount);

        return statistics;
    }

    /**
     * 更新商品销量
     * 
     * @param orderId 订单ID
     * @param increase true-增加销量，false-扣减销量
     */
    private void updateProductSalesCount(Long orderId, boolean increase) {
        // 查询订单商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);
        
        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.selectById(orderItem.getProductId());
            if (product != null) {
                int currentSalesCount = product.getSalesCount() != null ? product.getSalesCount() : 0;
                int quantity = orderItem.getQuantity() != null ? orderItem.getQuantity() : 0;
                
                if (increase) {
                    // 增加销量
                    product.setSalesCount(currentSalesCount + quantity);
                } else {
                    // 扣减销量（确保不为负数）
                    int newSalesCount = currentSalesCount - quantity;
                    product.setSalesCount(Math.max(0, newSalesCount));
                }
                
                productRepository.updateById(product);
                log.info("更新商品销量: productId={}, quantity={}, increase={}, newSalesCount={}", 
                        orderItem.getProductId(), quantity, increase, product.getSalesCount());
            }
        }
    }

    @Override
    public List<OrderRefundVO> getOrderRefundList(String orderNo, Long userId) {
        // 验证订单存在且属于当前用户
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, orderNo);
        orderWrapper.eq(Order::getUserId, userId);
        Order order = orderRepository.selectOne(orderWrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在或无权访问");
        }

        // 查询该订单的所有退款记录
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getOrderId, order.getId());
        wrapper.orderByDesc(OrderRefund::getCreateTime);
        List<OrderRefund> refunds = orderRefundRepository.selectList(wrapper);

        return refunds.stream().map(this::convertRefundToVO).collect(Collectors.toList());
    }

    /**
     * 转换为退款VO
     */
    private OrderRefundVO convertRefundToVO(OrderRefund orderRefund) {
        OrderRefundVO vo = new OrderRefundVO();
        vo.setId(orderRefund.getId());
        vo.setRefundNo(orderRefund.getRefundNo());
        vo.setOrderId(orderRefund.getOrderId());
        vo.setOrderNo(orderRefund.getOrderNo());
        vo.setUserId(orderRefund.getUserId());
        vo.setRefundAmount(orderRefund.getRefundAmount());
        vo.setRefundReason(orderRefund.getRefundReason());
        vo.setRefundStatus(orderRefund.getRefundStatus());
        vo.setRefundStatusText(getRefundStatusText(orderRefund.getRefundStatus()));
        vo.setRefundType(orderRefund.getRefundType());
        vo.setRefundTypeText(getRefundTypeText(orderRefund.getRefundType()));
        vo.setOperatorId(orderRefund.getOperatorId());
        vo.setOperatorName(orderRefund.getOperatorName());
        vo.setOperatorTime(orderRefund.getOperatorTime());
        vo.setOperatorRemark(orderRefund.getOperatorRemark());
        vo.setRefundTime(orderRefund.getRefundTime());
        vo.setRefundPaymentMethod(orderRefund.getRefundPaymentMethod());
        vo.setRefundPaymentNo(orderRefund.getRefundPaymentNo());
        vo.setCreateTime(orderRefund.getCreateTime());

        // 查询退款明细
        LambdaQueryWrapper<OrderRefundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderRefundItem::getRefundId, orderRefund.getId());
        List<OrderRefundItem> refundItems = orderRefundItemRepository.selectList(itemWrapper);
        
        List<OrderRefundVO.OrderRefundItemVO> itemVOs = refundItems.stream().map(item -> {
            OrderRefundVO.OrderRefundItemVO itemVO = new OrderRefundVO.OrderRefundItemVO();
            itemVO.setId(item.getId());
            itemVO.setOrderItemId(item.getOrderItemId());
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setProductCode(item.getProductCode());
            itemVO.setSkuId(item.getSkuId());
            itemVO.setSpecCombination(item.getSpecCombination());
            itemVO.setRefundQuantity(item.getRefundQuantity());
            itemVO.setRefundPrice(item.getRefundPrice());
            itemVO.setRefundSubtotal(item.getRefundSubtotal());
            return itemVO;
        }).collect(Collectors.toList());
        
        vo.setRefundItems(itemVOs);
        return vo;
    }

    /**
     * 获取退款状态文本
     */
    private String getRefundStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 3:
                return "退款中";
            case 4:
                return "退款成功";
            case 5:
                return "退款失败";
            default:
                return "未知";
        }
    }

    /**
     * 获取退款类型文本
     */
    private String getRefundTypeText(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "部分退款";
            case 2:
                return "全额退款";
            default:
                return "未知";
        }
    }

    /**
     * 计算订单运费
     *
     * @param orderItems 订单商品列表
     * @param address 收货地址
     * @return 运费金额
     */
    private BigDecimal calculateOrderShippingFee(List<OrderItem> orderItems, UserAddress address) {
        try {
            // 按运费模板分组商品
            Map<Long, List<OrderItem>> templateGroups = orderItems.stream()
                .filter(item -> item.getShippingTemplateId() != null)
                .collect(Collectors.groupingBy(OrderItem::getShippingTemplateId));

            // 无运费模板的商品（包邮）
            List<OrderItem> freeShippingItems = orderItems.stream()
                .filter(item -> item.getShippingTemplateId() == null)
                .collect(Collectors.toList());

            if (templateGroups.isEmpty()) {
                log.info("订单所有商品包邮，运费为0");
                return BigDecimal.ZERO;
            }

            BigDecimal totalShippingFee = BigDecimal.ZERO;

            // 计算每个运费模板组的运费
            for (Map.Entry<Long, List<OrderItem>> entry : templateGroups.entrySet()) {
                Long templateId = entry.getKey();
                List<OrderItem> items = entry.getValue();

                // 计算该模板组的总重量和总金额
                BigDecimal totalWeight = items.stream()
                    .map(item -> {
                        BigDecimal weight = item.getWeight() != null ? item.getWeight() : BigDecimal.ZERO;
                        return weight.multiply(BigDecimal.valueOf(item.getQuantity()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalAmount = items.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Integer totalQuantity = items.stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();

                // 构建运费计算参数
                ShippingFeeCalculateDTO calculateDTO = new ShippingFeeCalculateDTO();
                calculateDTO.setProvince(address.getProvince());
                calculateDTO.setCity(address.getCity());
                calculateDTO.setDistrict(address.getDistrict());
                // 转换为kg（商品重量单位是克）
                calculateDTO.setTotalWeight(totalWeight.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP));
                calculateDTO.setTotalAmount(totalAmount);
                calculateDTO.setTotalQuantity(totalQuantity);

                // 调用运费计算服务（直接使用运费模板ID）
                BigDecimal fee = shippingService.calculateShippingFeeByTemplate(templateId, calculateDTO);
                totalShippingFee = totalShippingFee.add(fee);

                log.info("模板ID={} 运费={}, 商品数={}, 总重量={}g, 总金额={}",
                    templateId, fee, items.size(), totalWeight, totalAmount);
            }

            log.info("订单运费计算完成: 总运费={}, 模板组数={}, 包邮商品数={}",
                totalShippingFee, templateGroups.size(), freeShippingItems.size());

            return totalShippingFee;
        } catch (Exception e) {
            log.error("计算运费失败，使用默认运费0", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 取消订单时联动取消相关的支付记录状态
     *
     * @param orderId 订单ID
     */
    private void cancelPaymentRecords(Long orderId) {
        // 查询该订单的所有支付记录
        LambdaQueryWrapper<PaymentRecord> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(PaymentRecord::getOrderId, orderId);

        List<PaymentRecord> paymentRecords = paymentRecordRepository.selectList(paymentWrapper);

        if (paymentRecords.isEmpty()) {
            log.info("订单 {} 没有关联的支付记录，无需取消", orderId);
            return;
        }

        // 更新支付记录状态为已关闭（3）
        for (PaymentRecord paymentRecord : paymentRecords) {
            // 只有待支付和支付中的记录才需要取消
            if (PaymentStatus.UNPAID.equals(paymentRecord.getPaymentStatus()) ||
                PaymentStatus.PAYING.equals(paymentRecord.getPaymentStatus())) {

                paymentRecord.setPaymentStatus(PaymentStatus.CLOSED);
                paymentRecordRepository.updateById(paymentRecord);

                log.info("订单取消联动取消支付记录：orderId={}, paymentRecordId={}, paymentNo={}",
                        orderId, paymentRecord.getId(), paymentRecord.getPaymentNo());
            } else {
                log.info("支付记录状态无需取消：orderId={}, paymentRecordId={}, paymentStatus={}",
                        orderId, paymentRecord.getId(), paymentRecord.getPaymentStatus());
            }
        }
    }
}

