package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.StockDTO;
import com.shoppingmall.dto.StockQueryDTO;
import com.shoppingmall.service.admin.StockService;
import com.shoppingmall.vo.StockStatisticsVO;
import com.shoppingmall.vo.StockVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理控制器（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("adminStockController")
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 分页查询库存列表
     */
    @GetMapping("/page")
    public Result<Page<StockVO>> getStockPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer productStatus,
            @RequestParam(required = false) Boolean onlyWarning
    ) {
        StockQueryDTO queryDTO = new StockQueryDTO();
        queryDTO.setProductId(productId);
        queryDTO.setProductCode(productCode);
        queryDTO.setProductName(productName);
        queryDTO.setProductStatus(productStatus);
        queryDTO.setOnlyWarning(onlyWarning);
        
        Page<StockVO> page = stockService.getStockPage(current, size, queryDTO);
        return Result.success("获取成功", page);
    }

    /**
     * 根据商品ID获取库存信息
     */
    @GetMapping("/product/{productId}")
    public Result<StockVO> getStockByProductId(@PathVariable Long productId) {
        StockVO stock = stockService.getStockByProductId(productId);
        return Result.success("获取成功", stock);
    }

    /**
     * 调整库存
     */
    @PostMapping("/adjust")
    public Result<?> adjustStock(@Valid @RequestBody StockDTO stockDTO) {
        stockService.adjustStock(stockDTO);
        return Result.success("库存调整成功");
    }

    /**
     * 更新预警阈值
     */
    @PutMapping("/warning-threshold")
    public Result<?> updateWarningThreshold(
            @RequestParam Long productId,
            @RequestParam Integer warningThreshold
    ) {
        stockService.updateWarningThreshold(productId, warningThreshold);
        return Result.success("预警阈值更新成功");
    }

    /**
     * 获取库存预警列表
     */
    @GetMapping("/warning/page")
    public Result<Page<StockVO>> getWarningStockPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size
    ) {
        Page<StockVO> page = stockService.getWarningStockPage(current, size);
        return Result.success("获取成功", page);
    }

    /**
     * 获取库存统计信息
     */
    @GetMapping("/statistics")
    public Result<StockStatisticsVO> getStockStatistics() {
        StockStatisticsVO statistics = stockService.getStockStatistics();
        return Result.success("获取成功", statistics);
    }
}


