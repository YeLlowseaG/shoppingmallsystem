package com.shoppingmall.vo;

import lombok.Data;
import java.util.List;

/**
 * 地区VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Data
public class RegionVO {
    /**
     * 地区ID
     */
    private Long id;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 父级地区ID
     */
    private Long parentId;

    /**
     * 级别（1-省/直辖市，2-市，3-区/县）
     */
    private Integer level;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 子级地区列表（用于树形结构）
     */
    private List<RegionVO> children;
}


















































