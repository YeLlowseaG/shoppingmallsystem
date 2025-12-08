package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.security.JwtUtil;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.AdminLoginDTO;
import com.shoppingmall.dto.AdminUserDTO;
import com.shoppingmall.service.permission.AdminUserService;
import com.shoppingmall.vo.AdminInfoVO;
import com.shoppingmall.vo.AdminLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员用户控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        AdminLoginVO loginVO = adminUserService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/info")
    public Result<AdminInfoVO> getAdminInfo() {
        // 从拦截器中获取管理员ID
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.error(401, "未授权，请重新登录");
        }
        AdminInfoVO adminInfo = adminUserService.getAdminInfo(adminId);
        return Result.success(adminInfo);
    }

    /**
     * 获取管理员列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<AdminInfoVO>> getAdminUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status
    ) {
        Page<AdminInfoVO> result = adminUserService.getAdminUserList(page, pageSize, username, status);
        return Result.success(result);
    }

    /**
     * 根据ID获取管理员信息
     */
    @GetMapping("/{id}")
    public Result<AdminInfoVO> getAdminUserById(@PathVariable Long id) {
        AdminInfoVO adminInfo = adminUserService.getAdminUserById(id);
        return Result.success(adminInfo);
    }

    /**
     * 新增管理员
     */
    @PostMapping
    public Result<Void> addAdminUser(
            @RequestBody AdminUserDTO adminUserDTO,
            @RequestParam(required = false) List<Long> roleIds
    ) {
        adminUserService.addAdminUser(adminUserDTO, roleIds);
        return Result.success();
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateAdminUser(
            @PathVariable Long id,
            @RequestBody AdminUserDTO adminUserDTO,
            @RequestParam(required = false) List<Long> roleIds
    ) {
        adminUserService.updateAdminUser(id, adminUserDTO, roleIds);
        return Result.success();
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdminUser(@PathVariable Long id) {
        adminUserService.deleteAdminUser(id);
        return Result.success();
    }

    /**
     * 启用/禁用管理员
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateAdminUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        adminUserService.updateAdminUserStatus(id, status);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String password
    ) {
        adminUserService.resetPassword(id, password);
        return Result.success();
    }
}

