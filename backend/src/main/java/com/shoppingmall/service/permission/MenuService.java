package com.shoppingmall.service.permission;

import com.shoppingmall.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public interface MenuService {

    /**
     * 获取菜单树（所有菜单）
     */
    List<MenuVO> getMenuTree();

    /**
     * 根据ID获取菜单信息
     */
    MenuVO getMenuById(Long id);

    /**
     * 新增菜单
     */
    void addMenu(MenuVO menuVO);

    /**
     * 更新菜单
     */
    void updateMenu(Long id, MenuVO menuVO);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 启用/禁用菜单
     */
    void updateMenuStatus(Long id, Integer status);
}































