package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级视图对象
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class MemberLevelVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 最低积分（包含）
     */
    private Integer minPoints;

    /**
     * 最高积分（不包含，NULL表示无上限）
     */
    private Integer maxPoints;

    /**
     * 积分区间显示文本
     */
    private String pointsRangeText;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 折扣率显示文本（如：95折）
     */
    private String discountRateText;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 等级描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}









