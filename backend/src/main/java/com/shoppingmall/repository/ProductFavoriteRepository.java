package com.shoppingmall.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.ProductFavorite;
import com.shoppingmall.vo.FavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品收藏Repository
 */
@Mapper
public interface ProductFavoriteRepository extends BaseMapper<ProductFavorite> {
    
    /**
     * 分页查询用户收藏的商品
     */
    Page<FavoriteVO> selectUserFavoritesPage(Page<FavoriteVO> page, @Param("userId") Long userId);
}