package com.shoppingmall.service.sku;

import com.shoppingmall.dto.ProductSkuDTO;
import com.shoppingmall.vo.ProductSkuVO;

import java.util.List;

/**
 * 商品SKU Service接口
 */
public interface ProductSkuService {
    
    /**
     * 创建SKU
     * @param dto SKU DTO
     * @return SKU ID
     */
    Long createSku(ProductSkuDTO dto);
    
    /**
     * 批量创建SKU
     * @param dtoList SKU DTO列表
     * @return 创建的SKU数量
     */
    int batchCreateSkus(List<ProductSkuDTO> dtoList);
    
    /**
     * 更新SKU
     * @param id SKU ID
     * @param dto SKU DTO
     * @return 是否成功
     */
    boolean updateSku(Long id, ProductSkuDTO dto);
    
    /**
     * 删除SKU
     * @param id SKU ID
     * @return 是否成功
     */
    boolean deleteSku(Long id);
    
    /**
     * 根据商品ID获取SKU列表
     * @param productId 商品ID
     * @return SKU列表
     */
    List<ProductSkuVO> getSkusByProductId(Long productId);
    
    /**
     * 根据ID获取SKU详情
     * @param id SKU ID
     * @return SKU详情
     */
    ProductSkuVO getSkuById(Long id);
    
    /**
     * 根据SKU编码获取SKU详情
     * @param skuCode SKU编码
     * @return SKU详情
     */
    ProductSkuVO getSkuByCode(String skuCode);
    
    /**
     * 根据商品ID和规格组合获取SKU
     * @param productId 商品ID
     * @param specCombination 规格组合JSON
     * @return SKU详情
     */
    ProductSkuVO getSkuBySpecCombination(Long productId, String specCombination);
    
    /**
     * 更新SKU库存
     * @param skuId SKU ID
     * @param stock 库存数量
     * @return 是否成功
     */
    boolean updateSkuStock(Long skuId, Integer stock);
    
    /**
     * 增加SKU销量
     * @param skuId SKU ID
     * @param salesCount 销量增加数
     * @return 是否成功
     */
    boolean increaseSalesCount(Long skuId, Integer salesCount);
    
    /**
     * 获取库存警戒的SKU列表
     * @return 库存警戒的SKU列表
     */
    List<ProductSkuVO> getLowStockSkus();
    
    /**
     * 检查SKU编码是否存在
     * @param skuCode SKU编码
     * @return 是否存在
     */
    boolean existsBySkuCode(String skuCode);
}