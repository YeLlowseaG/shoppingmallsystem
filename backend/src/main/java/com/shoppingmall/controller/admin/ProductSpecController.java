package com.shoppingmall.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ProductSpecKeyDTO;
import com.shoppingmall.service.sku.ProductSpecKeyService;
import com.shoppingmall.vo.ProductSpecKeyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理后台 - 商品规格管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/product-spec")
@RequiredArgsConstructor
@Validated
public class ProductSpecController {
    
    private final ProductSpecKeyService specKeyService;
    
    /**
     * 创建规格属性
     */
    @PostMapping
    public Result<Long> createSpecKey(@Valid @RequestBody ProductSpecKeyDTO dto) {
        try {
            Long specKeyId = specKeyService.createSpecKey(dto);
            return Result.success("规格属性创建成功", specKeyId);
        } catch (Exception e) {
            log.error("创建规格属性失败: {}", e.getMessage(), e);
            return Result.error("创建规格属性失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新规格属性
     */
    @PutMapping("/{id}")
    public Result<String> updateSpecKey(@PathVariable Long id, @Valid @RequestBody ProductSpecKeyDTO dto) {
        try {
            boolean success = specKeyService.updateSpecKey(id, dto);
            return success ? Result.success("规格属性更新成功") : Result.error("规格属性不存在");
        } catch (Exception e) {
            log.error("更新规格属性失败: {}", e.getMessage(), e);
            return Result.error("更新规格属性失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除规格属性
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteSpecKey(@PathVariable Long id) {
        try {
            boolean success = specKeyService.deleteSpecKey(id);
            return success ? Result.success("规格属性删除成功") : Result.error("规格属性不存在");
        } catch (Exception e) {
            log.error("删除规格属性失败: {}", e.getMessage(), e);
            return Result.error("删除规格属性失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据商品ID获取规格属性列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<ProductSpecKeyVO>> getSpecKeysByProductId(@PathVariable Long productId) {
        try {
            List<ProductSpecKeyVO> specKeys = specKeyService.getSpecKeysByProductId(productId);
            return Result.success(specKeys);
        } catch (Exception e) {
            log.error("查询商品规格属性失败: {}", e.getMessage(), e);
            return Result.error("查询规格属性失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取规格属性详情
     */
    @GetMapping("/{id}")
    public Result<ProductSpecKeyVO> getSpecKeyById(@PathVariable Long id) {
        try {
            ProductSpecKeyVO specKey = specKeyService.getSpecKeyById(id);
            return specKey != null ? Result.success(specKey) : Result.error("规格属性不存在");
        } catch (Exception e) {
            log.error("查询规格属性详情失败: {}", e.getMessage(), e);
            return Result.error("查询规格属性详情失败：" + e.getMessage());
        }
    }
}