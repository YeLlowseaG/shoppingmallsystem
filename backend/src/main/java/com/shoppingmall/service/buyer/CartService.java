package com.shoppingmall.service.buyer;

import com.shoppingmall.dto.CartDTO;
import com.shoppingmall.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
public interface CartService {

    /**
     * 获取购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 添加商品到购物车（通过商品ID）
     *
     * @param userId  用户ID
     * @param cartDTO 购物车DTO
     * @return 购物车ID
     */
    Long addToCart(Long userId, CartDTO cartDTO);

    /**
     * 通过货号添加商品到购物车
     *
     * @param userId      用户ID
     * @param productCode 商品编码
     * @param quantity    数量
     * @return 购物车ID
     */
    Long addToCartByCode(Long userId, String productCode, Integer quantity);

    /**
     * 更新购物车商品数量
     *
     * @param id      购物车ID
     * @param userId  用户ID
     * @param quantity 数量
     */
    void updateQuantity(Long id, Long userId, Integer quantity);

    /**
     * 删除购物车商品
     *
     * @param id     购物车ID
     * @param userId 用户ID
     */
    void deleteCartItem(Long id, Long userId);

    /**
     * 批量删除购物车商品
     *
     * @param ids    购物车ID列表
     * @param userId 用户ID
     */
    void batchDeleteCartItems(List<Long> ids, Long userId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 获取购物车商品数量统计
     *
     * @param userId 用户ID
     * @return 商品总数量
     */
    Integer getCartItemCount(Long userId);
}























































