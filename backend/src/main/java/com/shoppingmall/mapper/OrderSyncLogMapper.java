package com.shoppingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.OrderSyncLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单同步日志Mapper接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Mapper
public interface OrderSyncLogMapper extends BaseMapper<OrderSyncLog> {
}
