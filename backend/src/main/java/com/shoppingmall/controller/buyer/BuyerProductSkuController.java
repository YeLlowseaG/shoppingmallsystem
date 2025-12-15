package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.service.sku.ProductSpecKeyService;
import com.shoppingmall.vo.ProductSkuVO;
import com.shoppingmall.vo.ProductSpecKeyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 买家端 - 商品SKU Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/buyer/product")
@RequiredArgsConstructor
public class BuyerProductSkuController {
    
    private final ProductSkuService skuService;
    private final ProductSpecKeyService specKeyService;
    
    /**
     * 根据商品ID获取SKU列表
     */
    @GetMapping("/{productId}/skus")
    public Result<List<ProductSkuVO>> getSkusByProductId(
            @PathVariable Long productId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        try {
            List<ProductSkuVO> skus = skuService.getSkusByProductId(productId, userId);
            // 只返回启用状态的SKU（包括库存为0的，方便用户看到所有规格）
            List<ProductSkuVO> availableSkus = skus.stream()
                    .filter(sku -> sku.getStatus() == 1)
                    .toList();
            return Result.success(availableSkus);
        } catch (Exception e) {
            log.error("查询商品SKU列表失败: {}", e.getMessage(), e);
            return Result.error("查询商品SKU失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据商品ID获取规格属性列表
     */
    @GetMapping("/{productId}/specs")
    public Result<List<ProductSpecKeyVO>> getSpecKeysByProductId(@PathVariable Long productId) {
        try {
            List<ProductSpecKeyVO> specKeys = specKeyService.getSpecKeysByProductId(productId);
            return Result.success(specKeys);
        } catch (Exception e) {
            log.error("查询商品规格属性失败: {}", e.getMessage(), e);
            return Result.error("查询商品规格属性失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据商品ID和规格组合获取SKU
     */
    @GetMapping("/{productId}/sku")
    public Result<ProductSkuVO> getSkuBySpecCombination(
            @PathVariable Long productId, 
            @RequestParam String specCombination,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        try {
            ProductSkuVO sku = skuService.getSkuBySpecCombination(productId, specCombination, userId);
            if (sku == null) {
                return Result.success(null);
            }
            
            // 检查SKU是否可用
            if (sku.getStatus() == 0 || sku.getStock() <= 0) {
                return Result.success(null);
            }
            
            return Result.success(sku);
        } catch (Exception e) {
            log.error("根据规格组合查询SKU失败: {}", e.getMessage(), e);
            return Result.error("查询SKU失败：" + e.getMessage());
        }
    }
}