package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.OrderMessageDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderMessage;
import com.shoppingmall.repository.order.OrderMessageRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.service.buyer.OrderMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

/**
 * 订单问题/消息服务实现类（用户端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Slf4j
@Service("buyerOrderMessageServiceImpl")
@RequiredArgsConstructor
public class OrderMessageServiceImpl implements OrderMessageService {

    private final OrderMessageRepository orderMessageRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderMessage(Long userId, OrderMessageDTO dto) {
        // 验证订单是否存在且属于当前用户
        Order order = orderRepository.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, dto.getOrderNumber())
                        .eq(Order::getUserId, userId)
        );

        if (order == null) {
            throw new BusinessException(400, "订单不存在或无权访问");
        }

        // 创建订单问题记录
        OrderMessage message = new OrderMessage();
        message.setOrderNo(dto.getOrderNumber());
        message.setUserId(userId);
        
        // 转换消息类型：paid -> 1, question -> 2
        if ("paid".equals(dto.getMessageType())) {
            message.setMessageType(1);
            message.setPaymentAmount(dto.getPaymentAmount());
            message.setPaymentMethod(dto.getPaymentMethod());
            message.setPaymentDate(dto.getPaymentDate());
            
            // 组合付款时间
            if (dto.getPaymentHour() != null && dto.getPaymentMinute() != null) {
                message.setPaymentTime(LocalTime.of(dto.getPaymentHour(), dto.getPaymentMinute()));
            }
        } else if ("question".equals(dto.getMessageType())) {
            message.setMessageType(2);
            message.setTitle(dto.getTitle());
            message.setContent(dto.getContent());
        } else {
            throw new BusinessException(400, "无效的消息类型");
        }

        message.setRemarks(dto.getRemarks());
        message.setStatus(0); // 待处理

        orderMessageRepository.insert(message);
        log.info("创建订单问题成功: orderNo={}, messageType={}, id={}", dto.getOrderNumber(), dto.getMessageType(), message.getId());
        
        return message.getId();
    }
}













































