package com.shoppingmall.controller.buyer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ProductReviewDTO;
import com.shoppingmall.service.review.ProductReviewService;
import com.shoppingmall.vo.ProductReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 买家端商品评价控制器
 */
@Tag(name = "买家端评价管理")
@RestController
@RequestMapping("/api/buyer/review")
@RequiredArgsConstructor
public class BuyerProductReviewController {
    
    private final ProductReviewService reviewService;
    
    @Operation(summary = "提交商品评价")
    @PostMapping
    public Result<Void> submitReview(
            @Valid @RequestBody ProductReviewDTO reviewDTO,
            @RequestAttribute("userId") Long userId) {
        
        reviewService.submitReview(reviewDTO, userId);
        return Result.success();
    }
    
    @Operation(summary = "获取我的评价列表")
    @GetMapping("/my")
    public Result<Page<ProductReviewVO>> getMyReviews(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") Long userId) {
        
        Page<ProductReviewVO> page = reviewService.getUserReviews(current, size, userId);
        return Result.success(page);
    }
    
    @Operation(summary = "获取评价详情")
    @GetMapping("/{reviewId}")
    public Result<ProductReviewVO> getReviewById(
            @Parameter(description = "评价ID") @PathVariable Long reviewId,
            @RequestAttribute("userId") Long userId) {
        
        ProductReviewVO review = reviewService.getReviewById(reviewId);
        
        // 验证评价是否属于当前用户
        if (!review.getUserId().equals(userId)) {
            return Result.error("无权访问该评价");
        }
        
        return Result.success(review);
    }
    
    @Operation(summary = "检查是否可以评价")
    @GetMapping("/can-review")
    public Result<Boolean> canReviewProduct(
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @RequestAttribute("userId") Long userId) {
        
        boolean canReview = reviewService.canReviewProduct(orderId, productId, userId);
        return Result.success(canReview);
    }
}