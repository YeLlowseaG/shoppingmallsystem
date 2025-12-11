package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.StockDTO;
import com.shoppingmall.dto.StockQueryDTO;
import com.shoppingmall.vo.StockStatisticsVO;
import com.shoppingmall.vo.StockVO;

/**
 * 库存管理服务接口（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface StockService {

    /**
     * 分页查询库存列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param queryDTO 查询条件
     * @return 库存分页列表
     */
    Page<StockVO> getStockPage(Long current, Long size, StockQueryDTO queryDTO);

    /**
     * 根据商品ID获取库存信息
     *
     * @param productId 商品ID
     * @return 库存信息
     */
    StockVO getStockByProductId(Long productId);

    /**
     * 调整库存
     *
     * @param stockDTO 库存调整信息
     */
    void adjustStock(StockDTO stockDTO);

    /**
     * 更新预警阈值
     *
     * @param productId 商品ID
     * @param warningThreshold 预警阈值
     */
    void updateWarningThreshold(Long productId, Integer warningThreshold);

    /**
     * 获取库存预警列表（可用库存 <= 预警阈值）
     *
     * @param current 当前页
     * @param size 每页大小
     * @return 库存预警分页列表
     */
    Page<StockVO> getWarningStockPage(Long current, Long size);

    /**
     * 获取库存统计信息
     *
     * @return 库存统计信息
     */
    StockStatisticsVO getStockStatistics();
}


