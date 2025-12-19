package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.PaymentRecordQueryDTO;
import com.shoppingmall.dto.RefundRequestDTO;
import com.shoppingmall.service.admin.FinanceService;
import com.shoppingmall.vo.PaymentRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 财务管理控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/api/admin/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    /**
     * 分页查询支付记录
     */
    @GetMapping("/payment-records")
    public Result<IPage<PaymentRecordVO>> getPaymentRecordList(PaymentRecordQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }

        IPage<PaymentRecordVO> result = financeService.getPaymentRecordList(queryDTO);
        return Result.success(result);
    }

    /**
     * 根据ID获取支付记录详情
     */
    @GetMapping("/payment-record/{id}")
    public Result<PaymentRecordVO> getPaymentRecordById(@PathVariable Long id) {
        PaymentRecordVO record = financeService.getPaymentRecordById(id);
        if (record == null) {
            return Result.error(404, "记录不存在");
        }
        return Result.success(record);
    }

    /**
     * 支付记录退款
     */
    @PostMapping("/payment-record/refund")
    public Result<Void> refundPaymentRecord(@Valid @RequestBody RefundRequestDTO refundDTO) {
        if (refundDTO.getPaymentRecordId() == null) {
            return Result.error(400, "支付记录ID不能为空");
        }
        financeService.refundPaymentRecord(refundDTO);
        return Result.success();
    }

}








