package com.shoppingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.FavoriteDTO;
import com.shoppingmall.vo.FavoriteVO;

/**
 * 商品收藏服务接口
 */
public interface ProductFavoriteService {
    
    /**
     * 添加收藏
     */
    void addFavorite(FavoriteDTO favoriteDTO, Long userId);
    
    /**
     * 取消收藏
     */
    void removeFavorite(Long productId, Long userId);
    
    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long productId, Long userId);
    
    /**
     * 分页查询用户收藏
     */
    Page<FavoriteVO> getUserFavorites(int current, int size, Long userId);
}