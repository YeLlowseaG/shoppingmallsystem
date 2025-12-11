package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 广告位实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
@TableName("website_advertisement")
public class Advertisement {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 广告名称
     */
    private String adName;

    /**
     * 广告位置（floor_1/floor_2.../brand_side_1/brand_side_2）
     */
    private String adPosition;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 链接类型（0-无链接，1-商品分类，2-商品详情，3-促销活动，4-外部链接）
     */
    private Integer linkType;

    /**
     * 链接值
     */
    private String linkValue;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

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
}
