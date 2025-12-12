package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.NavigationMenu;
import com.shoppingmall.service.system.NavigationMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 导航菜单控制器（买家端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@RestController("buyerNavigationMenuController")
@RequestMapping("/api/buyer/navigation")
@RequiredArgsConstructor
public class NavigationMenuController {

    private final NavigationMenuService navigationMenuService;

    /**
     * 获取所有启用的导航菜单
     */
    @GetMapping("/menus")
    public Result<List<NavigationMenu>> getEnabledMenus() {
        List<NavigationMenu> menus = navigationMenuService.getEnabledMenus();
        return Result.success("获取成功", menus);
    }
}