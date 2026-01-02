package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.ProductSyncLog;
import com.shoppingmall.mapper.ProductSyncLogMapper;
import com.shoppingmall.service.erp.JushuitanItemService;
import com.shoppingmall.vo.ProductSyncLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品同步Controller（聚水潭ERP）
 *
 * @author ShoppingMall Team
 * @date 2025-12-31
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/erp/product")
public class ProductSyncController {

    @Resource
    private JushuitanItemService jushuitanItemService;

    @Resource
    private ProductSyncLogMapper productSyncLogMapper;

    /**
     * 同步单个商品到聚水潭
     *
     * @param productId 商品ID
     * @return 同步结果
     */
    @PostMapping("/sync/{productId}")
    public Result<String> syncProduct(@PathVariable Long productId) {
        log.info("手动同步商品到聚水潭: productId={}", productId);

        boolean success = jushuitanItemService.uploadItem(productId);

        if (success) {
            return Result.success("商品同步成功");
        } else {
            return Result.error("商品同步失败，请查看日志");
        }
    }

    /**
     * 批量同步商品到聚水潭
     *
     * @param productIds 商品ID列表（最多500个）
     * @return 同步结果
     */
    @PostMapping("/sync/batch")
    public Result<Map<String, Object>> batchSyncProducts(@RequestBody List<Long> productIds) {
        log.info("批量同步商品到聚水潭: count={}", productIds.size());

        if (productIds == null || productIds.isEmpty()) {
            return Result.error("商品列表不能为空");
        }

        if (productIds.size() > 500) {
            return Result.error("单次最多同步500个商品");
        }

        int successCount = jushuitanItemService.uploadItems(productIds);

        Map<String, Object> result = new HashMap<>();
        result.put("total", productIds.size());
        result.put("success", successCount);
        result.put("failed", productIds.size() - successCount);

        String message = String.format("批量同步完成，成功%d个，失败%d个",
            successCount, productIds.size() - successCount);

        return Result.success(message, result);
    }

    /**
     * 同步单个SKU到聚水潭
     *
     * @param skuId SKU ID
     * @return 同步结果
     */
    @PostMapping("/sync/sku/{skuId}")
    public Result<String> syncSku(@PathVariable Long skuId) {
        log.info("手动同步SKU到聚水潭: skuId={}", skuId);

        boolean success = jushuitanItemService.uploadSku(skuId);

        if (success) {
            return Result.success("SKU同步成功");
        } else {
            return Result.error("SKU同步失败，请查看日志");
        }
    }

    /**
     * 批量同步SKU到聚水潭
     *
     * @param skuIds SKU ID列表（最多500个）
     * @return 同步结果
     */
    @PostMapping("/sync/sku/batch")
    public Result<Map<String, Object>> batchSyncSkus(@RequestBody List<Long> skuIds) {
        log.info("批量同步SKU到聚水潭: count={}", skuIds.size());

        if (skuIds == null || skuIds.isEmpty()) {
            return Result.error("SKU列表不能为空");
        }

        if (skuIds.size() > 500) {
            return Result.error("单次最多同步500个SKU");
        }

        int successCount = jushuitanItemService.uploadSkus(skuIds);

        Map<String, Object> result = new HashMap<>();
        result.put("total", skuIds.size());
        result.put("success", successCount);
        result.put("failed", skuIds.size() - successCount);

        String message = String.format("批量同步完成，成功%d个，失败%d个",
            successCount, skuIds.size() - successCount);

        return Result.success(message, result);
    }

    /**
     * 分页查询商品同步日志
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param productId 商品ID（可选）
     * @param syncType 同步类型（可选）
     * @param syncStatus 同步状态（可选）
     * @param envType 环境类型（可选）
     * @return 同步日志列表
     */
    @GetMapping("/sync-log")
    public Result<Page<ProductSyncLogVO>> getSyncLogs(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String syncType,
            @RequestParam(required = false) Integer syncStatus,
            @RequestParam(required = false) String envType) {

        log.info("查询商品同步日志: pageNum={}, pageSize={}, productId={}, syncType={}, syncStatus={}, envType={}",
                pageNum, pageSize, productId, syncType, syncStatus, envType);

        // 构建查询条件
        QueryWrapper<ProductSyncLog> queryWrapper = new QueryWrapper<>();
        if (productId != null) {
            queryWrapper.eq("product_id", productId);
        }
        if (syncType != null && !syncType.isEmpty()) {
            queryWrapper.eq("sync_type", syncType);
        }
        if (syncStatus != null) {
            queryWrapper.eq("sync_status", syncStatus);
        }
        if (envType != null && !envType.isEmpty()) {
            queryWrapper.eq("env_type", envType);
        }
        queryWrapper.orderByDesc("create_time");

        // 分页查询
        Page<ProductSyncLog> page = new Page<>(pageNum, pageSize);
        Page<ProductSyncLog> resultPage = productSyncLogMapper.selectPage(page, queryWrapper);

        // 转换为VO
        Page<ProductSyncLogVO> voPage = new Page<>(pageNum, pageSize, resultPage.getTotal());
        List<ProductSyncLogVO> voList = resultPage.getRecords().stream().map(log -> {
            ProductSyncLogVO vo = new ProductSyncLogVO();
            BeanUtils.copyProperties(log, vo);

            // 设置同步类型描述
            if ("UPLOAD_ITEM".equals(log.getSyncType())) {
                vo.setSyncTypeDesc("上传商品");
            } else if ("UPDATE_ITEM".equals(log.getSyncType())) {
                vo.setSyncTypeDesc("更新商品");
            }

            // 设置同步状态描述
            if (log.getSyncStatus() != null) {
                switch (log.getSyncStatus()) {
                    case 0:
                        vo.setSyncStatusDesc("失败");
                        break;
                    case 1:
                        vo.setSyncStatusDesc("成功");
                        break;
                    case 2:
                        vo.setSyncStatusDesc("处理中");
                        break;
                    default:
                        vo.setSyncStatusDesc("未知");
                }
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);

        return Result.success("查询成功", voPage);
    }

    /**
     * 查询指定商品的同步日志
     *
     * @param productId 商品ID
     * @return 同步日志列表
     */
    @GetMapping("/sync-log/{productId}")
    public Result<List<ProductSyncLogVO>> getProductSyncLogs(@PathVariable Long productId) {
        log.info("查询商品同步日志: productId={}", productId);

        QueryWrapper<ProductSyncLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.orderByDesc("create_time");

        List<ProductSyncLog> logs = productSyncLogMapper.selectList(queryWrapper);

        // 转换为VO
        List<ProductSyncLogVO> voList = logs.stream().map(log -> {
            ProductSyncLogVO vo = new ProductSyncLogVO();
            BeanUtils.copyProperties(log, vo);

            // 设置同步类型描述
            if ("UPLOAD_ITEM".equals(log.getSyncType())) {
                vo.setSyncTypeDesc("上传商品");
            } else if ("UPDATE_ITEM".equals(log.getSyncType())) {
                vo.setSyncTypeDesc("更新商品");
            }

            // 设置同步状态描述
            if (log.getSyncStatus() != null) {
                switch (log.getSyncStatus()) {
                    case 0:
                        vo.setSyncStatusDesc("失败");
                        break;
                    case 1:
                        vo.setSyncStatusDesc("成功");
                        break;
                    case 2:
                        vo.setSyncStatusDesc("处理中");
                        break;
                    default:
                        vo.setSyncStatusDesc("未知");
                }
            }

            return vo;
        }).collect(Collectors.toList());

        return Result.success("查询成功", voList);
    }
}
