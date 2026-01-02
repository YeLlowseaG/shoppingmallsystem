package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品同步日志实体类
 *
 * @author ShoppingMall Team
 * @date 2026-01-02
 */
@Data
@TableName("product_sync_log")
public class ProductSyncLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 环境类型（test=测试环境，production=生产环境）
     */
    private String envType;

    /**
     * 同步类型（UPLOAD_ITEM-上传商品，UPDATE_ITEM-更新商品）
     */
    private String syncType;

    /**
     * 同步状态（0-失败，1-成功，2-处理中）
     */
    private Integer syncStatus;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求数据（JSON格式）
     */
    private String requestData;

    /**
     * 响应数据（JSON格式）
     */
    private String responseData;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
