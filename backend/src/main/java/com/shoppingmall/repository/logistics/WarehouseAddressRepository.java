package com.shoppingmall.repository.logistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.WarehouseAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发货地址库Repository
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
@Mapper
public interface WarehouseAddressRepository extends BaseMapper<WarehouseAddress> {
}

