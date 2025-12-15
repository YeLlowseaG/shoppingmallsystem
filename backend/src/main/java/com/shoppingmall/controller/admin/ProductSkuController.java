package com.shoppingmall.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ProductSkuDTO;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.vo.ProductSkuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理后台 - 商品SKU管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/product-sku")
@RequiredArgsConstructor
@Validated
public class ProductSkuController {
    
    private final ProductSkuService skuService;
    
    /**
     * 创建SKU
     */
    @PostMapping
    public Result<Long> createSku(@Valid @RequestBody ProductSkuDTO dto) {
        try {
            Long skuId = skuService.createSku(dto);
            return Result.success("SKU创建成功", skuId);
        } catch (Exception e) {
            log.error("创建SKU失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量创建SKU
     */
    @PostMapping("/batch")
    public Result<Integer> batchCreateSkus(@Valid @RequestBody List<ProductSkuDTO> dtoList) {
        try {
            int successCount = skuService.batchCreateSkus(dtoList);
            return Result.success("批量创建SKU完成，成功创建" + successCount + "个SKU", successCount);
        } catch (Exception e) {
            log.error("批量创建SKU失败: {}", e.getMessage(), e);
            return Result.error("批量创建SKU失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新SKU
     */
    @PutMapping("/{id}")
    public Result<String> updateSku(@PathVariable Long id, @Valid @RequestBody ProductSkuDTO dto) {
        try {
            boolean success = skuService.updateSku(id, dto);
            return success ? Result.success("SKU更新成功") : Result.error("SKU不存在");
        } catch (Exception e) {
            log.error("更新SKU失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除SKU
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteSku(@PathVariable Long id) {
        try {
            boolean success = skuService.deleteSku(id);
            return success ? Result.success("SKU删除成功") : Result.error("SKU不存在");
        } catch (Exception e) {
            log.error("删除SKU失败: {}", e.getMessage(), e);
            return Result.error("删除SKU失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据商品ID获取SKU列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<ProductSkuVO>> getSkusByProductId(@PathVariable Long productId) {
        try {
            List<ProductSkuVO> skus = skuService.getSkusByProductId(productId, null); // 管理员端不需要计算会员价，传递null
            return Result.success(skus);
        } catch (Exception e) {
            log.error("查询商品SKU列表失败: {}", e.getMessage(), e);
            return Result.error("查询SKU列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取SKU详情
     */
    @GetMapping("/{id}")
    public Result<ProductSkuVO> getSkuById(@PathVariable Long id) {
        try {
            ProductSkuVO sku = skuService.getSkuById(id);
            return sku != null ? Result.success(sku) : Result.error("SKU不存在");
        } catch (Exception e) {
            log.error("查询SKU详情失败: {}", e.getMessage(), e);
            return Result.error("查询SKU详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据SKU编码获取SKU详情
     */
    @GetMapping("/code/{skuCode}")
    public Result<ProductSkuVO> getSkuByCode(@PathVariable String skuCode) {
        try {
            ProductSkuVO sku = skuService.getSkuByCode(skuCode);
            return sku != null ? Result.success(sku) : Result.error("SKU不存在");
        } catch (Exception e) {
            log.error("根据编码查询SKU失败: {}", e.getMessage(), e);
            return Result.error("查询SKU失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新SKU库存
     */
    @PutMapping("/{id}/stock")
    public Result<String> updateSkuStock(@PathVariable Long id, @RequestParam Integer stock) {
        try {
            if (stock < 0) {
                return Result.error("库存数量不能为负数");
            }
            boolean success = skuService.updateSkuStock(id, stock);
            return success ? Result.success("库存更新成功") : Result.error("SKU不存在");
        } catch (Exception e) {
            log.error("更新SKU库存失败: {}", e.getMessage(), e);
            return Result.error("更新库存失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取库存警戒的SKU列表
     */
    @GetMapping("/low-stock")
    public Result<List<ProductSkuVO>> getLowStockSkus() {
        try {
            List<ProductSkuVO> skus = skuService.getLowStockSkus();
            return Result.success(skus);
        } catch (Exception e) {
            log.error("查询低库存SKU失败: {}", e.getMessage(), e);
            return Result.error("查询低库存SKU失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查SKU编码是否存在
     */
    @GetMapping("/check-code")
    public Result<Boolean> checkSkuCode(@RequestParam String skuCode) {
        try {
            boolean exists = skuService.existsBySkuCode(skuCode);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查SKU编码失败: {}", e.getMessage(), e);
            return Result.error("检查SKU编码失败：" + e.getMessage());
        }
    }
}