package com.shoppingmall.repository.website;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.Brand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Mapper
public interface BrandRepository extends BaseMapper<Brand> {

}
