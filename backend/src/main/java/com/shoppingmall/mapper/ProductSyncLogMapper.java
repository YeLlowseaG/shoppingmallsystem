package com.shoppingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ProductSyncLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品同步日志Mapper
 *
 * @author ShoppingMall Team
 * @date 2026-01-02
 */
@Mapper
public interface ProductSyncLogMapper extends BaseMapper<ProductSyncLog> {
}
