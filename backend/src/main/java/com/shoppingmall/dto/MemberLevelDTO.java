package com.shoppingmall.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员等级数据传输对象
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class MemberLevelDTO {

    /**
     * 主键ID（更新时必填）
     */
    private Long id;

    /**
     * 等级名称
     */
    @NotBlank(message = "等级名称不能为空")
    @Size(max = 50, message = "等级名称长度不能超过50个字符")
    private String levelName;

    /**
     * 最低积分（包含）
     * 已屏蔽：业务上不需要积分功能
     */
    // @NotNull(message = "最低积分不能为空")
    // @Min(value = 0, message = "最低积分不能小于0")
    private Integer minPoints;

    /**
     * 最高积分（不包含，NULL表示无上限）
     * 已屏蔽：业务上不需要积分功能
     */
    // @Min(value = 0, message = "最高积分不能小于0")
    private Integer maxPoints;

    /**
     * 折扣率（如：95.00表示95折，100.00表示无折扣）
     */
    @NotNull(message = "折扣率不能为空")
    @DecimalMin(value = "0.01", message = "折扣率不能小于0.01")
    @DecimalMax(value = "100.00", message = "折扣率不能大于100.00")
    private BigDecimal discountRate;

    /**
     * 排序号（数字越小越靠前）
     */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 等级描述
     */
    @Size(max = 500, message = "等级描述长度不能超过500个字符")
    private String description;
}



