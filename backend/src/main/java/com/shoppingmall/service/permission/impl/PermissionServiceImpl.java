package com.shoppingmall.service.permission.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.entity.AdminRole;
import com.shoppingmall.entity.Menu;
import com.shoppingmall.entity.RoleMenu;
import com.shoppingmall.repository.permission.AdminRoleRepository;
import com.shoppingmall.repository.permission.MenuRepository;
import com.shoppingmall.repository.permission.RoleMenuRepository;
import com.shoppingmall.service.permission.PermissionService;
import com.shoppingmall.vo.MenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public List<MenuVO> getMenusByAdminId(Long adminId) {
        // 获取管理员的所有角色
        List<AdminRole> adminRoles = adminRoleRepository.selectList(
                new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, adminId)
        );
        if (adminRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色ID
        Set<Long> roleIds = adminRoles.stream()
                .map(AdminRole::getRoleId)
                .collect(Collectors.toSet());

        // 获取所有菜单ID
        List<RoleMenu> roleMenus = roleMenuRepository.selectList(
                new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getRoleId, roleIds)
        );
        Set<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toSet());

        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有菜单（只获取目录和菜单类型，不包含按钮）
        List<Menu> menus = menuRepository.selectList(
                new LambdaQueryWrapper<Menu>()
                        .in(Menu::getId, menuIds)
                        .in(Menu::getMenuType, "目录", "菜单")
                        .eq(Menu::getStatus, 1)
                        .eq(Menu::getDeleted, 0)
                        .orderByAsc(Menu::getSortOrder)
        );

        // 转换为VO并构建树形结构
        List<MenuVO> menuVOList = menus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return buildMenuTree(menuVOList);
    }

    @Override
    public List<String> getPermissionsByAdminId(Long adminId) {
        // 获取管理员的所有角色
        List<AdminRole> adminRoles = adminRoleRepository.selectList(
                new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, adminId)
        );
        if (adminRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色ID
        Set<Long> roleIds = adminRoles.stream()
                .map(AdminRole::getRoleId)
                .collect(Collectors.toSet());

        // 获取所有菜单ID
        List<RoleMenu> roleMenus = roleMenuRepository.selectList(
                new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getRoleId, roleIds)
        );
        Set<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toSet());

        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有权限标识
        List<Menu> menus = menuRepository.selectList(
                new LambdaQueryWrapper<Menu>()
                        .in(Menu::getId, menuIds)
                        .eq(Menu::getStatus, 1)
                        .eq(Menu::getDeleted, 0)
                        .isNotNull(Menu::getPermission)
        );

        return menus.stream()
                .map(Menu::getPermission)
                .filter(permission -> permission != null && !permission.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private MenuVO convertToVO(Menu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> menuList) {
        List<MenuVO> tree = new ArrayList<>();
        for (MenuVO menu : menuList) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                tree.add(menu);
            }
            for (MenuVO child : menuList) {
                if (child.getParentId() != null && child.getParentId().equals(menu.getId())) {
                    if (menu.getChildren() == null) {
                        menu.setChildren(new ArrayList<>());
                    }
                    menu.getChildren().add(child);
                }
            }
        }
        return tree;
    }
}


