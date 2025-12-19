package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.BuyerDTO;
import com.shoppingmall.service.buyer.BuyerService;
import com.shoppingmall.vo.BuyerVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 采购者管理控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
@RestController
@RequestMapping("/api/admin/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;
    private final HttpServletRequest request;

    /**
     * 获取采购者列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<BuyerVO>> getBuyerList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isMember,
            @RequestParam(required = false) Long memberLevelId
    ) {
        Page<BuyerVO> result = buyerService.getBuyerList(page, pageSize, username, phone, status, isMember, memberLevelId);
        return Result.success(result);
    }

    /**
     * 根据ID获取采购者信息
     */
    @GetMapping("/{id}")
    public Result<BuyerVO> getBuyerById(@PathVariable Long id) {
        BuyerVO buyer = buyerService.getBuyerById(id);
        return Result.success(buyer);
    }

    /**
     * 更新采购者信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateBuyer(
            @PathVariable Long id,
            @RequestBody BuyerDTO buyerDTO
    ) {
        Long adminId = (Long) request.getAttribute("adminId");
        buyerService.updateBuyer(id, buyerDTO, adminId);
        return Result.success();
    }

    /**
     * 更新采购者状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateBuyerStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        buyerService.updateBuyerStatus(id, status);
        return Result.success();
    }

    /**
     * 更新采购者会员信息
     */
    @PutMapping("/{id}/member")
    public Result<Void> updateBuyerMemberInfo(
            @PathVariable Long id,
            @RequestParam(required = false) Integer isMember,
            @RequestParam(required = false) Long memberLevelId
    ) {
        buyerService.updateBuyerMemberInfo(id, isMember, memberLevelId);
        return Result.success();
    }

    /**
     * 审核采购者
     */
    @PostMapping("/{id}/audit")
    public Result<Void> auditBuyer(
            @PathVariable Long id,
            @Valid @RequestBody BuyerDTO buyerDTO
    ) {
        Long adminId = (Long) request.getAttribute("adminId");
        buyerService.auditBuyer(id, buyerDTO, adminId);
        return Result.success();
    }

    /**
     * 获取待审核采购者列表（分页）
     */
    @GetMapping("/audit/list")
    public Result<Page<BuyerVO>> getPendingAuditList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone
    ) {
        Page<BuyerVO> result = buyerService.getPendingAuditList(page, pageSize, username, realName, phone);
        return Result.success(result);
    }

    /**
     * 重置会员密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String password
    ) {
        buyerService.resetPassword(id, password);
        return Result.success();
    }
}
