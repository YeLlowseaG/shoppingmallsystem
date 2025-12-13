package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密码重置验证码实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
@TableName("password_reset_code")
public class PasswordResetCode {

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
     * 用户名
     */
    private String username;

    /**
     * 接收邮箱
     */
    private String email;

    /**
     * 验证码/重置令牌
     */
    private String code;

    /**
     * 验证码类型（RESET_PASSWORD-密码重置）
     */
    private String codeType;

    /**
     * 发送的邮件内容
     */
    private String emailContent;

    /**
     * 状态（0-未使用，1-已使用，2-已过期）
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 使用时间
     */
    private LocalDateTime usedTime;

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

