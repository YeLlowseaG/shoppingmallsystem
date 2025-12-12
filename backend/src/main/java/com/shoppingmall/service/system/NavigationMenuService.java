package com.shoppingmall.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.NavigationMenu;

import java.util.List;

/**
 * 导航菜单服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
public interface NavigationMenuService {

    /**
     * 分页查询导航菜单列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param menuName 菜单名称（可选）
     * @return 导航菜单分页列表
     */
    Page<NavigationMenu> getNavigationMenuPage(Long current, Long size, String menuName);

    /**
     * 获取所有启用的导航菜单
     *
     * @return 启用的导航菜单列表
     */
    List<NavigationMenu> getEnabledMenus();

    /**
     * 根据ID获取导航菜单详情
     *
     * @param id 菜单ID
     * @return 导航菜单详情
     */
    NavigationMenu getNavigationMenuById(Long id);

    /**
     * 创建导航菜单
     *
     * @param navigationMenu 导航菜单信息
     * @return 菜单ID
     */
    Long createNavigationMenu(NavigationMenu navigationMenu);

    /**
     * 更新导航菜单
     *
     * @param navigationMenu 导航菜单信息
     */
    void updateNavigationMenu(NavigationMenu navigationMenu);

    /**
     * 删除导航菜单
     *
     * @param id 菜单ID
     */
    void deleteNavigationMenu(Long id);

    /**
     * 更新导航菜单状态
     *
     * @param id 菜单ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);
}