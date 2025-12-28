package com.shoppingmall.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.admin.DashboardService;
import com.shoppingmall.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@RestController("adminDashboardController")
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取数据看板统计信息
     */
    @GetMapping("/statistics")
    public Result<DashboardVO> getDashboardStatistics() {
        DashboardVO statistics = dashboardService.getDashboardStatistics();
        return Result.success("获取成功", statistics);
    }
}
























