package com.shoppingmall.repository.sku;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ProductSpecValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品规格值Repository
 */
@Mapper
public interface ProductSpecValueRepository extends BaseMapper<ProductSpecValue> {
    
    /**
     * 根据规格属性ID查询规格值列表
     * @param specKeyId 规格属性ID
     * @return 规格值列表
     */
    List<ProductSpecValue> findBySpecKeyId(@Param("specKeyId") Long specKeyId);
    
    /**
     * 根据规格属性ID删除规格值
     * @param specKeyId 规格属性ID
     * @return 删除的记录数
     */
    int deleteBySpecKeyId(@Param("specKeyId") Long specKeyId);
    
    /**
     * 根据商品ID查询所有规格值
     * @param productId 商品ID
     * @return 规格值列表
     */
    List<ProductSpecValue> findByProductId(@Param("productId") Long productId);
}