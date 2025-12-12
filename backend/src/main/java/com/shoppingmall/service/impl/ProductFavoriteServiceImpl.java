package com.shoppingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.FavoriteDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductFavorite;
import com.shoppingmall.repository.ProductFavoriteRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.service.ProductFavoriteService;
import com.shoppingmall.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 商品收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductFavoriteServiceImpl implements ProductFavoriteService {
    
    private final ProductFavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    
    @Override
    public void addFavorite(FavoriteDTO favoriteDTO, Long userId) {
        // 检查商品是否存在
        Product product = productRepository.selectById(favoriteDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 检查是否已收藏
        LambdaQueryWrapper<ProductFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductFavorite::getUserId, userId)
               .eq(ProductFavorite::getProductId, favoriteDTO.getProductId());
        
        ProductFavorite existingFavorite = favoriteRepository.selectOne(wrapper);
        if (existingFavorite != null) {
            throw new BusinessException("商品已在收藏列表中");
        }
        
        // 添加收藏
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(favoriteDTO.getProductId());
        favoriteRepository.insert(favorite);
    }
    
    @Override
    public void removeFavorite(Long productId, Long userId) {
        LambdaQueryWrapper<ProductFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductFavorite::getUserId, userId)
               .eq(ProductFavorite::getProductId, productId);
        
        int deleted = favoriteRepository.delete(wrapper);
        if (deleted == 0) {
            throw new BusinessException("收藏记录不存在");
        }
    }
    
    @Override
    public boolean isFavorited(Long productId, Long userId) {
        LambdaQueryWrapper<ProductFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductFavorite::getUserId, userId)
               .eq(ProductFavorite::getProductId, productId);
        
        return favoriteRepository.selectCount(wrapper) > 0;
    }
    
    @Override
    public Page<FavoriteVO> getUserFavorites(int current, int size, Long userId) {
        Page<FavoriteVO> page = new Page<>(current, size);
        return favoriteRepository.selectUserFavoritesPage(page, userId);
    }
}