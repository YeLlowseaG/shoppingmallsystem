package com.shoppingmall.service.permission.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.entity.Menu;
import com.shoppingmall.entity.RoleMenu;
import com.shoppingmall.repository.permission.MenuRepository;
import com.shoppingmall.repository.permission.RoleMenuRepository;
import com.shoppingmall.service.permission.MenuService;
import com.shoppingmall.vo.MenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Override
    public List<MenuVO> getMenuTree() {
        List<Menu> menus = menuRepository.selectList(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getDeleted, 0)
                        .orderByAsc(Menu::getSortOrder)
        );

        List<MenuVO> menuVOList = menus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return buildMenuTree(menuVOList);
    }

    @Override
    public MenuVO getMenuById(Long id) {
        Menu menu = menuRepository.selectById(id);
        if (menu == null || menu.getDeleted() == 1) {
            throw new BusinessException("菜单不存在");
        }
        return convertToVO(menu);
    }

    @Override
    @Transactional
    public void addMenu(MenuVO menuVO) {
        // 检查权限标识是否已存在
        if (menuVO.getPermission() != null && !menuVO.getPermission().isEmpty()) {
            Menu existMenu = menuRepository.selectOne(
                    new LambdaQueryWrapper<Menu>()
                            .eq(Menu::getPermission, menuVO.getPermission())
                            .eq(Menu::getDeleted, 0)
            );
            if (existMenu != null) {
                throw new BusinessException("权限标识已存在");
            }
        }

        Menu menu = new Menu();
        BeanUtils.copyProperties(menuVO, menu);
        menu.setStatus(menuVO.getStatus() != null ? menuVO.getStatus() : 1);
        menu.setParentId(menuVO.getParentId() != null ? menuVO.getParentId() : 0L);
        menuRepository.insert(menu);
    }

    @Override
    @Transactional
    public void updateMenu(Long id, MenuVO menuVO) {
        Menu menu = menuRepository.selectById(id);
        if (menu == null || menu.getDeleted() == 1) {
            throw new BusinessException("菜单不存在");
        }

        // 检查权限标识是否已被其他菜单使用
        if (menuVO.getPermission() != null && !menuVO.getPermission().isEmpty()) {
            Menu existMenu = menuRepository.selectOne(
                    new LambdaQueryWrapper<Menu>()
                            .eq(Menu::getPermission, menuVO.getPermission())
                            .ne(Menu::getId, id)
                            .eq(Menu::getDeleted, 0)
            );
            if (existMenu != null) {
                throw new BusinessException("权限标识已被使用");
            }
        }

        BeanUtils.copyProperties(menuVO, menu);
        menuRepository.updateById(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.selectById(id);
        if (menu == null || menu.getDeleted() == 1) {
            throw new BusinessException("菜单不存在");
        }

        // 检查是否有子菜单
        List<Menu> children = menuRepository.selectList(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getParentId, id)
                        .eq(Menu::getDeleted, 0)
        );
        if (!children.isEmpty()) {
            throw new BusinessException("该菜单下还有子菜单，无法删除");
        }

        // 检查是否有关联角色
        List<RoleMenu> roleMenus = roleMenuRepository.selectList(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, id)
        );
        if (!roleMenus.isEmpty()) {
            throw new BusinessException("该菜单已被角色使用，无法删除");
        }

        // 逻辑删除
        menu.setDeleted(1);
        menuRepository.updateById(menu);
    }

    @Override
    public void updateMenuStatus(Long id, Integer status) {
        Menu menu = menuRepository.selectById(id);
        if (menu == null || menu.getDeleted() == 1) {
            throw new BusinessException("菜单不存在");
        }
        menu.setStatus(status);
        menuRepository.updateById(menu);
    }

    private MenuVO convertToVO(Menu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }

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





