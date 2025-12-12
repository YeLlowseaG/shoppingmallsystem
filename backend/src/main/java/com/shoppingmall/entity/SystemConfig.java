package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Data
@TableName("system_config")
public class SystemConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String configDesc;

    /**
     * 配置类型（text/textarea/image/number）
     */
    private String configType;

    /**
     * 排序
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
}