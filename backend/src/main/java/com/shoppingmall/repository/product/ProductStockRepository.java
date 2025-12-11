package com.shoppingmall.repository.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ProductStock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Mapper
public interface ProductStockRepository extends BaseMapper<ProductStock> {

}
