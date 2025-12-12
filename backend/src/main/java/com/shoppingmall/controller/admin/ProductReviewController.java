package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ReviewAuditDTO;
import com.shoppingmall.dto.ReviewReplyDTO;
import com.shoppingmall.service.review.ProductReviewService;
import com.shoppingmall.vo.ProductReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端商品评价控制器
 */
@Tag(name = "管理端评价管理")
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class ProductReviewController {
    
    private final ProductReviewService reviewService;
    
    @Operation(summary = "分页获取评价列表")
    @GetMapping("/page")
    public Result<Page<ProductReviewVO>> getReviewPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "商品名称") @RequestParam(required = false) String productName,
            @Parameter(description = "用户名称") @RequestParam(required = false) String userName,
            @Parameter(description = "评分") @RequestParam(required = false) Integer rating,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<ProductReviewVO> page = reviewService.getReviewPage(
                current, size, productName, userName, rating, status);
        return Result.success(page);
    }
    
    @Operation(summary = "获取评价详情")
    @GetMapping("/{reviewId}")
    public Result<ProductReviewVO> getReviewById(
            @Parameter(description = "评价ID") @PathVariable Long reviewId) {
        
        ProductReviewVO review = reviewService.getReviewById(reviewId);
        return Result.success(review);
    }
    
    @Operation(summary = "审核评价")
    @PutMapping("/{reviewId}/audit")
    public Result<Void> auditReview(
            @Parameter(description = "评价ID") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewAuditDTO auditDTO) {
        
        reviewService.auditReview(reviewId, auditDTO);
        return Result.success();
    }
    
    @Operation(summary = "回复评价")
    @PutMapping("/{reviewId}/reply")
    public Result<Void> replyReview(
            @Parameter(description = "评价ID") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReplyDTO replyDTO) {
        
        reviewService.replyReview(reviewId, replyDTO);
        return Result.success();
    }
    
    @Operation(summary = "删除评价")
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(
            @Parameter(description = "评价ID") @PathVariable Long reviewId) {
        
        reviewService.deleteReview(reviewId);
        return Result.success();
    }
}