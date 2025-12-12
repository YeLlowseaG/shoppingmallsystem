package com.shoppingmall.repository.review;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品评价Repository
 */
@Mapper
public interface ProductReviewRepository extends BaseMapper<ProductReview> {
    
    /**
     * 分页查询评价记录（管理端）
     */
    Page<ProductReview> selectPageWithProductAndUser(Page<ProductReview> page, 
                                                   @Param("productName") String productName,
                                                   @Param("userName") String userName,
                                                   @Param("rating") Integer rating,
                                                   @Param("status") Integer status);
    
    /**
     * 分页查询用户的评价记录
     */
    Page<ProductReview> selectUserReviews(Page<ProductReview> page, 
                                        @Param("userId") Long userId);
    
    /**
     * 检查订单是否已评价
     */
    int checkOrderReviewed(@Param("orderId") Long orderId, 
                          @Param("productId") Long productId);
}