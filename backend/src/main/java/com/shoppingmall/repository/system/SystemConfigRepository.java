package com.shoppingmall.repository.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Repository
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Mapper
public interface SystemConfigRepository extends BaseMapper<SystemConfig> {
}