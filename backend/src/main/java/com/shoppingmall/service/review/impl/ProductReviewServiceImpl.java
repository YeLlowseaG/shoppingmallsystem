package com.shoppingmall.service.review.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.ProductReviewDTO;
import com.shoppingmall.dto.ReviewAuditDTO;
import com.shoppingmall.dto.ReviewReplyDTO;
import com.shoppingmall.entity.ProductReview;
import com.shoppingmall.repository.review.ProductReviewRepository;
import com.shoppingmall.service.review.ProductReviewService;
import com.shoppingmall.vo.ProductReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 商品评价服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {
    
    private final ProductReviewRepository reviewRepository;
    
    @Override
    public void submitReview(ProductReviewDTO reviewDTO, Long userId) {
        ProductReview review = new ProductReview();
        BeanUtils.copyProperties(reviewDTO, review);
        review.setUserId(userId);
        review.setStatus(0); // 待审核
        reviewRepository.insert(review);
    }
    
    @Override
    public Page<ProductReviewVO> getReviewPage(int current, int size, 
                                             String productName, String userName, 
                                             Integer rating, Integer status) {
        // 简化实现，返回空页面
        return new Page<>(current, size, 0);
    }
    
    @Override
    public Page<ProductReviewVO> getUserReviews(int current, int size, Long userId) {
        // 简化实现，返回空页面
        return new Page<>(current, size, 0);
    }
    
    @Override
    public void auditReview(Long reviewId, ReviewAuditDTO auditDTO) {
        ProductReview review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setStatus(auditDTO.getStatus());
        reviewRepository.updateById(review);
    }
    
    @Override
    public void replyReview(Long reviewId, ReviewReplyDTO replyDTO) {
        ProductReview review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setAdminReply(replyDTO.getAdminReply());
        reviewRepository.updateById(review);
    }
    
    @Override
    public ProductReviewVO getReviewById(Long reviewId) {
        ProductReview review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        
        ProductReviewVO vo = new ProductReviewVO();
        BeanUtils.copyProperties(review, vo);
        vo.setStatusText(getStatusText(review.getStatus()));
        return vo;
    }
    
    @Override
    public boolean canReviewProduct(Long orderId, Long productId, Long userId) {
        // 简化实现，总是返回true
        return true;
    }
    
    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
    
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已拒绝";
            default: return "未知";
        }
    }
}