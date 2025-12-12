package com.shoppingmall.controller.buyer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ConsultationDTO;
import com.shoppingmall.service.consultation.ConsultationService;
import com.shoppingmall.vo.ConsultationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 买家端咨询控制器
 */
@Tag(name = "买家端咨询管理")
@RestController
@RequestMapping("/api/buyer/consultation")
@RequiredArgsConstructor
public class BuyerConsultationController {
    
    private final ConsultationService consultationService;
    
    @Operation(summary = "提交咨询")
    @PostMapping
    public Result<Void> submitConsultation(
            @Valid @RequestBody ConsultationDTO consultationDTO,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        
        consultationService.submitConsultation(consultationDTO, userId);
        return Result.success();
    }
    
    @Operation(summary = "获取我的咨询列表")
    @GetMapping("/my")
    public Result<Page<ConsultationVO>> getMyConsultations(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") Long userId) {
        
        Page<ConsultationVO> page = consultationService.getUserConsultations(current, size, userId);
        return Result.success(page);
    }
    
    @Operation(summary = "获取咨询详情")
    @GetMapping("/{consultationId}")
    public Result<ConsultationVO> getConsultationById(
            @Parameter(description = "咨询ID") @PathVariable Long consultationId,
            @RequestAttribute("userId") Long userId) {
        
        ConsultationVO consultation = consultationService.getConsultationById(consultationId);
        
        // 验证咨询是否属于当前用户
        if (consultation.getUserId() != null && !consultation.getUserId().equals(userId)) {
            return Result.error("无权访问该咨询");
        }
        
        return Result.success(consultation);
    }
}