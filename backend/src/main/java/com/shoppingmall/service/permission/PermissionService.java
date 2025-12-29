package com.shoppingmall.service.permission;

import com.shoppingmall.vo.MenuVO;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public interface PermissionService {

    /**
     * 根据管理员ID获取菜单列表
     *
     * @param adminId 管理员ID
     * @return 菜单列表（树形结构）
     */
    List<MenuVO> getMenusByAdminId(Long adminId);

    /**
     * 根据管理员ID获取权限标识列表
     *
     * @param adminId 管理员ID
     * @return 权限标识列表
     */
    List<String> getPermissionsByAdminId(Long adminId);
}
























































