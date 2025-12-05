package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户审核实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
@TableName("sys_user_audit")
public class UserAudit {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 审核状态（0-待审核，1-已通过，2-已拒绝）
     */
    private Integer auditStatus;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}


