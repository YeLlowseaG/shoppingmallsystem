package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.WarehouseAddressDTO;
import com.shoppingmall.service.logistics.WarehouseAddressService;
import com.shoppingmall.vo.WarehouseAddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发货地址库管理控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
@RestController
@RequestMapping("/api/admin/warehouse-address")
@RequiredArgsConstructor
public class WarehouseAddressController {

    private final WarehouseAddressService warehouseAddressService;

    /**
     * 获取发货地址列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<WarehouseAddressVO>> getWarehouseAddressList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        Page<WarehouseAddressVO> result = warehouseAddressService.getWarehouseAddressList(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 获取所有启用的发货地址列表
     */
    @GetMapping("/all")
    public Result<List<WarehouseAddressVO>> getAllEnabledWarehouseAddresses() {
        List<WarehouseAddressVO> addresses = warehouseAddressService.getAllEnabledWarehouseAddresses();
        return Result.success(addresses);
    }

    /**
     * 获取默认发货地址
     */
    @GetMapping("/default")
    public Result<WarehouseAddressVO> getDefaultWarehouseAddress() {
        WarehouseAddressVO address = warehouseAddressService.getDefaultWarehouseAddress();
        return Result.success(address);
    }

    /**
     * 根据ID获取发货地址信息
     */
    @GetMapping("/{id}")
    public Result<WarehouseAddressVO> getWarehouseAddressById(@PathVariable Long id) {
        WarehouseAddressVO address = warehouseAddressService.getWarehouseAddressById(id);
        return Result.success(address);
    }

    /**
     * 新增发货地址
     */
    @PostMapping
    public Result<Void> addWarehouseAddress(@RequestBody WarehouseAddressDTO dto) {
        warehouseAddressService.addWarehouseAddress(dto);
        return Result.success();
    }

    /**
     * 更新发货地址信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateWarehouseAddress(@PathVariable Long id, @RequestBody WarehouseAddressDTO dto) {
        warehouseAddressService.updateWarehouseAddress(id, dto);
        return Result.success();
    }

    /**
     * 删除发货地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteWarehouseAddress(@PathVariable Long id) {
        warehouseAddressService.deleteWarehouseAddress(id);
        return Result.success();
    }

    /**
     * 启用/禁用发货地址
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateWarehouseAddressStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        warehouseAddressService.updateWarehouseAddressStatus(id, status);
        return Result.success();
    }

    /**
     * 设置默认发货地址
     */
    @PutMapping("/{id}/set-default")
    public Result<Void> setDefaultWarehouseAddress(@PathVariable Long id) {
        warehouseAddressService.setDefaultWarehouseAddress(id);
        return Result.success();
    }
}

