package com.shoppingmall.repository.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.StockNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 缺货登记Repository
 *
 * @author ShoppingMall Team  
 * @date 2025-12-13
 */
@Mapper
public interface StockNotificationRepository extends BaseMapper<StockNotification> {
}