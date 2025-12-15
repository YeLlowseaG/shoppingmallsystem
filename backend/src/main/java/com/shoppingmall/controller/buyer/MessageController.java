package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.MessageQueryDTO;
import com.shoppingmall.service.buyer.MessageService;
import com.shoppingmall.vo.MessagePageVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 站内消息控制器（用户端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@RestController
@RequestMapping("/api/buyer/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final HttpServletRequest request;

    /**
     * 获取收件箱消息列表
     */
    @GetMapping("/inbox")
    public Result<MessagePageVO> getInboxMessages(MessageQueryDTO queryDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }

        MessagePageVO pageVO = messageService.getInboxMessages(userId, queryDTO);
        return Result.success(pageVO);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }

        messageService.markAsRead(userId, id);
        return Result.success("标记成功");
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<?> markAllAsRead() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }

        messageService.markAllAsRead(userId);
        return Result.success("全部标记成功");
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteMessage(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }

        messageService.deleteMessage(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }

        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }
}


