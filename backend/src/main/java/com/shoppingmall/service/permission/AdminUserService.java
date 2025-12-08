package com.shoppingmall.service.permission;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.AdminLoginDTO;
import com.shoppingmall.vo.AdminInfoVO;
import com.shoppingmall.vo.AdminLoginVO;

import java.util.List;

/**
 * 管理员用户服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public interface AdminUserService {

    /**
     * 管理员登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（Token和用户信息）
     */
    AdminLoginVO login(AdminLoginDTO loginDTO);

    /**
     * 获取当前管理员信息
     *
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    AdminInfoVO getAdminInfo(Long adminId);

    /**
     * 获取管理员列表（分页）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param username 用户名（可选）
     * @param status   状态（可选）
     * @return 管理员列表
     */
    Page<AdminInfoVO> getAdminUserList(Integer page, Integer pageSize, String username, Integer status);

    /**
     * 根据ID获取管理员信息
     *
     * @param id 管理员ID
     * @return 管理员信息
     */
    AdminInfoVO getAdminUserById(Long id);

    /**
     * 新增管理员
     *
     * @param adminUserDTO 管理员信息
     * @param roleIds      角色ID列表
     */
    void addAdminUser(com.shoppingmall.dto.AdminUserDTO adminUserDTO, List<Long> roleIds);

    /**
     * 更新管理员信息
     *
     * @param id           管理员ID
     * @param adminUserDTO 管理员信息
     * @param roleIds      角色ID列表
     */
    void updateAdminUser(Long id, com.shoppingmall.dto.AdminUserDTO adminUserDTO, List<Long> roleIds);

    /**
     * 删除管理员
     *
     * @param id 管理员ID
     */
    void deleteAdminUser(Long id);

    /**
     * 启用/禁用管理员
     *
     * @param id     管理员ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateAdminUserStatus(Long id, Integer status);

    /**
     * 重置密码
     *
     * @param id       管理员ID
     * @param password 新密码
     */
    void resetPassword(Long id, String password);
}

