package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聚水潭配置实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
@TableName("jushuitan_config")
public class JushuitanConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 聚水潭AppKey
     */
    private String appKey;

    /**
     * 聚水潭AppSecret
     */
    private String appSecret;

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * 店铺编号（多店铺时使用）
     */
    private String shopId;

    /**
     * 合作伙伴ID
     */
    private String partnerId;

    /**
     * 是否启用（0-否，1-是）
     */
    private Integer enabled;

    /**
     * 是否自动推送订单（0-否，1-是）
     */
    private Integer autoPushOrder;

    /**
     * 是否自动拉取物流（0-否，1-是）
     */
    private Integer autoPullLogistics;

    /**
     * 拉取物流间隔（分钟）
     */
    private Integer pullInterval;

    /**
     * 备注
     */
    private String remark;

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
