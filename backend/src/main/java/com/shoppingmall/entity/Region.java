package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地区实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Data
@TableName("region")
public class Region {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 地区编码（如：110000）
     */
    private String code;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 父级地区ID（NULL表示顶级）
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
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 用于树形结构返回（不映射到数据库）
     */
    @TableField(exist = false)
    private List<Region> children;
}





































































