package com.shoppingmall.service.review.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.ProductReviewDTO;
import com.shoppingmall.dto.ReviewAuditDTO;
import com.shoppingmall.dto.ReviewReplyDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductReview;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.review.ProductReviewRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.review.ProductReviewService;
import com.shoppingmall.vo.ProductReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品评价服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
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
        // 构建查询条件
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();

        if (rating != null) {
            wrapper.eq(ProductReview::getRating, rating);
        }
        if (status != null) {
            wrapper.eq(ProductReview::getStatus, status);
        }

        wrapper.orderByDesc(ProductReview::getCreatedTime);

        // 查询评价列表
        Page<ProductReview> reviewPage = reviewRepository.selectPage(new Page<>(current, size), wrapper);

        // 转换为VO
        return convertToVOPage(reviewPage);
    }
    
    @Override
    public Page<ProductReviewVO> getUserReviews(int current, int size, Long userId) {
        // 查询用户的评价列表
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getUserId, userId)
               .orderByDesc(ProductReview::getCreatedTime);

        Page<ProductReview> reviewPage = reviewRepository.selectPage(new Page<>(current, size), wrapper);

        // 转换为VO
        return convertToVOPage(reviewPage);
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

    /**
     * 转换Page<ProductReview>为Page<ProductReviewVO>
     */
    private Page<ProductReviewVO> convertToVOPage(Page<ProductReview> reviewPage) {
        Page<ProductReviewVO> voPage = new Page<>(reviewPage.getCurrent(),
                                                   reviewPage.getSize(),
                                                   reviewPage.getTotal());

        List<ProductReviewVO> voList = new ArrayList<>();
        for (ProductReview review : reviewPage.getRecords()) {
            voList.add(convertToVO(review));
        }
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 转换ProductReview为ProductReviewVO
     */
    private ProductReviewVO convertToVO(ProductReview review) {
        ProductReviewVO vo = new ProductReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 获取商品信息
        if (review.getProductId() != null) {
            Product product = productRepository.selectById(review.getProductId());
            if (product != null) {
                vo.setProductName(product.getProductName());
                vo.setProductImage(product.getMainImage());
            }
        }

        // 获取用户信息
        if (review.getUserId() != null) {
            User user = userRepository.selectById(review.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
            }
        }

        // 获取订单信息
        if (review.getOrderId() != null) {
            Order order = orderRepository.selectById(review.getOrderId());
            if (order != null) {
                vo.setOrderNumber(order.getOrderNo());
            }
        }

        // 设置状态文本
        vo.setStatusText(getStatusText(review.getStatus()));

        return vo;
    }
}