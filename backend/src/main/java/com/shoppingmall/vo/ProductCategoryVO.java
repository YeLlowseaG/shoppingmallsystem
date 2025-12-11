package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Data
public class ProductCategoryVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类级别（1-一级，2-二级，3-三级）
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 子分类列表
     */
    private List<ProductCategoryVO> children;
}
