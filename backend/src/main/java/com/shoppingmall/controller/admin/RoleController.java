package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.permission.RoleService;
import com.shoppingmall.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@RestController
@RequestMapping("/api/admin/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取角色列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<RoleVO>> getRoleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status
    ) {
        Page<RoleVO> result = roleService.getRoleList(page, pageSize, roleCode, status);
        return Result.success(result);
    }

    /**
     * 获取所有角色列表
     */
    @GetMapping("/all")
    public Result<List<RoleVO>> getAllRoles() {
        List<RoleVO> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(@PathVariable Long id) {
        RoleVO role = roleService.getRoleById(id);
        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result<Void> addRole(
            @RequestBody RoleVO roleVO,
            @RequestParam(required = false) List<Long> menuIds
    ) {
        roleService.addRole(roleVO, menuIds);
        return Result.success();
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public Result<Void> updateRole(
            @PathVariable Long id,
            @RequestBody RoleVO roleVO,
            @RequestParam(required = false) List<Long> menuIds
    ) {
        roleService.updateRole(id, roleVO, menuIds);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 启用/禁用角色
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateRoleStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        roleService.updateRoleStatus(id, status);
        return Result.success();
    }
}


































