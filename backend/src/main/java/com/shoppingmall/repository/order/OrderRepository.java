package com.shoppingmall.repository.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Mapper
public interface OrderRepository extends BaseMapper<Order> {

}




