package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.AdminDepositQueryDTO;
import com.shoppingmall.dto.RefundRequestDTO;
import com.shoppingmall.service.admin.DepositService;
import com.shoppingmall.vo.AdminDepositRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台预存款控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("adminDepositController")
@RequestMapping("/api/admin/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    /**
     * 分页查询预存款交易记录
     */
    @GetMapping("/records")
    public Result<IPage<AdminDepositRecordVO>> getDepositRecordList(AdminDepositQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }

        IPage<AdminDepositRecordVO> result = depositService.getDepositRecordList(queryDTO);
        return Result.success(result);
    }

    /**
     * 根据ID获取预存款交易记录详情
     */
    @GetMapping("/record/{id}")
    public Result<AdminDepositRecordVO> getDepositRecordById(@PathVariable Long id) {
        AdminDepositRecordVO record = depositService.getDepositRecordById(id);
        if (record == null) {
            return Result.error(404, "记录不存在");
        }
        return Result.success(record);
    }

    /**
     * 预存款充值退款
     */
    @PostMapping("/refund")
    public Result<Void> refundDepositRecharge(@Valid @RequestBody RefundRequestDTO refundDTO) {
        if (refundDTO.getDepositDetailId() == null) {
            return Result.error(400, "充值记录ID不能为空");
        }
        depositService.refundDepositRecharge(refundDTO);
        return Result.success();
    }

    /**
     * 手动查询并同步支付状态
     * 用于支付中状态的交易记录，主动查询支付宝或微信的支付状态并同步结果
     * 查询频率限制：30秒内同一记录只能查询一次
     * 日志记录：自动记录到payment_api_log表
     */
    @PostMapping("/sync-payment-status/{id}")
    public Result<Void> syncPaymentStatus(@PathVariable Long id) {
        depositService.syncPaymentStatus(id);
        return Result.success();
    }
}






































































