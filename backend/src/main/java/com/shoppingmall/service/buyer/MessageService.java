package com.shoppingmall.service.buyer;

import com.shoppingmall.dto.MessageQueryDTO;
import com.shoppingmall.vo.MessagePageVO;
import com.shoppingmall.vo.MessageVO;

/**
 * 站内消息服务接口（用户端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
public interface MessageService {

    /**
     * 获取收件箱消息列表
     */
    MessagePageVO getInboxMessages(Long userId, MessageQueryDTO queryDTO);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long userId, Long messageId);

    /**
     * 全部标记为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除消息
     */
    void deleteMessage(Long userId, Long messageId);

    /**
     * 获取未读消息数量
     */
    Integer getUnreadCount(Long userId);
}
