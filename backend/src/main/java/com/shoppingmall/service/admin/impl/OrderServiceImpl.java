package com.shoppingmall.service.admin.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.OrderQueryDTO;
import com.shoppingmall.dto.OrderRefundQueryDTO;
import com.shoppingmall.dto.ShippingAddressDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.entity.OrderLogistics;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.entity.User;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.common.constant.RefundStatus;
import com.shoppingmall.common.constant.RefundType;
import com.shoppingmall.dto.OrderRefundRequestDTO;
import com.shoppingmall.entity.OrderRefund;
import com.shoppingmall.entity.OrderRefundItem;
import com.shoppingmall.entity.PaymentRecord;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderLogisticsRepository;
import com.shoppingmall.repository.order.OrderRefundItemRepository;
import com.shoppingmall.repository.order.OrderRefundRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import com.shoppingmall.entity.PaymentApiLog;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.service.PaymentGatewayService;
import com.shoppingmall.service.payment.PaymentLogService;
import com.shoppingmall.payment.util.AlipayUtil;
import com.shoppingmall.service.admin.OrderService;
import com.shoppingmall.service.buyer.DepositService;
import com.shoppingmall.vo.OrderDetailVO;
import com.shoppingmall.vo.OrderListVO;
import com.shoppingmall.vo.OrderRefundVO;
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
    private final OrderRefundRepository orderRefundRepository;
    private final OrderRefundItemRepository orderRefundItemRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final ProductSkuRepository productSkuRepository;
    private final UserRepository userRepository;
    private final DepositService depositService;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentConfigService paymentConfigService;
    private final PaymentLogService paymentLogService;
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

        // 买家姓名或买家用户名查询（需要在内存中过滤，因为需要查询用户表）
        boolean needFilter = false;
        String searchBuyerName = null;
        String searchBuyerUsername = null;
        
        if (orderQueryDTO.getBuyerName() != null && !orderQueryDTO.getBuyerName().trim().isEmpty()) {
            searchBuyerName = orderQueryDTO.getBuyerName().trim().toLowerCase();
            needFilter = true;
        }
        
        if (orderQueryDTO.getBuyerUsername() != null && !orderQueryDTO.getBuyerUsername().trim().isEmpty()) {
            searchBuyerUsername = orderQueryDTO.getBuyerUsername().trim().toLowerCase();
            needFilter = true;
        }
        
        // 收货人姓名查询（需要解析shippingAddress JSON字段）
        String searchRecipientName = null;
        if (orderQueryDTO.getRecipientName() != null && !orderQueryDTO.getRecipientName().trim().isEmpty()) {
            searchRecipientName = orderQueryDTO.getRecipientName().trim().toLowerCase();
            needFilter = true;
        }
        
        // 如果需要过滤，进行内存过滤
        if (needFilter) {
            final String finalSearchBuyerName = searchBuyerName;
            final String finalSearchBuyerUsername = searchBuyerUsername;
            final String finalSearchRecipientName = searchRecipientName;
            
            List<OrderListVO> filteredList = voPage.getRecords().stream()
                .filter(vo -> {
                    // 买家姓名过滤
                    if (finalSearchBuyerName != null) {
                        String buyerName = vo.getBuyerName();
                        if (buyerName == null || !buyerName.toLowerCase().contains(finalSearchBuyerName)) {
                            return false;
                        }
                    }
                    
                    // 买家用户名过滤
                    if (finalSearchBuyerUsername != null) {
                        String buyerUsername = vo.getBuyerUsername();
                        if (buyerUsername == null || !buyerUsername.toLowerCase().contains(finalSearchBuyerUsername)) {
                            return false;
                        }
                    }
                    
                    // 收货人姓名过滤
                    if (finalSearchRecipientName != null) {
                        String recipientName = vo.getRecipientName();
                        if (recipientName == null || !recipientName.toLowerCase().contains(finalSearchRecipientName)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            // 重新构建分页结果
            Page<OrderListVO> filteredPage = new Page<>(orderQueryDTO.getPageNum(), orderQueryDTO.getPageSize());
            filteredPage.setRecords(filteredList);
            filteredPage.setTotal(filteredList.size());
            filteredPage.setCurrent(orderQueryDTO.getPageNum());
            filteredPage.setSize(orderQueryDTO.getPageSize());
            
            // 注意：由于在内存中过滤，总数可能不准确，这里使用过滤后的数量
            // 如果需要精确的分页，需要使用MySQL的JOIN查询在数据库层面查询
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);

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

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.updateById(order);

        // 联动取消支付记录状态
        cancelPaymentRecords(order.getId());

        log.info("管理员取消订单成功: orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refundOrder(String orderNo, OrderRefundRequestDTO refundDTO, Long adminId, String adminName) {
        // 1. 验证订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderRepository.selectOne(orderWrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 2. 验证订单状态（只有已支付、已发货、已完成的订单可以退款）
        if (!OrderStatus.PAID_UNSHIPPED.equals(order.getOrderStatus()) 
                && !OrderStatus.SHIPPED.equals(order.getOrderStatus())
                && !OrderStatus.COMPLETED.equals(order.getOrderStatus())) {
            throw new BusinessException(400, "当前订单状态不允许退款");
        }

        // 3. 验证订单是否已支付
        if (!PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new BusinessException(400, "订单未支付，无法退款");
        }

        // 4. 查询订单商品列表
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);
        
        if (orderItems.isEmpty()) {
            throw new BusinessException(400, "订单商品不存在");
        }

        // 5. 验证退款商品和数量
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<OrderRefundItem> refundItemList = new ArrayList<>();
        // 用于存储需要更新的订单商品（更新已退款数量）
        java.util.Map<Long, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));
        
        for (OrderRefundRequestDTO.RefundItemDTO refundItemDTO : refundDTO.getRefundItems()) {
            // 查找对应的订单商品
            OrderItem orderItem = orderItemMap.get(refundItemDTO.getOrderItemId());
            if (orderItem == null) {
                throw new BusinessException(400, "订单商品不存在: orderItemId=" + refundItemDTO.getOrderItemId());
            }

            // 验证退款数量
            if (refundItemDTO.getRefundQuantity() == null || refundItemDTO.getRefundQuantity() <= 0) {
                throw new BusinessException(400, "退款数量必须大于0: " + orderItem.getProductName());
            }

            // 计算已退款数量（查询该订单商品的所有已审核通过的退款）
            int refundedQuantity = calculateRefundedQuantity(orderItem.getId());
            int availableRefundQuantity = orderItem.getQuantity() - refundedQuantity;
            
            if (refundItemDTO.getRefundQuantity() > availableRefundQuantity) {
                throw new BusinessException(400, 
                    String.format("商品【%s】可退款数量不足，已退款：%d，订单数量：%d，可退款：%d", 
                        orderItem.getProductName(), refundedQuantity, orderItem.getQuantity(), availableRefundQuantity));
            }

            // 计算退款金额
            BigDecimal refundSubtotal = orderItem.getPrice()
                    .multiply(BigDecimal.valueOf(refundItemDTO.getRefundQuantity()))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            totalRefundAmount = totalRefundAmount.add(refundSubtotal);

            // 创建退款明细
            OrderRefundItem refundItem = new OrderRefundItem();
            refundItem.setOrderItemId(orderItem.getId());
            refundItem.setProductId(orderItem.getProductId());
            refundItem.setProductName(orderItem.getProductName());
            refundItem.setProductCode(orderItem.getProductCode());
            refundItem.setSkuId(orderItem.getSkuId());
            refundItem.setSpecCombination(orderItem.getSpecCombination());
            refundItem.setRefundQuantity(refundItemDTO.getRefundQuantity());
            refundItem.setRefundPrice(orderItem.getPrice());
            refundItem.setRefundSubtotal(refundSubtotal);
            refundItemList.add(refundItem);
        }

        // 6. 判断是部分退款还是全额退款
        BigDecimal totalOrderAmount = order.getTotalAmount(); // 商品总金额（不含运费）
        Integer refundType = totalRefundAmount.compareTo(totalOrderAmount) >= 0 
                ? RefundType.FULL_REFUND 
                : RefundType.PARTIAL_REFUND;

        // 7. 生成退款单号
        String refundNo = generateRefundNo();

        // 8. 查询支付记录
        LambdaQueryWrapper<PaymentRecord> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(PaymentRecord::getOrderId, order.getId());
        paymentWrapper.eq(PaymentRecord::getPaymentStatus, PaymentStatus.PAID);
        paymentWrapper.orderByDesc(PaymentRecord::getCreateTime);
        paymentWrapper.last("LIMIT 1");
        PaymentRecord paymentRecord = paymentRecordRepository.selectOne(paymentWrapper);
        
        if (paymentRecord == null) {
            throw new BusinessException(400, "未找到支付记录，无法退款");
        }

        // 9. 验证可退款金额
        BigDecimal refundedAmount = paymentRecord.getRefundedAmount() != null 
                ? paymentRecord.getRefundedAmount() 
                : BigDecimal.ZERO;
        BigDecimal refundableAmount = paymentRecord.getAmount().subtract(refundedAmount);
        
        if (totalRefundAmount.compareTo(refundableAmount) > 0) {
            throw new BusinessException(400, "退款金额不能超过可退款金额：" + refundableAmount);
        }

        // 10. 执行退款（根据支付方式）
        String paymentMethod = paymentRecord.getPaymentMethod();
        String refundPaymentNo = null;

        if (PaymentMethod.WECHAT.equals(paymentMethod) || PaymentMethod.ALIPAY.equals(paymentMethod)) {
            // 微信/支付宝退款（调用真实退款接口）
            // 重要：支付宝退款API需要的是订单号（out_trade_no），而不是支付流水号
            // 支付流水号格式：PAY_orderNo，直接使用订单号作为支付流水号后缀
            String orderNoForRefund = order.getOrderNo();
            
            try {
                log.info("开始调用第三方退款接口，支付方式：{}，订单号：{}，支付流水号：{}，退款金额：{}，退款原因：{}",
                        paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), totalRefundAmount, refundDTO.getRefundReason());
                
                // 先尝试使用商户订单号（out_trade_no）进行退款
                // 注意：支付宝要求同一笔退款请求必须使用相同的 out_request_no（退款单号）
                // 如果第一次退款失败需要重试，必须使用相同的退款单号
                String alipayRefundNo = "ALI_REFUND_" + System.currentTimeMillis();
                
                try {
                    // 对于支付宝，直接使用 AlipayUtil 以便控制退款单号
                    if (PaymentMethod.ALIPAY.equals(paymentMethod)) {
                        AlipayConfig alipayConfig = paymentConfigService.getAlipayConfig();
                        if (alipayConfig == null || !Boolean.TRUE.equals(alipayConfig.getEnabled())) {
                            throw new BusinessException(400, "支付宝未启用");
                        }
                        
                        AlipayConfig.AlipayEnvConfig envConfig = "production".equals(alipayConfig.getEnv())
                                ? alipayConfig.getProduction()
                                : alipayConfig.getSandbox();
                        
                        if (envConfig == null) {
                            throw new BusinessException(400, "支付宝环境配置不存在");
                        }
                        
                        String refundAmountStr = String.format("%.2f", totalRefundAmount.doubleValue());
                        
                        // 先尝试使用商户订单号（out_trade_no）进行退款
                        try {
                            log.info("尝试使用商户订单号进行订单退款，订单号：{}，退款金额：{}，退款单号：{}",
                                    orderNoForRefund, refundAmountStr, alipayRefundNo);
                            
                            long startTime = System.currentTimeMillis();
                            Map<String, String> result = AlipayUtil.refund(
                                    envConfig,
                                    orderNoForRefund, // 使用订单号（out_trade_no）
                                    alipayRefundNo, // 使用统一的退款单号
                                    refundAmountStr,
                                    false // 使用out_trade_no
                            );
                            long executionTime = System.currentTimeMillis() - startTime;
                            
                            String code = result.get("code");
                            String subCode = result.get("sub_code");
                            String subMsg = result.get("sub_msg");
                            String msg = result.get("msg");
                            
                            log.info("支付宝订单退款响应（使用out_trade_no），订单号：{}，响应码：{}，子错误码：{}，错误信息：{}",
                                    orderNoForRefund, code, subCode, subMsg != null ? subMsg : msg);
                            
                            // 记录退款接口日志
                            try {
                                PaymentApiLog apiLog = new PaymentApiLog();
                                apiLog.setPaymentMethod("ALIPAY");
                                apiLog.setApiType("REFUND");
                                apiLog.setBusinessType("ORDER");
                                apiLog.setOrderNo(orderNoForRefund);
                                apiLog.setPaymentNo(paymentRecord.getPaymentNo());
                                apiLog.setApiUrl(envConfig.getGateway() != null ? envConfig.getGateway() : 
                                    ("sandbox".equals(alipayConfig.getEnv()) ? "https://openapi.alipaydev.com/gateway.do" : "https://openapi.alipay.com/gateway.do"));
                                apiLog.setRequestMethod("POST");
                                apiLog.setExecutionTime((int) executionTime);
                                if ("10000".equals(code)) {
                                    apiLog.setApiStatus(1); // 成功
                                } else {
                                    apiLog.setApiStatus(0); // 失败
                                    apiLog.setErrorCode(code);
                                    apiLog.setErrorMessage(subMsg != null ? subMsg : msg);
                                }
                                try {
                                    Map<String, Object> requestData = new java.util.HashMap<>();
                                    requestData.put("orderNo", orderNoForRefund);
                                    requestData.put("refundNo", alipayRefundNo);
                                    requestData.put("refundAmount", refundAmountStr);
                                    requestData.put("refundReason", refundDTO.getRefundReason());
                                    apiLog.setRequestData(objectMapper.writeValueAsString(requestData));
                                    apiLog.setResponseData(objectMapper.writeValueAsString(result));
                                } catch (Exception e) {
                                    log.warn("序列化退款数据失败", e);
                                }
                                paymentLogService.savePaymentLog(apiLog);
                            } catch (Exception e) {
                                log.warn("记录退款接口日志失败", e);
                            }
                            
                            if ("10000".equals(code)) {
                                refundPaymentNo = alipayRefundNo;
                                log.info("支付宝订单退款成功（使用商户订单号），订单号：{}，退款单号：{}，退款金额：{}",
                                        orderNoForRefund, refundPaymentNo, totalRefundAmount);
                            } else {
                                String errorMsg = subMsg != null && !subMsg.isEmpty() ? subMsg : (msg != null ? msg : "退款失败");
                                
                                // 如果是系统错误，尝试使用trade_no
                                if ("20000".equals(code) && "aop.ACQ.SYSTEM_ERROR".equals(subCode)) {
                                    String tradeNo = paymentRecord.getExternalTradeNo();
                                    if (tradeNo != null && !tradeNo.isEmpty()) {
                                        log.warn("使用商户订单号退款失败（系统错误），尝试使用支付宝交易号进行退款，订单号：{}，支付宝交易号：{}，错误信息：{}",
                                                orderNoForRefund, tradeNo, errorMsg);
                                        throw new PaymentException(500, errorMsg); // 抛出异常，触发重试逻辑
                                    } else {
                                        throw new PaymentException(500, errorMsg);
                                    }
                                } else {
                                    throw new PaymentException(500, errorMsg);
                                }
                            }
                        } catch (PaymentException e) {
                            // 如果使用商户订单号退款失败，且错误码是系统错误，尝试使用支付宝交易号（trade_no）
                            String errorMessage = e.getMessage();
                            if (errorMessage != null && (errorMessage.contains("系统异常") || errorMessage.contains("SYSTEM_ERROR") || errorMessage.contains("订单不存在"))) {
                                String tradeNo = paymentRecord.getExternalTradeNo();
                                
                                if (tradeNo != null && !tradeNo.isEmpty()) {
                                    log.warn("使用商户订单号退款失败，尝试使用支付宝交易号进行订单退款，订单号：{}，支付宝交易号：{}，错误信息：{}",
                                            orderNoForRefund, tradeNo, errorMessage);
                                    
                                    // 使用支付宝交易号（trade_no）进行退款
                                    // 重要：使用相同的退款单号（alipayRefundNo），因为这是同一笔退款请求的重试
                                    // 注意：refundAmountStr 已经在外部作用域声明，这里直接使用
                                    
                                    Map<String, String> result = AlipayUtil.refund(
                                            envConfig,
                                            tradeNo, // 使用支付宝交易号（trade_no）
                                            alipayRefundNo, // 使用相同的退款单号
                                            refundAmountStr, // 使用外部作用域已声明的 refundAmountStr
                                            true // 使用trade_no
                                    );
                                    
                                    String code = result.get("code");
                                    String subCode = result.get("sub_code");
                                    String subMsg = result.get("sub_msg");
                                    String msg = result.get("msg");
                                    
                                    log.info("支付宝订单退款响应（使用trade_no），订单号：{}，支付宝交易号：{}，响应码：{}，子错误码：{}，错误信息：{}",
                                            orderNoForRefund, tradeNo, code, subCode, subMsg != null ? subMsg : msg);
                                    
                                    if ("10000".equals(code)) {
                                        refundPaymentNo = alipayRefundNo;
                                        log.info("支付宝订单退款成功（使用支付宝交易号），订单号：{}，支付宝交易号：{}，退款单号：{}，退款金额：{}",
                                                orderNoForRefund, tradeNo, refundPaymentNo, totalRefundAmount);
                                    } else {
                                        String errorMsg = subMsg != null && !subMsg.isEmpty() ? subMsg : (msg != null ? msg : "退款失败");
                                        log.error("使用支付宝交易号退款也失败，订单号：{}，支付宝交易号：{}，错误码：{}，子错误码：{}，错误信息：{}",
                                                orderNoForRefund, tradeNo, code, subCode, errorMsg);
                                        throw new PaymentException(500, "使用支付宝交易号退款失败：" + errorMsg);
                                    }
                                } else {
                                    log.error("支付记录中没有保存外部交易号，订单号：{}，无法使用trade_no进行退款", orderNoForRefund);
                                    throw e; // 抛出原始异常
                                }
                            } else {
                                // 不是系统错误，直接抛出异常
                                throw e;
                            }
                        }
                    } else {
                        // 微信支付，使用 PaymentGatewayService
                refundPaymentNo = paymentGatewayService.refund(
                        paymentMethod,
                                orderNoForRefund,
                        totalRefundAmount,
                        refundDTO.getRefundReason() != null ? refundDTO.getRefundReason() : "管理员退款"
                );
                
                        log.info("第三方退款成功（使用商户订单号），支付方式：{}，订单号：{}，支付流水号：{}，退款流水号：{}，退款金额：{}",
                                paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), refundPaymentNo, totalRefundAmount);
                    }
            } catch (PaymentException e) {
                    log.error("第三方退款失败，支付方式：{}，订单号：{}，支付流水号：{}，退款金额：{}，错误信息：{}",
                            paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), totalRefundAmount, e.getMessage(), e);
                    throw new BusinessException(500, "第三方退款失败：" + e.getMessage());
                }
            } catch (PaymentException e) {
                log.error("第三方退款失败，支付方式：{}，订单号：{}，支付流水号：{}，退款金额：{}，错误信息：{}",
                        paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), totalRefundAmount, e.getMessage(), e);
                throw new BusinessException(500, "第三方退款失败：" + e.getMessage());
            } catch (Exception e) {
                log.error("第三方退款异常，支付方式：{}，订单号：{}，支付流水号：{}，退款金额：{}",
                        paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), totalRefundAmount, e);
                throw new BusinessException(500, "第三方退款异常：" + e.getMessage());
            }
        } else if (PaymentMethod.PRE_DEPOSIT.equals(paymentMethod)) {
            // 预存款退款
            try {
                log.info("开始预存款退款，订单号：{}，用户ID：{}，退款金额：{}",
                        order.getOrderNo(), order.getUserId(), totalRefundAmount);
                
                depositService.depositRefund(order.getUserId(), order.getId(), order.getOrderNo(), totalRefundAmount);
                refundPaymentNo = "DEPOSIT_REFUND_" + System.currentTimeMillis();
                
                log.info("预存款退款成功，订单号：{}，退款流水号：{}，退款金额：{}",
                        order.getOrderNo(), refundPaymentNo, totalRefundAmount);
            } catch (Exception e) {
                log.error("预存款退款失败，订单号：{}，退款金额：{}", order.getOrderNo(), totalRefundAmount, e);
                throw new BusinessException(500, "预存款退款失败：" + e.getMessage());
            }
        } else {
            throw new BusinessException(400, "不支持的支付方式：" + paymentMethod);
        }

        // 11. 创建退款申请记录
        OrderRefund orderRefund = new OrderRefund();
        orderRefund.setRefundNo(refundNo);
        orderRefund.setOrderId(order.getId());
        orderRefund.setOrderNo(order.getOrderNo());
        orderRefund.setUserId(order.getUserId());
        orderRefund.setRefundAmount(totalRefundAmount);
        orderRefund.setRefundReason(refundDTO.getRefundReason());
        orderRefund.setRefundStatus(RefundStatus.REFUND_SUCCESS); // 管理员操作，直接标记为退款成功
        orderRefund.setRefundType(refundType);
        orderRefund.setOperatorId(adminId);
        orderRefund.setOperatorName(adminName);
        orderRefund.setOperatorTime(LocalDateTime.now());
        orderRefund.setOperatorRemark("管理员直接退款");
        orderRefund.setRefundTime(LocalDateTime.now());
        orderRefund.setRefundPaymentMethod(paymentMethod);
        orderRefund.setRefundPaymentNo(refundPaymentNo);
        orderRefundRepository.insert(orderRefund);

        // 12. 创建退款明细并更新订单商品的已退款数量
        for (OrderRefundItem refundItem : refundItemList) {
            refundItem.setRefundId(orderRefund.getId());
            orderRefundItemRepository.insert(refundItem);
            
            // 更新订单商品的已退款数量
            OrderItem orderItem = orderItemMap.get(refundItem.getOrderItemId());
            if (orderItem != null) {
                orderItem.setRefundedQuantity((orderItem.getRefundedQuantity() != null ? orderItem.getRefundedQuantity() : 0) 
                        + refundItem.getRefundQuantity());
                orderItemRepository.updateById(orderItem);
            }
        }

        // 13. 更新支付记录
        BigDecimal newRefundedAmount = refundedAmount.add(totalRefundAmount);
        paymentRecord.setRefundedAmount(newRefundedAmount);
        paymentRecord.setRefundTime(LocalDateTime.now());
        paymentRecord.setRefundReason(refundDTO.getRefundReason());
        paymentRecord.setRefundOperatorId(adminId);
        paymentRecord.setRefundOperatorName(adminName);

        // 如果全额退款，更新支付状态
        if (newRefundedAmount.compareTo(paymentRecord.getAmount()) >= 0) {
            paymentRecord.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setOrderStatus(OrderStatus.REFUNDED);
        }
        paymentRecordRepository.updateById(paymentRecord);
        orderRepository.updateById(order);

        // 14. 恢复库存（如果订单已完成，需要扣减销量）
        if (OrderStatus.COMPLETED.equals(order.getOrderStatus())) {
            updateProductSalesCount(order.getId(), false, refundItemList);
        }
        
        // 恢复商品库存
        for (OrderRefundItem refundItem : refundItemList) {
            // 恢复product表的库存
            Product product = productRepository.selectById(refundItem.getProductId());
            if (product != null && product.getStock() != null) {
                product.setStock(product.getStock() + refundItem.getRefundQuantity());
                productRepository.updateById(product);
            }

            // 恢复product_stock表的库存
            LambdaQueryWrapper<ProductStock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(ProductStock::getProductId, refundItem.getProductId());
            ProductStock productStock = productStockRepository.selectOne(stockWrapper);

            if (productStock != null) {
                // 增加可用库存
                int newAvailableStock = (productStock.getAvailableStock() != null ? productStock.getAvailableStock() : 0) 
                        + refundItem.getRefundQuantity();
                productStock.setAvailableStock(newAvailableStock);
                productStockRepository.updateById(productStock);
            }
        }

        log.info("订单退款成功: refundNo={}, orderNo={}, refundAmount={}, paymentMethod={}, operator={}", 
                refundNo, orderNo, totalRefundAmount, paymentMethod, adminName);
        
        return refundNo;
    }

    @Override
    public List<OrderRefundVO> getOrderRefundList(String orderNo) {
        // 验证订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderRepository.selectOne(orderWrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 查询该订单的所有退款申请
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getOrderId, order.getId());
        wrapper.orderByDesc(OrderRefund::getCreateTime);
        List<OrderRefund> refunds = orderRefundRepository.selectList(wrapper);

        return refunds.stream().map(this::convertRefundToVO).collect(Collectors.toList());
    }

    @Override
    public IPage<OrderRefundVO> getRefundList(OrderRefundQueryDTO queryDTO) {
        Page<OrderRefund> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();

        // 退款单号查询
        if (queryDTO.getRefundNo() != null && !queryDTO.getRefundNo().trim().isEmpty()) {
            wrapper.like(OrderRefund::getRefundNo, queryDTO.getRefundNo());
        }

        // 订单号查询
        if (queryDTO.getOrderNo() != null && !queryDTO.getOrderNo().trim().isEmpty()) {
            wrapper.like(OrderRefund::getOrderNo, queryDTO.getOrderNo());
        }

        // 用户ID查询
        if (queryDTO.getUserId() != null) {
            wrapper.eq(OrderRefund::getUserId, queryDTO.getUserId());
        }

        // 退款状态筛选
        if (queryDTO.getRefundStatus() != null) {
            wrapper.eq(OrderRefund::getRefundStatus, queryDTO.getRefundStatus());
        }

        // 退款类型筛选
        if (queryDTO.getRefundType() != null) {
            wrapper.eq(OrderRefund::getRefundType, queryDTO.getRefundType());
        }

        // 操作人ID筛选
        if (queryDTO.getOperatorId() != null) {
            wrapper.eq(OrderRefund::getOperatorId, queryDTO.getOperatorId());
        }

        // 日期范围筛选
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(OrderRefund::getCreateTime, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(OrderRefund::getCreateTime, queryDTO.getEndDate().atTime(23, 59, 59));
        }

        wrapper.orderByDesc(OrderRefund::getCreateTime);

        IPage<OrderRefund> refundPage = orderRefundRepository.selectPage(page, wrapper);

        // 转换为VO
        IPage<OrderRefundVO> voPage = refundPage.convert(this::convertRefundToVO);

        return voPage;
    }

    @Override
    public OrderRefundVO getRefundDetail(Long refundId) {
        OrderRefund orderRefund = orderRefundRepository.selectById(refundId);
        if (orderRefund == null) {
            throw new BusinessException(404, "退款记录不存在");
        }
        return convertRefundToVO(orderRefund);
    }

    /**
     * 计算订单商品的已退款数量
     */
    private int calculateRefundedQuantity(Long orderItemId) {
        // 查询该订单商品的所有已审核通过的退款明细
        LambdaQueryWrapper<OrderRefundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderRefundItem::getOrderItemId, orderItemId);
        List<OrderRefundItem> refundItems = orderRefundItemRepository.selectList(itemWrapper);
        
        if (refundItems.isEmpty()) {
            return 0;
        }

        // 查询这些退款明细对应的退款申请，只统计退款中或退款成功的
        List<Long> refundIds = refundItems.stream()
                .map(OrderRefundItem::getRefundId)
                .distinct()
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<OrderRefund> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.in(OrderRefund::getId, refundIds);
        refundWrapper.in(OrderRefund::getRefundStatus, 
                RefundStatus.REFUNDING, 
                RefundStatus.REFUND_SUCCESS);
        List<OrderRefund> approvedRefunds = orderRefundRepository.selectList(refundWrapper);
        
        if (approvedRefunds.isEmpty()) {
            return 0;
        }

        // 计算已退款数量
        List<Long> approvedRefundIds = approvedRefunds.stream()
                .map(OrderRefund::getId)
                .collect(Collectors.toList());
        
        return refundItems.stream()
                .filter(item -> approvedRefundIds.contains(item.getRefundId()))
                .mapToInt(OrderRefundItem::getRefundQuantity)
                .sum();
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
     * 生成退款单号（格式：RF + yyyyMMddHHmmss + 6位随机数）
     */
    private String generateRefundNo() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 1000000);
        return "RF" + dateTime + String.format("%06d", random);
    }


    /**
     * 更新商品销量（退款时扣减）
     */
    private void updateProductSalesCount(Long orderId, boolean increase, List<OrderRefundItem> refundItems) {
        for (OrderRefundItem refundItem : refundItems) {
            Product product = productRepository.selectById(refundItem.getProductId());
            if (product != null) {
                int currentSalesCount = product.getSalesCount() != null ? product.getSalesCount() : 0;
                int quantity = refundItem.getRefundQuantity();
                
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
                        refundItem.getProductId(), quantity, increase, product.getSalesCount());
            }
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
        vo.setErpSyncStatus(order.getErpSyncStatus());

        // 查询买家信息（用户信息）
        if (order.getUserId() != null) {
            User user = userRepository.selectById(order.getUserId());
            if (user != null) {
                vo.setBuyerName(user.getRealName());
                vo.setBuyerUsername(user.getUsername());
            } else {
                vo.setBuyerName("未知");
                vo.setBuyerUsername("未知");
            }
        } else {
            vo.setBuyerName("未知");
            vo.setBuyerUsername("未知");
        }

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

        // 查询买家信息（用户信息）
        if (order.getUserId() != null) {
            User user = userRepository.selectById(order.getUserId());
            if (user != null) {
                vo.setBuyerName(user.getRealName());
                vo.setBuyerUsername(user.getUsername());
            } else {
                vo.setBuyerName("未知");
                vo.setBuyerUsername("未知");
            }
        } else {
            vo.setBuyerName("未知");
            vo.setBuyerUsername("未知");
        }

        // 查询订单商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemRepository.selectList(itemWrapper);

        List<OrderDetailVO.OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderDetailVO.OrderItemVO itemVO = new OrderDetailVO.OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setProductCode(item.getProductCode());
            itemVO.setName(item.getProductName());
            itemVO.setSpecCombination(item.getSpecCombination());
            itemVO.setImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotal(item.getSubtotal());
            
            // 设置SKU ID和SKU编码
            if (item.getSkuId() != null) {
                itemVO.setSkuId(item.getSkuId());
                ProductSku sku = productSkuRepository.selectById(item.getSkuId());
                if (sku != null && sku.getSkuCode() != null && !sku.getSkuCode().trim().isEmpty()) {
                    itemVO.setSkuCode(sku.getSkuCode());
                }
            }
            
            // 计算已退款数量和可退款数量
            int refundedQuantity = calculateRefundedQuantity(item.getId());
            itemVO.setRefundedQuantity(refundedQuantity);
            itemVO.setAvailableRefundQuantity(item.getQuantity() - refundedQuantity);
            
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

