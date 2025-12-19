package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.OrderMessageHandleDTO;
import com.shoppingmall.dto.OrderMessageQueryDTO;
import com.shoppingmall.entity.OrderMessage;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.order.OrderMessageRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.admin.OrderMessageService;
import com.shoppingmall.vo.OrderMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单问题/消息服务实现类（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Slf4j
@Service("adminOrderMessageServiceImpl")
@RequiredArgsConstructor
public class OrderMessageServiceImpl implements OrderMessageService {

    private final OrderMessageRepository orderMessageRepository;
    private final UserRepository userRepository;

    @Override
    public IPage<OrderMessageVO> getOrderMessagePage(OrderMessageQueryDTO queryDTO) {
        Page<OrderMessage> page = new Page<>(
                queryDTO.getCurrent() != null ? queryDTO.getCurrent() : 1,
                queryDTO.getSize() != null ? queryDTO.getSize() : 10
        );

        LambdaQueryWrapper<OrderMessage> wrapper = new LambdaQueryWrapper<>();

        // 订单号筛选
        if (StringUtil.isNotBlank(queryDTO.getOrderNo())) {
            wrapper.like(OrderMessage::getOrderNo, queryDTO.getOrderNo());
        }

        // 用户ID筛选
        if (queryDTO.getUserId() != null) {
            wrapper.eq(OrderMessage::getUserId, queryDTO.getUserId());
        }

        // 消息类型筛选
        if (queryDTO.getMessageType() != null) {
            wrapper.eq(OrderMessage::getMessageType, queryDTO.getMessageType());
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(OrderMessage::getStatus, queryDTO.getStatus());
        }

        // 日期范围筛选
        if (StringUtil.isNotBlank(queryDTO.getStartDate())) {
            wrapper.ge(OrderMessage::getCreateTime, queryDTO.getStartDate() + " 00:00:00");
        }
        if (StringUtil.isNotBlank(queryDTO.getEndDate())) {
            wrapper.le(OrderMessage::getCreateTime, queryDTO.getEndDate() + " 23:59:59");
        }

        // 按创建时间倒序
        wrapper.orderByDesc(OrderMessage::getCreateTime);

        Page<OrderMessage> messagePage = orderMessageRepository.selectPage(page, wrapper);

        // 转换为VO
        IPage<OrderMessageVO> voPage = new Page<>();
        voPage.setCurrent(messagePage.getCurrent());
        voPage.setSize(messagePage.getSize());
        voPage.setTotal(messagePage.getTotal());
        voPage.setPages(messagePage.getPages());

        List<OrderMessageVO> voList = messagePage.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public OrderMessageVO getOrderMessageById(Long id) {
        OrderMessage message = orderMessageRepository.selectById(id);
        if (message == null) {
            throw new BusinessException(404, "订单问题不存在");
        }
        return convertToVO(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderMessage(OrderMessageHandleDTO handleDTO, Long handlerId, String handlerName) {
        OrderMessage message = orderMessageRepository.selectById(handleDTO.getId());
        if (message == null) {
            throw new BusinessException(404, "订单问题不存在");
        }

        message.setStatus(handleDTO.getStatus());
        message.setHandlerId(handlerId);
        message.setHandlerName(handlerName);
        message.setHandleTime(LocalDateTime.now());
        message.setHandleRemark(handleDTO.getHandleRemark());

        orderMessageRepository.updateById(message);
        log.info("处理订单问题成功: id={}, status={}, handler={}", handleDTO.getId(), handleDTO.getStatus(), handlerName);
    }

    /**
     * 转换为VO
     */
    private OrderMessageVO convertToVO(OrderMessage message) {
        OrderMessageVO vo = new OrderMessageVO();
        vo.setId(message.getId());
        vo.setOrderNo(message.getOrderNo());
        vo.setUserId(message.getUserId());
        vo.setMessageType(message.getMessageType());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setPaymentAmount(message.getPaymentAmount());
        vo.setPaymentMethod(message.getPaymentMethod());
        vo.setPaymentDate(message.getPaymentDate());
        vo.setPaymentTime(message.getPaymentTime());
        vo.setRemarks(message.getRemarks());
        vo.setStatus(message.getStatus());
        vo.setHandlerId(message.getHandlerId());
        vo.setHandlerName(message.getHandlerName());
        vo.setHandleTime(message.getHandleTime());
        vo.setHandleRemark(message.getHandleRemark());
        vo.setCreateTime(message.getCreateTime());
        vo.setUpdateTime(message.getUpdateTime());

        // 设置消息类型文本
        if (message.getMessageType() != null) {
            vo.setMessageTypeText(message.getMessageType() == 1 ? "我已付款" : "我有问题");
        }

        // 设置状态文本
        if (message.getStatus() != null) {
            switch (message.getStatus()) {
                case 0:
                    vo.setStatusText("待处理");
                    break;
                case 1:
                    vo.setStatusText("处理中");
                    break;
                case 2:
                    vo.setStatusText("已处理");
                    break;
                case 3:
                    vo.setStatusText("已关闭");
                    break;
                default:
                    vo.setStatusText("未知");
            }
        }

        // 查询用户信息
        if (message.getUserId() != null) {
            User user = userRepository.selectById(message.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
            }
        }

        return vo;
    }
}







