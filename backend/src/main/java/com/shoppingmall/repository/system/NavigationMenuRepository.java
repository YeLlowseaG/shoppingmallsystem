package com.shoppingmall.repository.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.NavigationMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导航菜单Repository
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Mapper
public interface NavigationMenuRepository extends BaseMapper<NavigationMenu> {
}