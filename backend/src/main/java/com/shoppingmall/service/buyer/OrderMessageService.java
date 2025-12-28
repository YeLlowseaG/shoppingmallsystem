package com.shoppingmall.service.buyer;

import com.shoppingmall.dto.OrderMessageDTO;

/**
 * 订单问题/消息服务接口（用户端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
public interface OrderMessageService {

    /**
     * 创建订单问题/消息
     *
     * @param userId 用户ID
     * @param dto 订单问题DTO
     * @return 订单问题ID
     */
    Long createOrderMessage(Long userId, OrderMessageDTO dto);
}























