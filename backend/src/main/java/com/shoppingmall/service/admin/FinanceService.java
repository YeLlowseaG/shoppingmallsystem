package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.PaymentRecordQueryDTO;
import com.shoppingmall.dto.RefundRequestDTO;
import com.shoppingmall.vo.PaymentRecordVO;

/**
 * 财务管理服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
public interface FinanceService {

    /**
     * 分页查询支付记录
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<PaymentRecordVO> getPaymentRecordList(PaymentRecordQueryDTO queryDTO);

    /**
     * 根据ID获取支付记录详情
     *
     * @param id 支付记录ID
     * @return 支付记录详情
     */
    PaymentRecordVO getPaymentRecordById(Long id);

    /**
     * 支付记录退款
     *
     * @param refundDTO 退款请求
     */
    void refundPaymentRecord(RefundRequestDTO refundDTO);
}

























