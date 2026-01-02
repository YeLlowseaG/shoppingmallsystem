package com.shoppingmall.service.permission;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public interface RoleService {

    /**
     * 获取角色列表（分页）
     */
    Page<RoleVO> getRoleList(Integer page, Integer pageSize, String roleCode, Integer status);

    /**
     * 获取所有角色列表
     */
    List<RoleVO> getAllRoles();

    /**
     * 根据ID获取角色信息
     */
    RoleVO getRoleById(Long id);

    /**
     * 新增角色
     */
    void addRole(RoleVO roleVO, List<Long> menuIds);

    /**
     * 更新角色
     */
    void updateRole(Long id, RoleVO roleVO, List<Long> menuIds);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 启用/禁用角色
     */
    void updateRoleStatus(Long id, Integer status);
}




































































