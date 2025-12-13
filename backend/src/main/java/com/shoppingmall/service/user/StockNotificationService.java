package com.shoppingmall.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.StockNotificationDTO;
import com.shoppingmall.vo.StockNotificationVO;

/**
 * 缺货登记服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
public interface StockNotificationService {

    /**
     * 创建缺货登记
     */
    Long createNotification(Long userId, StockNotificationDTO dto);

    /**
     * 取消缺货登记
     */
    void cancelNotification(Long userId, Long id);

    /**
     * 获取用户的缺货登记列表（分页）
     */
    Page<StockNotificationVO> getUserNotifications(Long userId, Long current, Long size);

    /**
     * 检查用户是否已登记某商品
     */
    boolean hasRegistered(Long userId, Long productId);

    /**
     * 商品补货时通知登记用户
     */
    void notifyUsers(Long productId);
}