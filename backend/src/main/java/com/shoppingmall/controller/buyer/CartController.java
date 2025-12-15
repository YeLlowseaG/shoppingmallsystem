package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.CartDTO;
import com.shoppingmall.service.buyer.CartService;
import com.shoppingmall.vo.CartVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@RestController
@RequestMapping("/api/buyer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final HttpServletRequest request;

    /**
     * 获取购物车列表
     */
    @GetMapping
    public Result<List<CartVO>> getCartList() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        List<CartVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public Result<Long> addToCart(@Valid @RequestBody CartDTO cartDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        Long cartId = cartService.addToCart(userId, cartDTO);
        return Result.success("添加成功", cartId);
    }

    /**
     * 通过货号添加商品到购物车
     */
    @PostMapping("/add-by-code")
    public Result<Long> addToCartByCode(@RequestParam String productCode, @RequestParam Integer quantity) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        if (quantity == null || quantity <= 0) {
            return Result.error(400, "数量必须大于0");
        }
        Long cartId = cartService.addToCartByCode(userId, productCode, quantity);
        return Result.success("添加成功", cartId);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{id}")
    public Result<?> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        cartService.updateQuantity(id, userId, quantity);
        return Result.success("更新成功");
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteCartItem(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        cartService.deleteCartItem(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除购物车商品
     */
    @DeleteMapping("/batch")
    public Result<?> batchDeleteCartItems(@RequestBody List<Long> ids) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        cartService.batchDeleteCartItems(ids, userId);
        return Result.success("删除成功");
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<?> clearCart() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        cartService.clearCart(userId);
        return Result.success("清空成功");
    }

    /**
     * 获取购物车商品数量统计
     */
    @GetMapping("/count")
    public Result<Integer> getCartItemCount() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        Integer count = cartService.getCartItemCount(userId);
        return Result.success(count);
    }
}





























