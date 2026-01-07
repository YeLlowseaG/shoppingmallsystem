package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预存款余额VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class DepositBalanceVO {

    /**
     * 预存款余额
     */
    private BigDecimal depositBalance;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 交易记录列表
     */
    private List<DepositRecordVO> records;

    /**
     * 总记录数
     */
    private Long total;
}





































































