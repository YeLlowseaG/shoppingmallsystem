package com.shoppingmall.service.permission.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.entity.AdminRole;
import com.shoppingmall.entity.Role;
import com.shoppingmall.entity.RoleMenu;
import com.shoppingmall.repository.permission.AdminRoleRepository;
import com.shoppingmall.repository.permission.RoleMenuRepository;
import com.shoppingmall.repository.permission.RoleRepository;
import com.shoppingmall.service.permission.RoleService;
import com.shoppingmall.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Override
    public Page<RoleVO> getRoleList(Integer page, Integer pageSize, String roleCode, Integer status) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getDeleted, 0);
        if (StringUtils.hasText(roleCode)) {
            wrapper.like(Role::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }
        wrapper.orderByAsc(Role::getSortOrder);

        Page<Role> rolePage = new Page<>(page, pageSize);
        Page<Role> result = roleRepository.selectPage(rolePage, wrapper);

        Page<RoleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<RoleVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<RoleVO> getAllRoles() {
        List<Role> roles = roleRepository.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getDeleted, 0)
                        .eq(Role::getStatus, 1)
                        .orderByAsc(Role::getSortOrder)
        );
        return roles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleById(Long id) {
        Role role = roleRepository.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        RoleVO vo = convertToVO(role);
        
        // 获取角色的菜单权限ID列表
        List<RoleMenu> roleMenus = roleMenuRepository.selectList(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id)
        );
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
        vo.setMenuIds(menuIds);
        
        return vo;
    }

    @Override
    @Transactional
    public void addRole(RoleVO roleVO, List<Long> menuIds) {
        // 检查角色编码是否已存在
        Role existRole = roleRepository.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, roleVO.getRoleCode())
                        .eq(Role::getDeleted, 0)
        );
        if (existRole != null) {
            throw new BusinessException("角色编码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(roleVO, role);
        role.setStatus(roleVO.getStatus() != null ? roleVO.getStatus() : 1);
        roleRepository.insert(role);

        // 分配菜单权限
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(role.getId());
                roleMenu.setMenuId(menuId);
                roleMenuRepository.insert(roleMenu);
            }
        }
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleVO roleVO, List<Long> menuIds) {
        Role role = roleRepository.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        BeanUtils.copyProperties(roleVO, role);
        roleRepository.updateById(role);

        // 更新菜单权限
        roleMenuRepository.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id)
        );
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(id);
                roleMenu.setMenuId(menuId);
                roleMenuRepository.insert(roleMenu);
            }
        }
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        // 检查是否有关联用户
        List<AdminRole> adminRoles = adminRoleRepository.selectList(
                new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getRoleId, id)
        );
        if (!adminRoles.isEmpty()) {
            throw new BusinessException("该角色下还有关联用户，无法删除");
        }

        // 逻辑删除
        role.setDeleted(1);
        roleRepository.updateById(role);

        // 删除角色菜单关联
        roleMenuRepository.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id)
        );
    }

    @Override
    public void updateRoleStatus(Long id, Integer status) {
        Role role = roleRepository.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        role.setStatus(status);
        roleRepository.updateById(role);
    }

    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
