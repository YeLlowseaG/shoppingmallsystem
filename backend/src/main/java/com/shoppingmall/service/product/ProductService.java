package com.shoppingmall.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
public interface ProductService {

    /**
     * 分页查询商品列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param categoryId 分类ID（可选）
     * @param keyword 关键词（可选）
     * @param status 状态（可选）
     * @return 商品分页列表
     */
    Page<ProductVO> getProductPage(Long current, Long size, Long categoryId, String keyword, String status);

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getProductById(Long id);

    /**
     * 创建商品
     *
     * @param productDTO 商品信息
     * @return 商品ID
     */
    Long createProduct(ProductDTO productDTO);

    /**
     * 更新商品
     *
     * @param productDTO 商品信息
     */
    void updateProduct(ProductDTO productDTO);

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    void deleteProduct(Long id);

    /**
     * 更新商品状态
     *
     * @param id 商品ID
     * @param status 状态（上架/下架）
     */
    void updateStatus(Long id, String status);

    /**
     * 获取热门商品列表
     *
     * @param limit 数量限制
     * @return 热门商品列表
     */
    List<ProductVO> getHotProducts(Long limit);

    /**
     * 根据分类获取推荐商品
     *
     * @param categoryId 分类ID
     * @param limit 数量限制
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendProducts(Long categoryId, Long limit);
}
