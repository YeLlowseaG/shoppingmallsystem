package com.shoppingmall.service.permission.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.security.JwtUtil;
import com.shoppingmall.common.util.EncryptUtil;
import com.shoppingmall.dto.AdminLoginDTO;
import com.shoppingmall.entity.AdminRole;
import com.shoppingmall.entity.AdminUser;
import com.shoppingmall.entity.Role;
import com.shoppingmall.repository.permission.AdminRoleRepository;
import com.shoppingmall.repository.permission.AdminUserRepository;
import com.shoppingmall.repository.permission.RoleRepository;
import com.shoppingmall.service.permission.AdminUserService;
import com.shoppingmall.vo.AdminInfoVO;
import com.shoppingmall.vo.AdminLoginVO;
import com.shoppingmall.vo.MenuVO;
import com.shoppingmall.vo.RoleVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.shoppingmall.service.permission.PermissionService permissionService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 获取当前管理员ID（从拦截器中获取）
     */
    private Long getCurrentAdminId() {
        Long adminId = (Long) request.getAttribute("adminId");
        return adminId != null ? adminId : 0L; // 如果没有，返回0表示系统创建
    }

    @Override
    public AdminLoginVO login(AdminLoginDTO loginDTO) {
        // 查询管理员
        AdminUser adminUser = adminUserRepository.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, loginDTO.getUsername())
                        .eq(AdminUser::getDeleted, 0)
        );

        if (adminUser == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!EncryptUtil.bcryptMatches(loginDTO.getPassword(), adminUser.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (adminUser.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新最后登录时间和IP
        adminUser.setLastLoginTime(LocalDateTime.now());
        String clientIp = getClientIp();
        adminUser.setLastLoginIp(clientIp);
        adminUserRepository.updateById(adminUser);

        // 生成Token
        String token = jwtUtil.generateToken(adminUser.getId(), adminUser.getUsername());

        // 获取管理员信息
        AdminInfoVO adminInfo = convertToVO(adminUser);

        // 获取菜单和权限
        List<MenuVO> menus = permissionService.getMenusByAdminId(adminUser.getId());
        List<String> permissions = permissionService.getPermissionsByAdminId(adminUser.getId());

        // 构建响应
        AdminLoginVO loginVO = new AdminLoginVO();
        loginVO.setToken(token);
        loginVO.setAdminInfo(adminInfo);
        loginVO.setMenus(menus);
        loginVO.setPermissions(permissions);

        return loginVO;
    }

    @Override
    public AdminInfoVO getAdminInfo(Long adminId) {
        AdminUser adminUser = adminUserRepository.selectById(adminId);
        if (adminUser == null) {
            throw new BusinessException("管理员不存在");
        }
        return convertToVO(adminUser);
    }

    @Override
    public Page<AdminInfoVO> getAdminUserList(Integer page, Integer pageSize, String username, Integer status) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getDeleted, 0);
        if (StringUtils.hasText(username)) {
            wrapper.like(AdminUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(AdminUser::getStatus, status);
        }
        wrapper.orderByDesc(AdminUser::getCreateTime);

        Page<AdminUser> userPage = new Page<>(page, pageSize);
        Page<AdminUser> result = adminUserRepository.selectPage(userPage, wrapper);

        // 转换为VO
        Page<AdminInfoVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<AdminInfoVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AdminInfoVO getAdminUserById(Long id) {
        AdminUser adminUser = adminUserRepository.selectById(id);
        if (adminUser == null || adminUser.getDeleted() == 1) {
            throw new BusinessException("管理员不存在");
        }
        return convertToVO(adminUser);
    }

    @Override
    @Transactional
    public void addAdminUser(com.shoppingmall.dto.AdminUserDTO adminUserDTO, List<Long> roleIds) {
        // 检查用户名是否已存在
        AdminUser existUser = adminUserRepository.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, adminUserDTO.getUsername())
                        .eq(AdminUser::getDeleted, 0)
        );
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建管理员
        AdminUser adminUser = new AdminUser();
        BeanUtils.copyProperties(adminUserDTO, adminUser);
        adminUser.setPassword(EncryptUtil.bcryptEncode(adminUserDTO.getPassword()));
        adminUser.setStatus(adminUserDTO.getStatus() != null ? adminUserDTO.getStatus() : 1);
        adminUser.setCreatorId(getCurrentAdminId());
        adminUserRepository.insert(adminUser);

        // 分配角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                AdminRole adminRole = new AdminRole();
                adminRole.setAdminId(adminUser.getId());
                adminRole.setRoleId(roleId);
                adminRoleRepository.insert(adminRole);
            }
        }
    }

    @Override
    @Transactional
    public void updateAdminUser(Long id, com.shoppingmall.dto.AdminUserDTO adminUserDTO, List<Long> roleIds) {
        AdminUser adminUser = adminUserRepository.selectById(id);
        if (adminUser == null || adminUser.getDeleted() == 1) {
            throw new BusinessException("管理员不存在");
        }

        // 更新基本信息
        if (StringUtils.hasText(adminUserDTO.getRealName())) {
            adminUser.setRealName(adminUserDTO.getRealName());
        }
        if (StringUtils.hasText(adminUserDTO.getEmail())) {
            adminUser.setEmail(adminUserDTO.getEmail());
        }
        if (StringUtils.hasText(adminUserDTO.getPhone())) {
            adminUser.setPhone(adminUserDTO.getPhone());
        }
        if (adminUserDTO.getStatus() != null) {
            adminUser.setStatus(adminUserDTO.getStatus());
        }
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(adminUserDTO.getPassword())) {
            adminUser.setPassword(EncryptUtil.bcryptEncode(adminUserDTO.getPassword()));
        }
        adminUserRepository.updateById(adminUser);

        // 更新角色（先删除旧角色，再添加新角色）
        adminRoleRepository.delete(
                new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, id)
        );
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                AdminRole adminRole = new AdminRole();
                adminRole.setAdminId(id);
                adminRole.setRoleId(roleId);
                adminRoleRepository.insert(adminRole);
            }
        }
    }

    @Override
    @Transactional
    public void deleteAdminUser(Long id) {
        AdminUser adminUser = adminUserRepository.selectById(id);
        if (adminUser == null || adminUser.getDeleted() == 1) {
            throw new BusinessException("管理员不存在");
        }
        // 逻辑删除
        adminUser.setDeleted(1);
        adminUserRepository.updateById(adminUser);
    }

    @Override
    public void updateAdminUserStatus(Long id, Integer status) {
        AdminUser adminUser = adminUserRepository.selectById(id);
        if (adminUser == null || adminUser.getDeleted() == 1) {
            throw new BusinessException("管理员不存在");
        }
        adminUser.setStatus(status);
        adminUserRepository.updateById(adminUser);
    }

    @Override
    public void resetPassword(Long id, String password) {
        AdminUser adminUser = adminUserRepository.selectById(id);
        if (adminUser == null || adminUser.getDeleted() == 1) {
            throw new BusinessException("管理员不存在");
        }
        adminUser.setPassword(EncryptUtil.bcryptEncode(password));
        adminUserRepository.updateById(adminUser);
    }

    /**
     * 转换为VO
     */
    private AdminInfoVO convertToVO(AdminUser adminUser) {
        AdminInfoVO vo = new AdminInfoVO();
        BeanUtils.copyProperties(adminUser, vo);

        // 获取角色列表
        List<AdminRole> adminRoles = adminRoleRepository.selectList(
                new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, adminUser.getId())
        );
        List<RoleVO> roles = new ArrayList<>();
        for (AdminRole adminRole : adminRoles) {
            Role role = roleRepository.selectById(adminRole.getRoleId());
            if (role != null) {
                RoleVO roleVO = new RoleVO();
                BeanUtils.copyProperties(role, roleVO);
                roles.add(roleVO);
            }
        }
        vo.setRoles(roles);

        return vo;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

