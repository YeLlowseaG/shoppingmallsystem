package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.LogisticsCompanyDTO;
import com.shoppingmall.service.logistics.LogisticsService;
import com.shoppingmall.vo.LogisticsCompanyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物流管理控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@RestController
@RequestMapping("/api/admin/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    /**
     * 获取物流公司列表（分页）
     */
    @GetMapping("/company/list")
    public Result<Page<LogisticsCompanyVO>> getLogisticsCompanyList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        Page<LogisticsCompanyVO> result = logisticsService.getLogisticsCompanyList(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 获取所有启用的物流公司列表
     */
    @GetMapping("/company/all")
    public Result<List<LogisticsCompanyVO>> getAllEnabledLogisticsCompanies() {
        List<LogisticsCompanyVO> companies = logisticsService.getAllEnabledLogisticsCompanies();
        return Result.success(companies);
    }

    /**
     * 根据ID获取物流公司信息
     */
    @GetMapping("/company/{id}")
    public Result<LogisticsCompanyVO> getLogisticsCompanyById(@PathVariable Long id) {
        LogisticsCompanyVO company = logisticsService.getLogisticsCompanyById(id);
        return Result.success(company);
    }

    /**
     * 新增物流公司
     */
    @PostMapping("/company")
    public Result<Void> addLogisticsCompany(@RequestBody LogisticsCompanyDTO dto) {
        logisticsService.addLogisticsCompany(dto);
        return Result.success();
    }

    /**
     * 更新物流公司信息
     */
    @PutMapping("/company/{id}")
    public Result<Void> updateLogisticsCompany(@PathVariable Long id, @RequestBody LogisticsCompanyDTO dto) {
        logisticsService.updateLogisticsCompany(id, dto);
        return Result.success();
    }

    /**
     * 删除物流公司
     */
    @DeleteMapping("/company/{id}")
    public Result<Void> deleteLogisticsCompany(@PathVariable Long id) {
        logisticsService.deleteLogisticsCompany(id);
        return Result.success();
    }

    /**
     * 启用/禁用物流公司
     */
    @PutMapping("/company/{id}/status")
    public Result<Void> updateLogisticsCompanyStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        logisticsService.updateLogisticsCompanyStatus(id, status);
        return Result.success();
    }
}

