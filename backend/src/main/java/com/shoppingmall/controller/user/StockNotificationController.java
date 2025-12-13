package com.shoppingmall.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.StockNotificationDTO;
import com.shoppingmall.service.user.StockNotificationService;
import com.shoppingmall.vo.StockNotificationVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 缺货登记控制器（用户端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@RestController("userStockNotificationController")
@RequestMapping("/api/user/stock-notification")
@RequiredArgsConstructor
public class StockNotificationController {

    private final StockNotificationService stockNotificationService;

    /**
     * 创建缺货登记
     */
    @PostMapping
    public Result<Long> createNotification(@Valid @RequestBody StockNotificationDTO dto, HttpServletRequest request) {
        // 从请求中获取用户ID（通常从JWT token中解析）
        // 这里暂时使用硬编码，实际项目中应该从认证信息中获取
        Long userId = getUserIdFromRequest(request);
        
        Long id = stockNotificationService.createNotification(userId, dto);
        return Result.success("登记成功，商品补货时将及时通知您", id);
    }

    /**
     * 取消缺货登记
     */
    @DeleteMapping("/{id}")
    public Result<?> cancelNotification(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        stockNotificationService.cancelNotification(userId, id);
        return Result.success("取消登记成功");
    }

    /**
     * 获取用户的缺货登记列表
     */
    @GetMapping("/list")
    public Result<Page<StockNotificationVO>> getUserNotifications(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        Page<StockNotificationVO> page = stockNotificationService.getUserNotifications(userId, current, size);
        return Result.success("获取成功", page);
    }

    /**
     * 检查是否已登记某商品
     */
    @GetMapping("/check/{productId}")
    public Result<Boolean> checkRegistered(@PathVariable Long productId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        boolean hasRegistered = stockNotificationService.hasRegistered(userId, productId);
        return Result.success("查询成功", hasRegistered);
    }

    /**
     * 从请求中获取用户ID
     * TODO: 实际项目中应该从JWT token中解析用户信息
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 暂时返回固定值，实际应该从token中获取
        String userIdHeader = request.getHeader("X-User-Id");
        return userIdHeader != null ? Long.parseLong(userIdHeader) : 1L;
    }
}