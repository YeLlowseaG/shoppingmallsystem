package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.constant.UserLevel;
import com.shoppingmall.dto.CartDTO;
import com.shoppingmall.entity.Cart;
import com.shoppingmall.entity.User;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductMemberPrice;
import com.shoppingmall.entity.ProductPrice;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.entity.ProductSkuMemberPrice;
import com.shoppingmall.repository.cart.CartRepository;
import com.shoppingmall.repository.product.ProductMemberPriceRepository;
import com.shoppingmall.repository.product.ProductPriceRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.sku.ProductSkuMemberPriceRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
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
    private final ProductSkuRepository productSkuRepository;
    private final MemberLevelService memberLevelService;
    private final ProductMemberPriceRepository productMemberPriceRepository;
    private final ProductSkuMemberPriceRepository productSkuMemberPriceRepository;
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

        // 0. 获取用户信息，判断是否是会员
        User user = userRepository.selectById(userId);
        Integer isMember = (user != null && user.getIsMember() != null && user.getIsMember() == 1) ? 1 : 0;
        vo.setIsMember(isMember);

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
        
        // 设置运费模板ID
        vo.setShippingTemplateId(product.getShippingTemplateId());

        // 2. 查询价格信息
        // 判断是否有SKU
        ProductSku sku = null;
        if (cart.getSkuId() != null) {
            sku = productSkuRepository.selectById(cart.getSkuId());
            // 设置SKU ID和SKU编码
            vo.setSkuId(cart.getSkuId());
            if (sku != null && sku.getSkuCode() != null && !sku.getSkuCode().trim().isEmpty()) {
                vo.setSkuCode(sku.getSkuCode());
            }
        }

        BigDecimal salesPrice;
        BigDecimal memberPrice;

        // 3. 设置商品重量（根据是否启用多个规格SKU来决定使用哪个表的重量）
        // 如果商品没有启用多个规格sku（enableSpec == 0），则获取商品表重量字段的数据
        // 如果启用sku（enableSpec == 1），则读取product_sku表的重量字段来计算运费
        boolean enableSpec = product.getEnableSpec() != null && product.getEnableSpec() == 1;
        if (enableSpec && sku != null) {
            // 启用SKU，使用SKU表的重量
            if (sku.getWeight() != null) {
                vo.setWeight(sku.getWeight());
            } else {
                // SKU没有重量，使用商品表的重量作为兜底
                vo.setWeight(product.getWeight() != null ? BigDecimal.valueOf(product.getWeight().longValue()) : BigDecimal.ZERO);
            }
        } else {
            // 没有启用SKU，使用商品表的重量
            vo.setWeight(product.getWeight() != null ? BigDecimal.valueOf(product.getWeight().longValue()) : BigDecimal.ZERO);
        }

        if (sku != null) {
            // 有SKU，使用SKU的价格
            salesPrice = sku.getPrice();
            if (salesPrice == null) {
                salesPrice = BigDecimal.ZERO;
            }
            
            // 计算会员价格：优先使用SKU配置的会员价
            memberPrice = calculateMemberPriceForSku(sku, salesPrice, userId);
        } else {
            // 没有SKU，使用商品的价格
            salesPrice = product.getBasePrice();
            if (salesPrice == null) {
                salesPrice = BigDecimal.ZERO;
            }
            
            // 计算会员价格：优先使用商品配置的会员价
            memberPrice = calculateMemberPriceForProduct(product, salesPrice, userId);
        }

        vo.setSalesPrice(salesPrice);
        // 保留两位小数
        memberPrice = memberPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setMemberPrice(memberPrice);

        // 设置 enableMemberPrice：优先使用SKU的，如果没有SKU则使用商品的
        if (sku != null) {
            // 有SKU，使用SKU的 enableMemberPrice
            vo.setEnableMemberPrice(sku.getEnableMemberPrice());
        } else {
            // 没有SKU，使用商品的 enableMemberPrice
            vo.setEnableMemberPrice(product.getEnableMemberPrice());
        }

        // 3. 处理规格信息：将specCombination JSON转换为格式化的文本
        String specText = formatSpecText(cart.getSpecCombination());
        vo.setSpecText(specText);

        return vo;
    }

    /**
     * 计算商品的会员价格
     * 新逻辑：
     * 1. 普通用户：返回基础价格
     * 2. 会员用户：
     *    a. 如果启用会员价：根据会员等级从 product_member_price 表查询会员价
     *    b. 如果没有启用会员价：返回基础价格
     * 
     * @param product 商品实体
     * @param salesPrice 销售价格（基础价格）
     * @param userId 用户ID
     * @return 会员价格（非会员或未启用会员价返回基础价格）
     */
    private BigDecimal calculateMemberPriceForProduct(Product product, BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 1. 检查用户是否是会员
        User user = userRepository.selectById(userId);
        if (user == null || user.getIsMember() == null || user.getIsMember() != 1) {
            // 普通用户：返回基础价格
            return salesPrice;
        }

        // 2. 如果是会员，检查商品是否启用了会员价
        if (product.getEnableMemberPrice() == null || product.getEnableMemberPrice() != 1) {
            // 未启用会员价：返回基础价格
            return salesPrice;
        }

        // 3. 启用会员价：根据会员等级查询会员价
        Long memberLevelId = user.getMemberLevelId();
        if (memberLevelId == null) {
            // 会员但没有等级：返回基础价格
            return salesPrice;
        }

        try {
            LambdaQueryWrapper<ProductMemberPrice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductMemberPrice::getProductId, product.getId())
                   .eq(ProductMemberPrice::getMemberLevelId, memberLevelId);
            ProductMemberPrice productMemberPrice = productMemberPriceRepository.selectOne(wrapper);
            
            if (productMemberPrice != null && productMemberPrice.getMemberPrice() != null 
                    && productMemberPrice.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
                return productMemberPrice.getMemberPrice();
            }
        } catch (Exception e) {
            log.error("查询商品会员价失败: productId={}, memberLevelId={}, userId={}", 
                    product.getId(), memberLevelId, userId, e);
        }

        // 如果查询不到会员价配置，返回基础价格
        return salesPrice;
    }

    /**
     * 计算SKU的会员价格
     * 新逻辑：
     * 1. 普通用户：返回基础价格
     * 2. 会员用户：
     *    a. 如果启用会员价：根据会员等级从 product_sku_member_price 表查询会员价
     *    b. 如果没有启用会员价：返回基础价格
     * 
     * @param sku SKU实体
     * @param salesPrice 销售价格（基础价格）
     * @param userId 用户ID
     * @return 会员价格（非会员或未启用会员价返回基础价格）
     */
    private BigDecimal calculateMemberPriceForSku(ProductSku sku, BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 1. 检查用户是否是会员
        User user = userRepository.selectById(userId);
        if (user == null || user.getIsMember() == null || user.getIsMember() != 1) {
            // 普通用户：返回基础价格
            return salesPrice;
        }

        // 2. 如果是会员，检查SKU是否启用了会员价
        if (sku.getEnableMemberPrice() == null || sku.getEnableMemberPrice() != 1) {
            // 未启用会员价：返回基础价格
            return salesPrice;
        }

        // 3. 启用会员价：根据会员等级查询会员价
        Long memberLevelId = user.getMemberLevelId();
        if (memberLevelId == null || sku.getId() == null) {
            // 会员但没有等级或SKU没有ID：返回基础价格
            return salesPrice;
        }

        try {
            LambdaQueryWrapper<ProductSkuMemberPrice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductSkuMemberPrice::getSkuId, sku.getId())
                   .eq(ProductSkuMemberPrice::getMemberLevelId, memberLevelId);
            ProductSkuMemberPrice skuMemberPrice = productSkuMemberPriceRepository.selectOne(wrapper);
            
            if (skuMemberPrice != null && skuMemberPrice.getMemberPrice() != null 
                    && skuMemberPrice.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
                return skuMemberPrice.getMemberPrice();
            }
        } catch (Exception e) {
            log.error("查询SKU会员价失败: skuId={}, memberLevelId={}, userId={}", 
                    sku.getId(), memberLevelId, userId, e);
        }

        // 如果查询不到会员价配置，返回基础价格
        return salesPrice;
    }

    /**
     * 根据会员等级折扣率计算会员价格
     * 已废弃：不再使用折扣率计算会员价，改为从 product_member_price 和 product_sku_member_price 表查询
     * 
     * @deprecated 此方法已废弃，不再使用折扣率计算会员价
     */
    @Deprecated
    private BigDecimal calculateMemberPriceByDiscount(BigDecimal salesPrice, Long userId) {
        // 已废弃，直接返回基础价格
        return salesPrice;
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



