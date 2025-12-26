package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帮助中心分类VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class HelpCategoryVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 父分类ID（0表示顶级分类）
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort;

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
    private List<HelpCategoryVO> children;
}












































