package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.constant.UserLevel;
import com.shoppingmall.dto.CartDTO;
import com.shoppingmall.entity.Cart;
import com.shoppingmall.entity.User;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductPrice;
import com.shoppingmall.repository.cart.CartRepository;
import com.shoppingmall.repository.product.ProductPriceRepository;
import com.shoppingmall.repository.product.ProductRepository;
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
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;

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

        // 检查商品是否存在且已上架
        Product product = productRepository.selectById(cartDTO.getProductId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(400, "商品已下架，无法添加到购物车");
        }

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

        // 通过商品编码查询商品
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getProductCode, productCode);
        productWrapper.eq(Product::getStatus, 1); // 只查询上架商品
        Product product = productRepository.selectOne(productWrapper);
        
        if (product == null) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        // 使用商品ID添加到购物车
        CartDTO cartDTO = new CartDTO();
        cartDTO.setProductId(product.getId());
        cartDTO.setQuantity(quantity);
        return addToCart(userId, cartDTO);
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
     * 查询商品信息、价格信息、重量信息等
     */
    private CartVO convertToVO(Cart cart, Long userId) {
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setSelected(false); // 默认未选中

        // 1. 查询商品信息
        Product product = productRepository.selectById(cart.getProductId());
        if (product == null) {
            log.warn("购物车中的商品不存在: productId={}", cart.getProductId());
            // 商品不存在时设置默认值
            vo.setProductCode("未知");
            vo.setName("商品已下架");
            vo.setImage("");
            vo.setSalesPrice(BigDecimal.ZERO);
            vo.setMemberPrice(BigDecimal.ZERO);
            vo.setWholesalePrice(null);
            vo.setWeight(BigDecimal.ZERO);
            return vo;
        }

        // 设置商品基本信息
        vo.setProductCode(product.getProductCode());
        vo.setName(product.getProductName());
        // 使用主图，如果没有主图则使用第一张图片
        if (product.getMainImage() != null && !product.getMainImage().trim().isEmpty()) {
            vo.setImage(product.getMainImage());
        } else {
            vo.setImage(""); // 如果没有图片，设置为空字符串
        }
        
        // 设置商品重量（从商品表获取，单位：克）
        if (product.getWeight() != null) {
            vo.setWeight(BigDecimal.valueOf(product.getWeight()));
        } else {
            vo.setWeight(BigDecimal.ZERO);
        }

        // 2. 查询用户等级
        User user = userRepository.selectById(userId);
        Integer userLevel = (user != null && user.getUserLevel() != null) ? user.getUserLevel() : UserLevel.NORMAL;
        String userLevelName = convertUserLevelToString(userLevel);

        // 3. 查询价格信息
        // 销售价格：使用商品表的salePrice，如果没有则使用basePrice
        BigDecimal salesPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
        vo.setSalesPrice(salesPrice != null ? salesPrice : BigDecimal.ZERO);

        // 会员价：根据用户等级查询价格表
        BigDecimal memberPrice = getPriceByUserLevel(cart.getProductId(), userLevelName, cart.getQuantity());
        vo.setMemberPrice(memberPrice != null ? memberPrice : salesPrice);

        // 批发优惠价：查询金牌等级的价格（如果有）
        BigDecimal wholesalePrice = getPriceByUserLevel(cart.getProductId(), "金牌", cart.getQuantity());
        vo.setWholesalePrice(wholesalePrice);

        return vo;
    }

    /**
     * 根据用户等级获取价格
     * 
     * @param productId 商品ID
     * @param userLevelName 用户等级名称（普通/VIP/金牌）
     * @param quantity 数量（用于阶梯价格）
     * @return 价格
     */
    private BigDecimal getPriceByUserLevel(Long productId, String userLevelName, Integer quantity) {
        LambdaQueryWrapper<ProductPrice> priceWrapper = new LambdaQueryWrapper<>();
        priceWrapper.eq(ProductPrice::getProductId, productId);
        priceWrapper.eq(ProductPrice::getUserLevel, userLevelName);
        
        // 如果有数量，查询符合数量范围的阶梯价格
        if (quantity != null && quantity > 0) {
            priceWrapper.le(ProductPrice::getMinQuantity, quantity);
            priceWrapper.and(w -> w.isNull(ProductPrice::getMaxQuantity)
                    .or().ge(ProductPrice::getMaxQuantity, quantity));
        }
        
        priceWrapper.orderByDesc(ProductPrice::getMinQuantity); // 按最小数量降序，优先匹配高阶梯价格
        priceWrapper.last("LIMIT 1");
        
        ProductPrice productPrice = productPriceRepository.selectOne(priceWrapper);
        return productPrice != null ? productPrice.getPrice() : null;
    }

    /**
     * 将用户等级数字转换为字符串
     * 
     * @param userLevel 用户等级（0-普通，1-VIP，2-金牌）
     * @return 用户等级名称
     */
    private String convertUserLevelToString(Integer userLevel) {
        if (userLevel == null) {
            return "普通";
        }
        switch (userLevel) {
            case 0:
                return "普通";
            case 1:
                return "VIP";
            case 2:
                return "金牌";
            default:
                return "普通";
        }
    }
}



