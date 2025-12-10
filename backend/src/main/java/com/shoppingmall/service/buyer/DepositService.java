package com.shoppingmall.service.buyer;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.DepositQueryDTO;
import com.shoppingmall.dto.DepositRechargeDTO;
import com.shoppingmall.vo.DepositBalanceVO;

/**
 * 预存款服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface DepositService {

    /**
     * 预存款充值
     *
     * @param userId 用户ID
     * @param rechargeDTO 充值信息
     * @return 支付响应信息（包含支付URL等）
     */
    com.shoppingmall.dto.PaymentResponseDTO recharge(Long userId, DepositRechargeDTO rechargeDTO);

    /**
     * 获取预存款余额和交易记录
     *
     * @param userId 用户ID
     * @param queryDTO 查询条件
     * @return 预存款余额和交易记录
     */
    DepositBalanceVO getBalanceAndRecords(Long userId, DepositQueryDTO queryDTO);

    /**
     * 支付回调处理（更新外部交易号和状态）
     *
     * @param internalOrderNo 内部订单号
     * @param externalTradeNo 外部交易号（微信/支付宝返回）
     * @param success 是否支付成功
     */
    void handlePaymentCallback(String internalOrderNo, String externalTradeNo, boolean success);

    /**
     * 预存款支付（用于订单支付）
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param amount 支付金额
     */
    void depositPayment(Long userId, Long orderId, String orderNo, java.math.BigDecimal amount);

    /**
     * 预存款退款（用于订单退款）
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param amount 退款金额
     */
    void depositRefund(Long userId, Long orderId, String orderNo, java.math.BigDecimal amount);
}

