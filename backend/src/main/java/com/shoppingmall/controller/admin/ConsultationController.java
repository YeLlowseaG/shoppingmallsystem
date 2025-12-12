package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ConsultationReplyDTO;
import com.shoppingmall.service.consultation.ConsultationService;
import com.shoppingmall.vo.ConsultationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端咨询控制器
 */
@Tag(name = "管理端咨询管理")
@RestController
@RequestMapping("/api/admin/consultation")
@RequiredArgsConstructor
public class ConsultationController {
    
    private final ConsultationService consultationService;
    
    @Operation(summary = "分页获取咨询列表")
    @GetMapping("/page")
    public Result<Page<ConsultationVO>> getConsultationPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "商品名称") @RequestParam(required = false) String productName,
            @Parameter(description = "联系人姓名") @RequestParam(required = false) String contactName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<ConsultationVO> page = consultationService.getConsultationPage(
                current, size, productName, contactName, status);
        return Result.success(page);
    }
    
    @Operation(summary = "获取咨询详情")
    @GetMapping("/{consultationId}")
    public Result<ConsultationVO> getConsultationById(
            @Parameter(description = "咨询ID") @PathVariable Long consultationId) {
        
        ConsultationVO consultation = consultationService.getConsultationById(consultationId);
        return Result.success(consultation);
    }
    
    @Operation(summary = "回复咨询")
    @PutMapping("/{consultationId}/reply")
    public Result<Void> replyConsultation(
            @Parameter(description = "咨询ID") @PathVariable Long consultationId,
            @Valid @RequestBody ConsultationReplyDTO replyDTO,
            @RequestAttribute("adminId") Long adminId) {
        
        consultationService.replyConsultation(consultationId, replyDTO, adminId);
        return Result.success();
    }
    
    @Operation(summary = "更新咨询状态")
    @PutMapping("/{consultationId}/status")
    public Result<Void> updateConsultationStatus(
            @Parameter(description = "咨询ID") @PathVariable Long consultationId,
            @Parameter(description = "状态") @RequestParam Integer status) {
        
        consultationService.updateConsultationStatus(consultationId, status);
        return Result.success();
    }
    
    @Operation(summary = "删除咨询")
    @DeleteMapping("/{consultationId}")
    public Result<Void> deleteConsultation(
            @Parameter(description = "咨询ID") @PathVariable Long consultationId) {
        
        consultationService.deleteConsultation(consultationId);
        return Result.success();
    }
}