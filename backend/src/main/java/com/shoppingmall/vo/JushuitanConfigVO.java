package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聚水潭配置VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
public class JushuitanConfigVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 聚水潭AppKey
     */
    private String appKey;

    /**
     * 聚水潭AppSecret（脱敏显示）
     */
    private String appSecret;

    /**
     * 生产环境AccessToken（脱敏显示）
     */
    private String accessToken;

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * 环境类型（test=测试环境，production=生产环境）
     */
    private String envType;

    /**
     * 测试环境API地址
     */
    private String testApiUrl;

    /**
     * 测试环境AppKey
     */
    private String testAppKey;

    /**
     * 测试环境AppSecret（脱敏显示）
     */
    private String testAppSecret;

    /**
     * 测试环境AccessToken（脱敏显示）
     */
    private String testAccessToken;

    /**
     * 店铺编号
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
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
