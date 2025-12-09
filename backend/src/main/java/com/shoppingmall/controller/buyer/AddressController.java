package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.AddressDTO;
import com.shoppingmall.service.buyer.AddressService;
import com.shoppingmall.vo.AddressVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-08
 */
@RestController
@RequestMapping("/api/buyer/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final HttpServletRequest request;

    /**
     * 获取收货地址列表
     */
    @GetMapping
    public Result<List<AddressVO>> getAddressList() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        List<AddressVO> addresses = addressService.getAddressList(userId);
        return Result.success(addresses);
    }

    /**
     * 根据ID获取收货地址详情
     */
    @GetMapping("/{id}")
    public Result<AddressVO> getAddressById(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        AddressVO address = addressService.getAddressById(id, userId);
        return Result.success(address);
    }

    /**
     * 新增收货地址
     */
    @PostMapping
    public Result<Long> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        Long addressId = addressService.addAddress(userId, addressDTO);
        return Result.success("新增成功", addressId);
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/{id}")
    public Result<?> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDTO addressDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        addressService.updateAddress(id, userId, addressDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteAddress(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        addressService.deleteAddress(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 设置默认收货地址
     */
    @PutMapping("/{id}/default")
    public Result<?> setDefaultAddress(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        addressService.setDefaultAddress(id, userId);
        return Result.success("设置成功");
    }
}

