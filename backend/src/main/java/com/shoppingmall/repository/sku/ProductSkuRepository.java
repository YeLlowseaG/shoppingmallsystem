package com.shoppingmall.repository.sku;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品SKU Repository
 */
@Mapper
public interface ProductSkuRepository extends BaseMapper<ProductSku> {
    
    /**
     * 根据商品ID查询SKU列表
     * @param productId 商品ID
     * @return SKU列表
     */
    List<ProductSku> findByProductId(@Param("productId") Long productId);
    
    /**
     * 根据SKU编码查询SKU
     * @param skuCode SKU编码
     * @return SKU信息
     */
    ProductSku findBySkuCode(@Param("skuCode") String skuCode);
    
    /**
     * 根据商品ID和规格组合查询SKU
     * @param productId 商品ID
     * @param specCombination 规格组合JSON
     * @return SKU信息
     */
    ProductSku findByProductIdAndSpecCombination(@Param("productId") Long productId, 
                                                @Param("specCombination") String specCombination);
    
    /**
     * 根据商品ID删除SKU
     * @param productId 商品ID
     * @return 删除的记录数
     */
    int deleteByProductId(@Param("productId") Long productId);
    
    /**
     * 更新SKU库存
     * @param skuId SKU ID
     * @param stock 库存数量
     * @return 更新的记录数
     */
    int updateStock(@Param("skuId") Long skuId, @Param("stock") Integer stock);
    
    /**
     * 增加SKU销量
     * @param skuId SKU ID
     * @param salesCount 销量增加数
     * @return 更新的记录数
     */
    int increaseSalesCount(@Param("skuId") Long skuId, @Param("salesCount") Integer salesCount);
    
    /**
     * 查询库存警戒的SKU
     * @return 库存警戒的SKU列表
     */
    List<ProductSku> findLowStockSkus();
}