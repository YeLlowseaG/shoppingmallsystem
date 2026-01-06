package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.OrderMessageHandleDTO;
import com.shoppingmall.dto.OrderMessageQueryDTO;
import com.shoppingmall.vo.OrderMessageVO;

/**
 * 订单问题/消息服务接口（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
public interface OrderMessageService {

    /**
     * 分页查询订单问题列表
     *
     * @param queryDTO 查询条件
     * @return 订单问题列表（分页）
     */
    IPage<OrderMessageVO> getOrderMessagePage(OrderMessageQueryDTO queryDTO);

    /**
     * 根据ID获取订单问题详情
     *
     * @param id 订单问题ID
     * @return 订单问题详情
     */
    OrderMessageVO getOrderMessageById(Long id);

    /**
     * 处理订单问题
     *
     * @param handleDTO 处理DTO
     * @param handlerId 处理人ID
     * @param handlerName 处理人姓名
     */
    void handleOrderMessage(OrderMessageHandleDTO handleDTO, Long handlerId, String handlerName);
}









































