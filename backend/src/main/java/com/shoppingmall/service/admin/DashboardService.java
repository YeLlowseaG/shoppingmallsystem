package com.shoppingmall.service.admin;

import com.shoppingmall.vo.DashboardVO;

/**
 * 数据看板服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
public interface DashboardService {

    /**
     * 获取数据看板统计信息
     *
     * @return 数据看板VO
     */
    DashboardVO getDashboardStatistics();
}










