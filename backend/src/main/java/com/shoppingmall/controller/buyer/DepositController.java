package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.DepositQueryDTO;
import com.shoppingmall.dto.DepositRechargeDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.service.buyer.DepositService;
import com.shoppingmall.vo.DepositBalanceVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 预存款控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/api/buyer/member/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;
    private final HttpServletRequest request;

    /**
     * 预存款充值
     */
    @PostMapping("/recharge")
    public Result<PaymentResponseDTO> recharge(@Valid @RequestBody DepositRechargeDTO rechargeDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }

        PaymentResponseDTO paymentResponse = depositService.recharge(userId, rechargeDTO);
        return Result.success("充值订单创建成功", paymentResponse);
    }

    /**
     * 获取预存款余额和交易记录
     */
    @GetMapping("/balance")
    public Result<DepositBalanceVO> getBalance(DepositQueryDTO queryDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }

        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }

        DepositBalanceVO result = depositService.getBalanceAndRecords(userId, queryDTO);
        return Result.success(result);
    }

    /**
     * 支付回调接口（微信/支付宝回调）
     */
    @PostMapping("/payment/callback")
    public Result<?> paymentCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            // 从回调数据中提取信息
            // 注意：实际对接时需要根据微信/支付宝的回调格式来解析
            String internalOrderNo = (String) callbackData.get("outTradeNo"); // 内部订单号（商户订单号）
            String externalTradeNo = (String) callbackData.get("tradeNo"); // 外部交易号（微信/支付宝交易号）
            String tradeStatus = (String) callbackData.get("tradeStatus"); // 交易状态

            // 如果字段名不同，需要根据实际支付接口文档调整
            if (internalOrderNo == null) {
                internalOrderNo = (String) callbackData.get("out_trade_no");
            }
            if (externalTradeNo == null) {
                externalTradeNo = (String) callbackData.get("trade_no");
            }
            if (tradeStatus == null) {
                tradeStatus = (String) callbackData.get("trade_status");
            }

            if (internalOrderNo == null || externalTradeNo == null) {
                return Result.error(400, "回调数据缺少必要参数");
            }

            // 判断支付是否成功
            boolean success = "TRADE_SUCCESS".equals(tradeStatus) 
                    || "SUCCESS".equals(tradeStatus)
                    || "PAID".equals(tradeStatus);

            depositService.handlePaymentCallback(internalOrderNo, externalTradeNo, success);

            return Result.success("回调处理成功");
        } catch (Exception e) {
            return Result.error(500, "回调处理失败：" + e.getMessage());
        }
    }
}

