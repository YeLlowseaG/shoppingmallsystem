package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.PaymentRecordQueryDTO;
import com.shoppingmall.dto.RefundRequestDTO;
import com.shoppingmall.entity.*;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.payment.PaymentRecordRepository;
import com.shoppingmall.repository.permission.AdminUserRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.payment.exception.PaymentException;
import com.shoppingmall.payment.service.PaymentGatewayService;
import com.shoppingmall.service.admin.FinanceService;
import com.shoppingmall.service.buyer.DepositService;
import com.shoppingmall.vo.PaymentRecordVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 财务管理服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final DepositService depositService;
    private final PaymentGatewayService paymentGatewayService;
    private final HttpServletRequest request;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public IPage<PaymentRecordVO> getPaymentRecordList(PaymentRecordQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();

        // 订单号（需要通过订单表关联查询）
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderNo, queryDTO.getOrderNo());
            List<Order> orders = orderRepository.selectList(orderWrapper);
            if (orders.isEmpty()) {
                Page<PaymentRecordVO> emptyPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
                emptyPage.setRecords(new ArrayList<>());
                emptyPage.setTotal(0);
                emptyPage.setPages(0);
                return emptyPage;
            }
            List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            wrapper.in(PaymentRecord::getOrderId, orderIds);
        }

        // 支付流水号
        if (StringUtils.hasText(queryDTO.getPaymentNo())) {
            wrapper.like(PaymentRecord::getPaymentNo, queryDTO.getPaymentNo());
        }

        // 支付方式
        if (StringUtils.hasText(queryDTO.getPaymentMethod())) {
            wrapper.eq(PaymentRecord::getPaymentMethod, queryDTO.getPaymentMethod());
        }

        // 支付状态
        if (queryDTO.getPaymentStatus() != null) {
            wrapper.eq(PaymentRecord::getPaymentStatus, queryDTO.getPaymentStatus());
        }

        // 时间范围
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            LocalDate startDate = LocalDate.parse(queryDTO.getStartTime(), DATE_FORMATTER);
            wrapper.ge(PaymentRecord::getCreateTime, startDate.atStartOfDay());
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            LocalDate endDate = LocalDate.parse(queryDTO.getEndTime(), DATE_FORMATTER);
            wrapper.le(PaymentRecord::getCreateTime, endDate.atTime(23, 59, 59));
        }

        // 排序
        wrapper.orderByDesc(PaymentRecord::getCreateTime);

        // 分页查询
        Page<PaymentRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PaymentRecord> recordPage = paymentRecordRepository.selectPage(page, wrapper);

        // 获取所有订单ID和用户ID
        List<Long> orderIds = recordPage.getRecords().stream()
                .map(PaymentRecord::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        // 查询订单信息（如果列表为空，返回空Map）
        Map<Long, Order> orderMap = new java.util.HashMap<>();
        if (!orderIds.isEmpty()) {
            orderMap = orderRepository.selectBatchIds(orderIds).stream()
                    .collect(Collectors.toMap(Order::getId, order -> order));
        }

        // 获取所有用户ID
        List<Long> userIds = orderMap.values().stream()
                .map(Order::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 查询用户信息（如果列表为空，返回空Map）
        Map<Long, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = userRepository.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        }

        // 转换为VO
        List<PaymentRecordVO> voList = new ArrayList<>();
        for (PaymentRecord record : recordPage.getRecords()) {
            PaymentRecordVO vo = convertToPaymentRecordVO(record, orderMap, userMap);
            voList.add(vo);
        }

        // 构建分页结果
        Page<PaymentRecordVO> resultPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        resultPage.setRecords(voList);
        resultPage.setTotal(recordPage.getTotal());
        resultPage.setPages(recordPage.getPages());

        return resultPage;
    }

    @Override
    public PaymentRecordVO getPaymentRecordById(Long id) {
        PaymentRecord record = paymentRecordRepository.selectById(id);
        if (record == null) {
            return null;
        }

        // 查询订单信息
        Order order = orderRepository.selectById(record.getOrderId());
        Map<Long, Order> orderMap = order != null ? Map.of(order.getId(), order) : Map.of();

        // 查询用户信息
        Map<Long, User> userMap = Map.of();
        if (order != null) {
            User user = userRepository.selectById(order.getUserId());
            if (user != null) {
                userMap = Map.of(user.getId(), user);
            }
        }

        return convertToPaymentRecordVO(record, orderMap, userMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundPaymentRecord(RefundRequestDTO refundDTO) {
        // 1. 验证支付记录
        PaymentRecord paymentRecord = paymentRecordRepository.selectById(refundDTO.getPaymentRecordId());
        if (paymentRecord == null) {
            throw new BusinessException(404, "支付记录不存在");
        }

        // 2. 验证支付状态
        if (!PaymentStatus.PAID.equals(paymentRecord.getPaymentStatus())) {
            throw new BusinessException(400, "只有已支付的记录才能退款");
        }

        // 3. 验证退款金额
        BigDecimal refundedAmount = paymentRecord.getRefundedAmount() != null 
                ? paymentRecord.getRefundedAmount() 
                : BigDecimal.ZERO;
        BigDecimal refundableAmount = paymentRecord.getAmount().subtract(refundedAmount);
        
        if (refundDTO.getRefundAmount().compareTo(refundableAmount) > 0) {
            throw new BusinessException(400, "退款金额不能超过可退款金额：" + refundableAmount);
        }

        if (refundDTO.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "退款金额必须大于0");
        }

        // 4. 获取当前管理员信息
        Long adminId = getCurrentAdminId();
        AdminUser adminUser = adminUserRepository.selectById(adminId);
        String adminName = adminUser != null ? adminUser.getUsername() : "系统";

        // 5. 根据支付方式处理退款
        String paymentMethod = paymentRecord.getPaymentMethod();
        String refundPaymentNo = null;

        if (PaymentMethod.WECHAT.equals(paymentMethod) || PaymentMethod.ALIPAY.equals(paymentMethod)) {
            // 微信/支付宝退款（调用真实退款接口）
            // 重要：支付宝退款API需要的是订单号（out_trade_no），而不是支付流水号
            // 需要从支付记录关联的订单中获取订单号
            Order order = orderRepository.selectById(paymentRecord.getOrderId());
            if (order == null) {
                throw new BusinessException(404, "订单不存在，无法退款");
            }
            String orderNoForRefund = order.getOrderNo();
            
            try {
                log.info("开始调用第三方退款接口，支付方式：{}，订单号：{}，支付流水号：{}，退款金额：{}，退款原因：{}",
                        paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), refundDTO.getRefundAmount(), refundDTO.getRefundReason());
                
                // 调用退款接口，传入订单号（支付宝/微信退款API需要的是创建支付订单时的订单号）
                refundPaymentNo = paymentGatewayService.refund(
                        paymentMethod,
                        orderNoForRefund, // 使用订单号而不是支付流水号
                        refundDTO.getRefundAmount(),
                        refundDTO.getRefundReason() != null ? refundDTO.getRefundReason() : "管理员退款"
                );
                
                log.info("第三方退款成功，支付方式：{}，订单号：{}，支付流水号：{}，退款流水号：{}，退款金额：{}",
                        paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), refundPaymentNo, refundDTO.getRefundAmount());
            } catch (PaymentException e) {
                log.error("第三方退款失败，支付方式：{}，订单号：{}，支付流水号：{}，退款金额：{}，错误信息：{}",
                        paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), refundDTO.getRefundAmount(), e.getMessage(), e);
                throw new BusinessException(500, "第三方退款失败：" + e.getMessage());
            } catch (Exception e) {
                log.error("第三方退款异常，支付方式：{}，订单号：{}，支付流水号：{}，退款金额：{}",
                        paymentMethod, orderNoForRefund, paymentRecord.getPaymentNo(), refundDTO.getRefundAmount(), e);
                throw new BusinessException(500, "第三方退款异常：" + e.getMessage());
            }
        } else if (PaymentMethod.PRE_DEPOSIT.equals(paymentMethod)) {
            // 预存款退款
            Order order = orderRepository.selectById(paymentRecord.getOrderId());
            if (order == null) {
                throw new BusinessException(404, "订单不存在");
            }
            try {
                log.info("开始预存款退款，订单号：{}，用户ID：{}，退款金额：{}",
                        order.getOrderNo(), order.getUserId(), refundDTO.getRefundAmount());
                
                depositService.depositRefund(order.getUserId(), order.getId(), order.getOrderNo(), refundDTO.getRefundAmount());
                refundPaymentNo = "DEPOSIT_REFUND_" + System.currentTimeMillis();
                
                log.info("预存款退款成功，订单号：{}，退款流水号：{}，退款金额：{}",
                        order.getOrderNo(), refundPaymentNo, refundDTO.getRefundAmount());
            } catch (Exception e) {
                log.error("预存款退款失败，订单号：{}，退款金额：{}", order.getOrderNo(), refundDTO.getRefundAmount(), e);
                throw new BusinessException(500, "预存款退款失败：" + e.getMessage());
            }
        } else {
            throw new BusinessException(400, "不支持的支付方式：" + paymentMethod);
        }

        // 6. 更新支付记录
        BigDecimal newRefundedAmount = refundedAmount.add(refundDTO.getRefundAmount());
        paymentRecord.setRefundedAmount(newRefundedAmount);
        paymentRecord.setRefundTime(LocalDateTime.now());
        paymentRecord.setRefundReason(refundDTO.getRefundReason());
        paymentRecord.setRefundOperatorId(adminId);
        paymentRecord.setRefundOperatorName(adminName);
        // 保存退款流水号（如果有）
        if (refundPaymentNo != null && paymentRecord.getCallbackData() != null) {
            // 可以在callbackData中保存退款流水号，或者添加新字段
            // 这里暂时记录在日志中，后续可以考虑添加refund_payment_no字段
            log.info("退款流水号：{}，支付记录ID：{}", refundPaymentNo, paymentRecord.getId());
        }

        // 如果全额退款，更新支付状态
        if (newRefundedAmount.compareTo(paymentRecord.getAmount()) >= 0) {
            paymentRecord.setPaymentStatus(PaymentStatus.REFUNDED);
            
            // 更新订单支付状态
            Order order = orderRepository.selectById(paymentRecord.getOrderId());
            if (order != null) {
                // 如果订单之前是已完成状态，需要扣减销量
                if (OrderStatus.COMPLETED.equals(order.getOrderStatus())) {
                    updateProductSalesCount(order.getId(), false);
                }
                
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                order.setOrderStatus(OrderStatus.REFUNDED);
                orderRepository.updateById(order);
            }
        }

        paymentRecordRepository.updateById(paymentRecord);

        log.info("支付记录退款成功：paymentRecordId={}, refundAmount={}, paymentMethod={}, operator={}", 
                refundDTO.getPaymentRecordId(), refundDTO.getRefundAmount(), paymentMethod, adminName);
    }

    /**
     * 转换为支付记录VO
     */
    private PaymentRecordVO convertToPaymentRecordVO(PaymentRecord record, Map<Long, Order> orderMap, Map<Long, User> userMap) {
        PaymentRecordVO vo = new PaymentRecordVO();
        BeanUtils.copyProperties(record, vo);

        // 设置订单信息
        Order order = orderMap.get(record.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());

            // 设置用户信息
            User user = userMap.get(order.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
        }

        // 设置支付方式名称
        if (StringUtils.hasText(record.getPaymentMethod())) {
            switch (record.getPaymentMethod()) {
                case "WECHAT":
                    vo.setPaymentMethodName("微信支付");
                    break;
                case "ALIPAY":
                    vo.setPaymentMethodName("支付宝");
                    break;
                case "PRE_DEPOSIT":
                    vo.setPaymentMethodName("预存款");
                    break;
                default:
                    vo.setPaymentMethodName(record.getPaymentMethod());
            }
        }

        // 设置支付状态名称
        if (record.getPaymentStatus() != null) {
            switch (record.getPaymentStatus()) {
                case 0:
                    vo.setPaymentStatusName("待支付");
                    break;
                case 1:
                    vo.setPaymentStatusName("支付中");
                    break;
                case 2:
                    vo.setPaymentStatusName("已支付");
                    break;
                case 3:
                    vo.setPaymentStatusName("已关闭");
                    break;
                case 4:
                    vo.setPaymentStatusName("已失败");
                    break;
                case 5:
                    vo.setPaymentStatusName("已退款");
                    break;
                default:
                    vo.setPaymentStatusName("未知");
            }
        }

        // 计算可退款金额
        BigDecimal refundedAmount = record.getRefundedAmount() != null 
                ? record.getRefundedAmount() 
                : BigDecimal.ZERO;
        vo.setRefundableAmount(record.getAmount().subtract(refundedAmount));

        return vo;
    }


    /**
     * 获取当前管理员ID
     */
    private Long getCurrentAdminId() {
        Long adminId = (Long) request.getAttribute("adminId");
        return adminId != null ? adminId : 0L;
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
}
