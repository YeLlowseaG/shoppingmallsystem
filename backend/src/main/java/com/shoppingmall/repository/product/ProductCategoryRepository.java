package com.shoppingmall.repository.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Mapper
public interface ProductCategoryRepository extends BaseMapper<ProductCategory> {

}
