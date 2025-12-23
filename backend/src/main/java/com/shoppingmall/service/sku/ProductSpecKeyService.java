package com.shoppingmall.service.sku;

import com.shoppingmall.dto.ProductSpecKeyDTO;
import com.shoppingmall.vo.ProductSpecKeyVO;

import java.util.List;

/**
 * 商品规格属性Service接口
 */
public interface ProductSpecKeyService {
    
    /**
     * 创建规格属性
     * @param dto 规格属性DTO
     * @return 规格属性ID
     */
    Long createSpecKey(ProductSpecKeyDTO dto);
    
    /**
     * 更新规格属性
     * @param id 规格属性ID
     * @param dto 规格属性DTO
     * @return 是否成功
     */
    boolean updateSpecKey(Long id, ProductSpecKeyDTO dto);
    
    /**
     * 删除规格属性
     * @param id 规格属性ID
     * @return 是否成功
     */
    boolean deleteSpecKey(Long id);
    
    /**
     * 根据商品ID获取规格属性列表
     * @param productId 商品ID
     * @return 规格属性列表
     */
    List<ProductSpecKeyVO> getSpecKeysByProductId(Long productId);
    
    /**
     * 根据ID获取规格属性详情
     * @param id 规格属性ID
     * @return 规格属性详情
     */
    ProductSpecKeyVO getSpecKeyById(Long id);

    /**
     * 根据商品ID删除所有规格属性和规格值
     * @param productId 商品ID
     * @return 删除的规格属性数量
     */
    int deleteSpecsByProductId(Long productId);
}