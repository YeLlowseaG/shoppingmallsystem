package com.shoppingmall.repository.sku;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ProductSpecKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品规格属性Repository
 */
@Mapper
public interface ProductSpecKeyRepository extends BaseMapper<ProductSpecKey> {
    
    /**
     * 根据商品ID查询规格属性列表
     * @param productId 商品ID
     * @return 规格属性列表
     */
    List<ProductSpecKey> findByProductId(@Param("productId") Long productId);
    
    /**
     * 根据商品ID删除规格属性
     * @param productId 商品ID
     * @return 删除的记录数
     */
    int deleteByProductId(@Param("productId") Long productId);
}