package com.shoppingmall.repository.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ProductPrice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品价格数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Mapper
public interface ProductPriceRepository extends BaseMapper<ProductPrice> {

}
