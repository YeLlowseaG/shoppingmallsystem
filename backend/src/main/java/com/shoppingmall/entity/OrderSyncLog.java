package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单同步日志实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
@TableName("order_sync_log")
public class OrderSyncLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 同步类型（PUSH_ORDER-推送订单，PULL_LOGISTICS-拉取物流，QUERY_ORDER-查询订单）
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
