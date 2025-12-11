package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.CartDTO;
import com.shoppingmall.entity.Cart;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.cart.CartRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.buyer.CartService;
import com.shoppingmall.vo.CartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    public List<CartVO> getCartList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getUpdateTime);

        List<Cart> carts = cartRepository.selectList(wrapper);
        return carts.stream().map(cart -> convertToVO(cart, userId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addToCart(Long userId, CartDTO cartDTO) {
        if (cartDTO.getProductId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }

        // 检查商品是否存在（TODO: 后续需要查询商品表验证）
        // 这里先简单验证，后续需要完善商品查询逻辑

        // 检查购物车中是否已存在该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, cartDTO.getProductId());

        Cart existingCart = cartRepository.selectOne(wrapper);
        if (existingCart != null) {
            // 如果已存在，更新数量
            existingCart.setQuantity(existingCart.getQuantity() + cartDTO.getQuantity());
            cartRepository.updateById(existingCart);
            log.info("更新购物车商品数量: userId={}, productId={}, quantity={}", userId, cartDTO.getProductId(), existingCart.getQuantity());
            return existingCart.getId();
        } else {
            // 如果不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(cartDTO.getProductId());
            cart.setQuantity(cartDTO.getQuantity());
            cartRepository.insert(cart);
            log.info("添加商品到购物车: userId={}, productId={}, quantity={}", userId, cartDTO.getProductId(), cartDTO.getQuantity());
            return cart.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addToCartByCode(Long userId, String productCode, Integer quantity) {
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new BusinessException(400, "商品编码不能为空");
        }

        // TODO: 通过商品编码查询商品ID
        // 这里需要先查询商品表，根据product_code获取product_id
        // 暂时抛出异常，提示需要先实现商品查询功能
        throw new BusinessException(501, "通过货号添加商品功能待实现，需要先完成商品模块");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long id, Long userId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "数量必须大于0");
        }

        Cart cart = cartRepository.selectById(id);
        if (cart == null) {
            throw new BusinessException(404, "购物车商品不存在");
        }

        // 验证购物车是否属于当前用户
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改该购物车商品");
        }

        cart.setQuantity(quantity);
        cartRepository.updateById(cart);
        log.info("更新购物车商品数量: cartId={}, quantity={}", id, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long id, Long userId) {
        Cart cart = cartRepository.selectById(id);
        if (cart == null) {
            throw new BusinessException(404, "购物车商品不存在");
        }

        // 验证购物车是否属于当前用户
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除该购物车商品");
        }

        cartRepository.deleteById(id);
        log.info("删除购物车商品: cartId={}, userId={}", id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCartItems(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的商品");
        }

        // 验证所有购物车商品是否属于当前用户
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.in(Cart::getId, ids);

        List<Cart> carts = cartRepository.selectList(wrapper);
        if (carts.size() != ids.size()) {
            throw new BusinessException(403, "部分购物车商品不存在或无权删除");
        }

        cartRepository.deleteBatchIds(ids);
        log.info("批量删除购物车商品: userId={}, count={}", userId, ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartRepository.delete(wrapper);
        log.info("清空购物车: userId={}", userId);
    }

    @Override
    public Integer getCartItemCount(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> carts = cartRepository.selectList(wrapper);
        return carts.stream().mapToInt(Cart::getQuantity).sum();
    }

    /**
     * 实体转VO
     * TODO: 需要查询商品信息、价格信息、重量信息等
     */
    private CartVO convertToVO(Cart cart, Long userId) {
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setSelected(false); // 默认未选中

        // TODO: 查询商品信息
        // 1. 根据productId查询商品表，获取商品名称、图片、编码等
        // 2. 根据userId查询用户等级
        // 3. 根据productId和用户等级查询价格表，获取销售价格、会员价、批发优惠价
        // 4. 查询商品重量（可能在商品表或商品库存表中）

        // 临时数据，后续需要替换为实际查询
        vo.setProductCode("TEMP_" + cart.getProductId());
        vo.setName("商品名称（待实现）");
        vo.setImage("https://via.placeholder.com/80x80?text=Product");
        vo.setSalesPrice(BigDecimal.ZERO);
        vo.setMemberPrice(BigDecimal.ZERO);
        vo.setWholesalePrice(null);
        vo.setWeight(BigDecimal.ZERO);

        return vo;
    }
}



