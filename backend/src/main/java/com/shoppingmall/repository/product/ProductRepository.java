package com.shoppingmall.repository.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Mapper
public interface ProductRepository extends BaseMapper<Product> {

}
