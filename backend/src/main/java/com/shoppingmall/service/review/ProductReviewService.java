package com.shoppingmall.service.review;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.ProductReviewDTO;
import com.shoppingmall.dto.ReviewAuditDTO;
import com.shoppingmall.dto.ReviewReplyDTO;
import com.shoppingmall.entity.ProductReview;
import com.shoppingmall.vo.ProductReviewVO;

/**
 * 商品评价服务接口
 */
public interface ProductReviewService {
    
    /**
     * 提交商品评价
     */
    void submitReview(ProductReviewDTO reviewDTO, Long userId);
    
    /**
     * 管理端分页查询评价
     */
    Page<ProductReviewVO> getReviewPage(int current, int size, 
                                      String productName, String userName, 
                                      Integer rating, Integer status);
    
    /**
     * 用户分页查询自己的评价
     */
    Page<ProductReviewVO> getUserReviews(int current, int size, Long userId);
    
    /**
     * 审核评价
     */
    void auditReview(Long reviewId, ReviewAuditDTO auditDTO);
    
    /**
     * 回复评价
     */
    void replyReview(Long reviewId, ReviewReplyDTO replyDTO);
    
    /**
     * 根据ID获取评价详情
     */
    ProductReviewVO getReviewById(Long reviewId);
    
    /**
     * 检查订单商品是否可以评价
     */
    boolean canReviewProduct(Long orderId, Long productId, Long userId);
    
    /**
     * 删除评价
     */
    void deleteReview(Long reviewId);
}