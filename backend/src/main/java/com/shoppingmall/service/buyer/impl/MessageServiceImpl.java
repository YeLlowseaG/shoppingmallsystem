package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.MessageQueryDTO;
import com.shoppingmall.entity.Message;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.MessageRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.buyer.MessageService;
import com.shoppingmall.vo.MessagePageVO;
import com.shoppingmall.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内消息服务实现（用户端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public MessagePageVO getInboxMessages(Long userId, MessageQueryDTO queryDTO) {
        Page<Message> page = new Page<>(
                queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1,
                queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10
        );

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getDeleted, 0);

        // 消息类型筛选
        if (queryDTO.getMessageType() != null) {
            wrapper.eq(Message::getMessageType, queryDTO.getMessageType());
        }

        // 是否已读筛选
        if (queryDTO.getIsRead() != null) {
            wrapper.eq(Message::getIsRead, queryDTO.getIsRead() ? 1 : 0);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> messagePage = messageRepository.selectPage(page, wrapper);

        // 转换为VO
        List<MessageVO> voList = messagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 获取发送人信息
        List<Long> senderIds = voList.stream()
                .map(MessageVO::getSenderId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, String> senderNameMap;
        if (!senderIds.isEmpty()) {
            senderNameMap = userRepository.selectBatchIds(senderIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
        } else {
            senderNameMap = Map.of();
        }

        voList.forEach(vo -> {
            if (vo.getSenderId() != null && senderNameMap.containsKey(vo.getSenderId())) {
                vo.setSenderName(senderNameMap.get(vo.getSenderId()));
            }
        });

        // 获取未读数量
        LambdaQueryWrapper<Message> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Message::getReceiverId, userId);
        unreadWrapper.eq(Message::getIsRead, 0);
        unreadWrapper.eq(Message::getDeleted, 0);
        Long unreadCount = messageRepository.selectCount(unreadWrapper);

        MessagePageVO pageVO = new MessagePageVO();
        pageVO.setRecords(voList);
        pageVO.setTotal(messagePage.getTotal());
        pageVO.setUnreadCount(unreadCount.intValue());

        return pageVO;
    }

    @Override
    public void markAsRead(Long userId, Long messageId) {
        Message message = messageRepository.selectById(messageId);
        if (message == null || !message.getReceiverId().equals(userId)) {
            throw new RuntimeException("消息不存在或无权限");
        }

        message.setIsRead(1);
        messageRepository.updateById(message);
    }

    @Override
    public void markAllAsRead(Long userId) {
        Message message = new Message();
        message.setIsRead(1);

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.eq(Message::getDeleted, 0);

        messageRepository.update(message, wrapper);
    }

    @Override
    public void deleteMessage(Long userId, Long messageId) {
        Message message = messageRepository.selectById(messageId);
        if (message == null || !message.getReceiverId().equals(userId)) {
            throw new RuntimeException("消息不存在或无权限");
        }

        messageRepository.deleteById(messageId);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.eq(Message::getDeleted, 0);

        return messageRepository.selectCount(wrapper).intValue();
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setIsRead(message.getIsRead() == 1);
        return vo;
    }
}
