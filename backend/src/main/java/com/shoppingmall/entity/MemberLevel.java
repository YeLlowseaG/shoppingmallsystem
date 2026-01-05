package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
@TableName("member_level")
public class MemberLevel {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称（如：普通会员、银卡会员、金卡会员、钻石会员）
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
     * 折扣率（如：95.00表示95折，100.00表示无折扣）
     */
    private BigDecimal discountRate;

    /**
     * 排序号（数字越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 等级描述
     */
    private String description;

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









































