package com.shoppingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.PaymentApiLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付接口日志Mapper接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Mapper
public interface PaymentApiLogMapper extends BaseMapper<PaymentApiLog> {
}

