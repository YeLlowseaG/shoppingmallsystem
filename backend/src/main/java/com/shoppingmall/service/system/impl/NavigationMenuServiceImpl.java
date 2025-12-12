package com.shoppingmall.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.entity.NavigationMenu;
import com.shoppingmall.repository.system.NavigationMenuRepository;
import com.shoppingmall.service.system.NavigationMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 导航菜单服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NavigationMenuServiceImpl implements NavigationMenuService {

    private final NavigationMenuRepository navigationMenuRepository;

    @Override
    public Page<NavigationMenu> getNavigationMenuPage(Long current, Long size, String menuName) {
        Page<NavigationMenu> page = new Page<>(current, size);

        LambdaQueryWrapper<NavigationMenu> wrapper = new LambdaQueryWrapper<>();

        // 菜单名称筛选
        if (StringUtil.isNotBlank(menuName)) {
            wrapper.like(NavigationMenu::getMenuName, menuName);
        }

        // 按排序号升序
        wrapper.orderByAsc(NavigationMenu::getSortOrder);
        wrapper.orderByAsc(NavigationMenu::getId);

        return navigationMenuRepository.selectPage(page, wrapper);
    }

    @Override
    public List<NavigationMenu> getEnabledMenus() {
        LambdaQueryWrapper<NavigationMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NavigationMenu::getStatus, 1)
                .orderByAsc(NavigationMenu::getSortOrder);

        return navigationMenuRepository.selectList(wrapper);
    }

    @Override
    public NavigationMenu getNavigationMenuById(Long id) {
        NavigationMenu menu = navigationMenuRepository.selectById(id);
        if (menu == null) {
            throw new BusinessException(404, "导航菜单不存在");
        }
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNavigationMenu(NavigationMenu navigationMenu) {
        navigationMenuRepository.insert(navigationMenu);
        log.info("创建导航菜单成功: {}", navigationMenu.getMenuName());
        return navigationMenu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNavigationMenu(NavigationMenu navigationMenu) {
        NavigationMenu existing = navigationMenuRepository.selectById(navigationMenu.getId());
        if (existing == null) {
            throw new BusinessException(404, "导航菜单不存在");
        }

        navigationMenuRepository.updateById(navigationMenu);
        log.info("更新导航菜单成功: {}", navigationMenu.getMenuName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNavigationMenu(Long id) {
        NavigationMenu existing = navigationMenuRepository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "导航菜单不存在");
        }

        navigationMenuRepository.deleteById(id);
        log.info("删除导航菜单成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        NavigationMenu menu = navigationMenuRepository.selectById(id);
        if (menu == null) {
            throw new BusinessException(404, "导航菜单不存在");
        }

        menu.setStatus(status);
        navigationMenuRepository.updateById(menu);
        log.info("更新导航菜单状态成功: id={}, status={}", id, status);
    }
}