package com.shoppingmall.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.permission.MenuService;
import com.shoppingmall.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@RestController
@RequestMapping("/api/admin/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public Result<List<MenuVO>> getMenuTree() {
        List<MenuVO> menuTree = menuService.getMenuTree();
        return Result.success(menuTree);
    }

    /**
     * 根据ID获取菜单信息
     */
    @GetMapping("/{id}")
    public Result<MenuVO> getMenuById(@PathVariable Long id) {
        MenuVO menu = menuService.getMenuById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @PostMapping
    public Result<Void> addMenu(@RequestBody MenuVO menuVO) {
        menuService.addMenu(menuVO);
        return Result.success();
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody MenuVO menuVO) {
        menuService.updateMenu(id, menuVO);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }

    /**
     * 启用/禁用菜单
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateMenuStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        menuService.updateMenuStatus(id, status);
        return Result.success();
    }
}




























































