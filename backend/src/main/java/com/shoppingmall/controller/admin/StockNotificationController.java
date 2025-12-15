package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.user.StockNotificationService;
import com.shoppingmall.vo.StockNotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 缺货登记控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-15
 */
@Slf4j
@RestController("adminStockNotificationController")
@RequestMapping("/api/admin/stock-notification")
@RequiredArgsConstructor
public class StockNotificationController {

    private final StockNotificationService stockNotificationService;

    /**
     * 分页查询所有缺货登记
     */
    @GetMapping("/page")
    public Result<Page<StockNotificationVO>> getNotificationPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {
        try {
            Page<StockNotificationVO> page = stockNotificationService.getAdminNotifications(
                    current, size, productName, status);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询缺货登记失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 商品补货后通知用户
     */
    @PostMapping("/notify/{productId}")
    public Result<?> notifyUsers(@PathVariable Long productId) {
        try {
            stockNotificationService.notifyUsers(productId);
            return Result.success("通知发送成功");
        } catch (Exception e) {
            log.error("发送补货通知失败: productId={}", productId, e);
            return Result.error("通知发送失败：" + e.getMessage());
        }
    }
}
