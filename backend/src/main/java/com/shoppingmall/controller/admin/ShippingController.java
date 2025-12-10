package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ShippingMethodDTO;
import com.shoppingmall.dto.ShippingTemplateDTO;
import com.shoppingmall.service.logistics.ShippingService;
import com.shoppingmall.vo.ShippingMethodVO;
import com.shoppingmall.vo.ShippingTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配送方式管理控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@RestController("adminShippingController")
@RequestMapping("/api/admin/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    // ==================== 配送方式管理 ====================

    /**
     * 获取配送方式列表（分页）
     */
    @GetMapping("/method/list")
    public Result<Page<ShippingMethodVO>> getShippingMethodList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        Page<ShippingMethodVO> result = shippingService.getShippingMethodList(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 根据ID获取配送方式信息
     */
    @GetMapping("/method/{id}")
    public Result<ShippingMethodVO> getShippingMethodById(@PathVariable Long id) {
        ShippingMethodVO method = shippingService.getShippingMethodById(id);
        return Result.success(method);
    }

    /**
     * 新增配送方式
     */
    @PostMapping("/method")
    public Result<Void> addShippingMethod(@RequestBody ShippingMethodDTO dto) {
        shippingService.addShippingMethod(dto);
        return Result.success();
    }

    /**
     * 更新配送方式信息
     */
    @PutMapping("/method/{id}")
    public Result<Void> updateShippingMethod(@PathVariable Long id, @RequestBody ShippingMethodDTO dto) {
        shippingService.updateShippingMethod(id, dto);
        return Result.success();
    }

    /**
     * 删除配送方式
     */
    @DeleteMapping("/method/{id}")
    public Result<Void> deleteShippingMethod(@PathVariable Long id) {
        shippingService.deleteShippingMethod(id);
        return Result.success();
    }

    /**
     * 启用/禁用配送方式
     */
    @PutMapping("/method/{id}/status")
    public Result<Void> updateShippingMethodStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        shippingService.updateShippingMethodStatus(id, status);
        return Result.success();
    }

    // ==================== 运费模板管理 ====================

    /**
     * 获取运费模板列表（分页）
     */
    @GetMapping("/template/list")
    public Result<Page<ShippingTemplateVO>> getShippingTemplateList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        Page<ShippingTemplateVO> result = shippingService.getShippingTemplateList(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 获取所有启用的运费模板列表
     */
    @GetMapping("/template/all")
    public Result<List<ShippingTemplateVO>> getAllEnabledShippingTemplates() {
        List<ShippingTemplateVO> templates = shippingService.getAllEnabledShippingTemplates();
        return Result.success(templates);
    }

    /**
     * 根据ID获取运费模板信息
     */
    @GetMapping("/template/{id}")
    public Result<ShippingTemplateVO> getShippingTemplateById(@PathVariable Long id) {
        ShippingTemplateVO template = shippingService.getShippingTemplateById(id);
        return Result.success(template);
    }

    /**
     * 新增运费模板
     */
    @PostMapping("/template")
    public Result<Void> addShippingTemplate(@RequestBody ShippingTemplateDTO dto) {
        shippingService.addShippingTemplate(dto);
        return Result.success();
    }

    /**
     * 更新运费模板信息
     */
    @PutMapping("/template/{id}")
    public Result<Void> updateShippingTemplate(@PathVariable Long id, @RequestBody ShippingTemplateDTO dto) {
        shippingService.updateShippingTemplate(id, dto);
        return Result.success();
    }

    /**
     * 删除运费模板
     */
    @DeleteMapping("/template/{id}")
    public Result<Void> deleteShippingTemplate(@PathVariable Long id) {
        shippingService.deleteShippingTemplate(id);
        return Result.success();
    }

    /**
     * 启用/禁用运费模板
     */
    @PutMapping("/template/{id}/status")
    public Result<Void> updateShippingTemplateStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        shippingService.updateShippingTemplateStatus(id, status);
        return Result.success();
    }
}

