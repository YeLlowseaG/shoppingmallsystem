package com.shoppingmall.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.StockNotificationDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.StockNotification;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.user.StockNotificationRepository;
import com.shoppingmall.service.user.StockNotificationService;
import com.shoppingmall.vo.StockNotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 缺货登记服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockNotificationServiceImpl implements StockNotificationService {

    private final StockNotificationRepository stockNotificationRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNotification(Long userId, StockNotificationDTO dto) {
        // 检查商品是否存在
        Product product = productRepository.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 检查商品是否有库存
        if (product.getStock() != null && product.getStock() > 0) {
            throw new BusinessException(400, "商品有库存，无需登记");
        }

        // 检查是否已经登记过
        LambdaQueryWrapper<StockNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockNotification::getUserId, userId)
                .eq(StockNotification::getProductId, dto.getProductId())
                .eq(StockNotification::getStatus, 0); // 待通知状态
        
        if (stockNotificationRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "您已登记过该商品，请勿重复登记");
        }

        // 创建登记记录
        StockNotification notification = new StockNotification();
        notification.setUserId(userId);
        notification.setProductId(dto.getProductId());
        notification.setProductName(product.getProductName());
        notification.setProductCode(product.getProductCode());
        notification.setMainImage(product.getMainImage());
        notification.setBasePrice(product.getBasePrice());
        notification.setContactPhone(dto.getContactPhone());
        notification.setContactEmail(dto.getContactEmail());
        notification.setNotifyType(StringUtil.isNotBlank(dto.getNotifyType()) ? dto.getNotifyType() : "email");
        notification.setRemark(dto.getRemark());
        notification.setStatus(0); // 待通知
        notification.setExpiredAt(LocalDateTime.now().plusDays(30)); // 30天后过期

        stockNotificationRepository.insert(notification);
        log.info("用户{}登记商品{}缺货通知", userId, dto.getProductId());
        
        return notification.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelNotification(Long userId, Long id) {
        StockNotification notification = stockNotificationRepository.selectById(id);
        if (notification == null) {
            throw new BusinessException(404, "登记记录不存在");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该登记记录");
        }

        notification.setStatus(2); // 已取消
        stockNotificationRepository.updateById(notification);
        
        log.info("用户{}取消登记记录{}", userId, id);
    }

    @Override
    public Page<StockNotificationVO> getUserNotifications(Long userId, Long current, Long size) {
        Page<StockNotification> page = new Page<>(current, size);
        
        LambdaQueryWrapper<StockNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockNotification::getUserId, userId)
                .orderByDesc(StockNotification::getCreateTime);
        
        Page<StockNotification> notificationPage = stockNotificationRepository.selectPage(page, wrapper);
        
        // 转换为VO
        Page<StockNotificationVO> voPage = new Page<>();
        voPage.setCurrent(notificationPage.getCurrent());
        voPage.setSize(notificationPage.getSize());
        voPage.setTotal(notificationPage.getTotal());
        voPage.setRecords(notificationPage.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }

    @Override
    public boolean hasRegistered(Long userId, Long productId) {
        LambdaQueryWrapper<StockNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockNotification::getUserId, userId)
                .eq(StockNotification::getProductId, productId)
                .eq(StockNotification::getStatus, 0); // 待通知状态
        
        return stockNotificationRepository.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyUsers(Long productId) {
        // 查找所有待通知的登记记录
        LambdaQueryWrapper<StockNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockNotification::getProductId, productId)
                .eq(StockNotification::getStatus, 0) // 待通知状态
                .gt(StockNotification::getExpiredAt, LocalDateTime.now()); // 未过期
        
        List<StockNotification> notifications = stockNotificationRepository.selectList(wrapper);
        
        if (!notifications.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            
            // 更新状态为已通知
            for (StockNotification notification : notifications) {
                notification.setStatus(1); // 已通知
                notification.setNotifiedAt(now);
                stockNotificationRepository.updateById(notification);
                
                // TODO: 发送实际的通知（邮件/短信）
                log.info("通知用户{}商品{}已补货", notification.getUserId(), productId);
            }
            
            log.info("商品{}补货通知已发送给{}位用户", productId, notifications.size());
        }
    }

    /**
     * 转换为VO
     */
    private StockNotificationVO convertToVO(StockNotification notification) {
        StockNotificationVO vo = new StockNotificationVO();
        BeanUtils.copyProperties(notification, vo);
        
        // 状态描述
        switch (notification.getStatus()) {
            case 0:
                vo.setStatusDesc("待通知");
                break;
            case 1:
                vo.setStatusDesc("已通知");
                break;
            case 2:
                vo.setStatusDesc("已取消");
                break;
            default:
                vo.setStatusDesc("未知");
        }
        
        return vo;
    }
}