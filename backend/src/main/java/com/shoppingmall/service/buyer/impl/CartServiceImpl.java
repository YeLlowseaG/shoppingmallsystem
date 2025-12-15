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
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.vo.CartVO;
import com.shoppingmall.vo.MemberLevelVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
    private final MemberLevelService memberLevelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // 检查购物车中是否已存在该商品（相同商品且相同规格才合并）
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, cartDTO.getProductId());
        
        // 如果有SKU ID，需要匹配SKU ID
        if (cartDTO.getSkuId() != null) {
            wrapper.eq(Cart::getSkuId, cartDTO.getSkuId());
        } else if (cartDTO.getSpecCombination() != null && !cartDTO.getSpecCombination().trim().isEmpty()) {
            // 如果没有SKU ID但有规格组合，检查specCombination是否相同
            wrapper.eq(Cart::getSpecCombination, cartDTO.getSpecCombination());
            wrapper.isNull(Cart::getSkuId); // 确保SKU ID也为空
        } else {
            // 既没有SKU ID也没有规格组合，检查是否也没有SKU ID和规格组合的项
            wrapper.isNull(Cart::getSkuId);
            wrapper.and(w -> w.isNull(Cart::getSpecCombination).or().eq(Cart::getSpecCombination, ""));
        }

        Cart existingCart = cartRepository.selectOne(wrapper);
        if (existingCart != null) {
            // 如果已存在相同商品且相同规格，更新数量
            existingCart.setQuantity(existingCart.getQuantity() + cartDTO.getQuantity());
            cartRepository.updateById(existingCart);
            log.info("更新购物车商品数量: userId={}, productId={}, skuId={}, quantity={}", userId, cartDTO.getProductId(), cartDTO.getSkuId(), existingCart.getQuantity());
            return existingCart.getId();
        } else {
            // 如果不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(cartDTO.getProductId());
            cart.setQuantity(cartDTO.getQuantity());
            // 设置SKU ID和规格组合（如果有）
            if (cartDTO.getSkuId() != null) {
                cart.setSkuId(cartDTO.getSkuId());
            }
            if (cartDTO.getSpecCombination() != null && !cartDTO.getSpecCombination().trim().isEmpty()) {
                cart.setSpecCombination(cartDTO.getSpecCombination());
            }
            cartRepository.insert(cart);
            log.info("添加商品到购物车: userId={}, productId={}, quantity={}, skuId={}", userId, cartDTO.getProductId(), cartDTO.getQuantity(), cartDTO.getSkuId());
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
        if (carts == null || carts.isEmpty()) {
            return 0;
        }
        // 处理quantity可能为null的情况
        return carts.stream()
                .mapToInt(cart -> cart.getQuantity() != null ? cart.getQuantity() : 0)
                .sum();
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

        // 2. 查询价格信息
        // 销售价格：统一使用basePrice作为销售价格
        BigDecimal salesPrice = product.getBasePrice();
        if (salesPrice == null) {
            salesPrice = BigDecimal.ZERO;
        }
        vo.setSalesPrice(salesPrice);

        // 3. 计算会员价格：根据会员等级表的折扣率计算
        BigDecimal memberPrice = calculateMemberPrice(salesPrice, userId);
        // 保留两位小数
        memberPrice = memberPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setMemberPrice(memberPrice);

        // 4. 处理规格信息：将specCombination JSON转换为格式化的文本
        String specText = formatSpecText(cart.getSpecCombination());
        vo.setSpecText(specText);

        return vo;
    }

    /**
     * 根据会员等级折扣率计算会员价格
     * 
     * @param salesPrice 销售价格
     * @param userId 用户ID
     * @return 会员价格
     */
    private BigDecimal calculateMemberPrice(BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        try {
            // 获取所有启用的会员等级（按排序号排序）
            List<MemberLevelVO> memberLevels = memberLevelService.getAllEnabledMemberLevels();
            if (memberLevels == null || memberLevels.isEmpty()) {
                // 如果没有会员等级，返回原价
                return salesPrice;
            }

            // 获取用户的会员等级
            User user = userRepository.selectById(userId);
            Long memberLevelId = null;
            
            // sys_user.user_level 字段存储的是 member_level.id（会员等级ID）
            // 如果 user_level 为 null 或 0，则查找默认等级（第一个等级，通常是 id=1 的普卡会员）
            if (user != null && user.getUserLevel() != null && user.getUserLevel() > 0) {
                // user_level 存储的是 member_level.id
                memberLevelId = user.getUserLevel().longValue();
            }
            
            // 查找匹配的会员等级
            MemberLevelVO memberLevel = null;
            if (memberLevelId != null) {
                // 根据 member_level.id 查找
                for (MemberLevelVO level : memberLevels) {
                    if (level.getId() != null && level.getId().equals(memberLevelId)) {
                        memberLevel = level;
                        break;
                    }
                }
            }
            
            // 如果找不到匹配的等级，使用第一个等级（默认，通常是 id=1 的普卡会员）
            if (memberLevel == null && !memberLevels.isEmpty()) {
                memberLevel = memberLevels.get(0);
            }
            
            // 如果还是没有找到，返回原价
            if (memberLevel == null) {
                return salesPrice;
            }
            BigDecimal discountRate = memberLevel.getDiscountRate();
            
            if (discountRate == null) {
                // 如果没有折扣率，返回原价
                return salesPrice;
            }

            // 计算会员价格：销售价格 * (折扣率 / 100.00)
            // 例如：100.00 * (95.00 / 100.00) = 95.00
            BigDecimal memberPrice = salesPrice.multiply(discountRate).divide(new BigDecimal("100.00"), 2, BigDecimal.ROUND_HALF_UP);
            return memberPrice;
        } catch (Exception e) {
            log.error("计算会员价格失败: userId={}, salesPrice={}", userId, salesPrice, e);
            // 计算失败时返回原价
            return salesPrice;
        }
    }

    /**
     * 将规格组合JSON转换为格式化的文本
     * 例如：{"颜色":"白色","尺寸":"L"} -> "颜色:白色 / 尺寸:L"
     *
     * @param specCombination 规格组合JSON字符串
     * @return 格式化的规格文本
     */
    private String formatSpecText(String specCombination) {
        if (specCombination == null || specCombination.trim().isEmpty()) {
            return "";
        }

        try {
            Map<String, String> specMap = objectMapper.readValue(
                    specCombination,
                    new TypeReference<Map<String, String>>() {}
            );

            return specMap.entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.joining(" / "));
        } catch (Exception e) {
            log.warn("解析规格组合失败: specCombination={}", specCombination, e);
            return "";
        }
    }
}



