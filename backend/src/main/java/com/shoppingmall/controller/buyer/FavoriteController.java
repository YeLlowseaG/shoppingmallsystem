package com.shoppingmall.controller.buyer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.FavoriteDTO;
import com.shoppingmall.service.ProductFavoriteService;
import com.shoppingmall.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 商品收藏控制器
 */
@RestController
@RequestMapping("/api/buyer/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final ProductFavoriteService favoriteService;
    
    /**
     * 添加收藏
     */
    @PostMapping
    public Result<String> addFavorite(@Valid @RequestBody FavoriteDTO favoriteDTO) {
        // TODO: 从JWT token中获取用户ID
        Long userId = 1L; // 临时硬编码，实际应从认证信息中获取
        favoriteService.addFavorite(favoriteDTO, userId);
        return Result.success("收藏成功");
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/{productId}")
    public Result<String> removeFavorite(@PathVariable Long productId) {
        // TODO: 从JWT token中获取用户ID
        Long userId = 1L; // 临时硬编码，实际应从认证信息中获取
        favoriteService.removeFavorite(productId, userId);
        return Result.success("取消收藏成功");
    }
    
    /**
     * 检查是否已收藏
     * 支持未登录用户访问，未登录时返回false
     */
    @GetMapping("/check/{productId}")
    public Result<Boolean> checkFavorite(@PathVariable Long productId, jakarta.servlet.http.HttpServletRequest request) {
        // 从request中获取用户ID，如果未登录则为null
        Long userId = (Long) request.getAttribute("userId");
        // 未登录用户返回false
        if (userId == null) {
            return Result.success("检查成功", false);
        }
        boolean isFavorited = favoriteService.isFavorited(productId, userId);
        return Result.success("检查成功", isFavorited);
    }
    
    /**
     * 分页查询用户收藏
     */
    @GetMapping("/page")
    public Result<Page<FavoriteVO>> getFavoritePage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 从JWT token中获取用户ID
        Long userId = 1L; // 临时硬编码，实际应从认证信息中获取
        Page<FavoriteVO> page = favoriteService.getUserFavorites(current, size, userId);
        return Result.success("获取成功", page);
    }
}