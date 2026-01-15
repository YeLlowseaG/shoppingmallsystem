package com.shoppingmall.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.erp.JushuitanInventoryService;
import com.shoppingmall.vo.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存同步Controller（聚水潭ERP）
 *
 * @author ShoppingMall Team
 * @date 2026-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/inventory/sync")
@RequiredArgsConstructor
public class InventorySyncController {

    private final JushuitanInventoryService jushuitanInventoryService;

    /**
     * 同步单个商品库存到聚水潭ERP
     *
     * @param productId 商品ID
     * @return 同步结果
     */
    @PostMapping("/{productId}")
    public Result<Boolean> syncInventory(@PathVariable Long productId) {
        log.info("手动同步商品库存到聚水潭ERP，商品ID: {}", productId);
        boolean success = jushuitanInventoryService.syncInventory(productId);
        if (success) {
            return Result.success("库存同步成功", true);
        } else {
            return Result.error("库存同步失败");
        }
    }

    /**
     * 批量同步商品库存到聚水潭ERP
     *
     * @param productIds 商品ID列表
     * @return 同步结果
     */
    @PostMapping("/batch")
    public Result<Integer> syncInventoryBatch(@RequestBody List<Long> productIds) {
        log.info("批量同步商品库存到聚水潭ERP，商品数量: {}", productIds.size());
        int successCount = jushuitanInventoryService.syncInventories(productIds);
        return Result.success(String.format("成功同步%d个商品库存", successCount), successCount);
    }

    /**
     * 完整同步商品资料和库存到聚水潭ERP（先同步商品资料，成功后再同步库存，带重试机制）
     *
     * @param productId 商品ID
     * @return 同步结果
     */
    @PostMapping("/{productId}/full")
    public Result<SyncResult> syncProductAndInventory(@PathVariable Long productId) {
        log.info("完整同步商品和库存到聚水潭ERP，商品ID: {}", productId);
        
        SyncResult result = jushuitanInventoryService.syncProductAndInventory(productId);
        
        if (result.isSuccess()) {
            return Result.success("商品资料和库存同步成功", result);
        } else {
            // 根据失败步骤返回不同的错误信息
            String errorMsg = result.getMessage();
            if (result.getErrorDetail() != null && !result.getErrorDetail().isEmpty()) {
                errorMsg += " 错误详情: " + result.getErrorDetail();
            }
            // 使用 error(Integer code, String message, T data) 方法，返回错误信息和结果数据
            return Result.error(500, errorMsg, result);
        }
    }
}