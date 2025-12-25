package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.OrderMessageDTO;
import com.shoppingmall.service.buyer.OrderMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单问题/消息控制器（用户端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@RestController("buyerOrderMessageController")
@RequestMapping("/api/buyer/order-messages")
@RequiredArgsConstructor
public class OrderMessageController {

    private final OrderMessageService orderMessageService;
    private final HttpServletRequest request;

    /**
     * 创建订单问题/消息
     */
    @PostMapping
    public Result<Long> createOrderMessage(@Valid @RequestBody OrderMessageDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        Long id = orderMessageService.createOrderMessage(userId, dto);
        return Result.success("提交成功", id);
    }
}















