package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.NavigationMenu;
import com.shoppingmall.service.system.NavigationMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 导航菜单控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@RestController("adminNavigationMenuController")
@RequestMapping("/api/admin/system/navigation")
@RequiredArgsConstructor
public class NavigationMenuController {

    private final NavigationMenuService navigationMenuService;

    /**
     * 分页查询导航菜单列表
     */
    @GetMapping("/page")
    public Result<Page<NavigationMenu>> getNavigationMenuPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestParam(required = false) String menuName) {
        Page<NavigationMenu> page = navigationMenuService.getNavigationMenuPage(current, size, menuName);
        return Result.success("获取成功", page);
    }

    /**
     * 获取所有启用的导航菜单
     */
    @GetMapping("/enabled")
    public Result<List<NavigationMenu>> getEnabledMenus() {
        List<NavigationMenu> menus = navigationMenuService.getEnabledMenus();
        return Result.success("获取成功", menus);
    }

    /**
     * 根据ID获取导航菜单详情
     */
    @GetMapping("/{id}")
    public Result<NavigationMenu> getNavigationMenuById(@PathVariable Long id) {
        NavigationMenu menu = navigationMenuService.getNavigationMenuById(id);
        return Result.success("获取成功", menu);
    }

    /**
     * 创建导航菜单
     */
    @PostMapping
    public Result<Long> createNavigationMenu(@RequestBody NavigationMenu navigationMenu) {
        Long id = navigationMenuService.createNavigationMenu(navigationMenu);
        return Result.success("创建成功", id);
    }

    /**
     * 更新导航菜单
     */
    @PutMapping
    public Result<Void> updateNavigationMenu(@RequestBody NavigationMenu navigationMenu) {
        navigationMenuService.updateNavigationMenu(navigationMenu);
        return Result.success("更新成功", null);
    }

    /**
     * 删除导航菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNavigationMenu(@PathVariable Long id) {
        navigationMenuService.deleteNavigationMenu(id);
        return Result.success("删除成功", null);
    }

    /**
     * 更新导航菜单状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        navigationMenuService.updateStatus(id, status);
        return Result.success("更新状态成功", null);
    }
}