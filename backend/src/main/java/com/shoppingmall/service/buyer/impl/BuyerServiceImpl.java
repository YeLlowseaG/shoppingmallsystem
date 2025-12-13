package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.UserLevel;
import com.shoppingmall.common.constant.UserStatus;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.BuyerDTO;
import com.shoppingmall.entity.User;
import com.shoppingmall.entity.UserAudit;
import com.shoppingmall.repository.user.UserAuditRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.buyer.BuyerService;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.vo.BuyerVO;
import com.shoppingmall.vo.MemberLevelVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购者管理服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerServiceImpl implements BuyerService {

    private final UserRepository userRepository;
    private final UserAuditRepository userAuditRepository;
    private final MemberLevelService memberLevelService;
    private final ObjectMapper objectMapper;
    
    // 缓存会员等级映射（提高性能）
    private Map<Long, String> memberLevelCache = null;

    @Override
    public Page<BuyerVO> getBuyerList(Integer page, Integer pageSize, String username, String phone, Integer status, Integer userLevel) {
        Page<User> userPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            wrapper.like(User::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (userLevel != null) {
            wrapper.eq(User::getUserLevel, userLevel);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userRepository.selectPage(userPage, wrapper);
        
        Page<BuyerVO> voPage = new Page<>(page, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());
        
        return voPage;
    }

    @Override
    public BuyerVO getBuyerById(Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "采购者不存在");
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuyer(Long id, BuyerDTO buyerDTO, Long auditorId) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "采购者不存在");
        }

        if (buyerDTO.getUserLevel() != null) {
            user.setUserLevel(buyerDTO.getUserLevel());
        }
        if (buyerDTO.getStatus() != null) {
            user.setStatus(buyerDTO.getStatus());
        }

        userRepository.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuyerStatus(Long id, Integer status) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "采购者不存在");
        }
        user.setStatus(status);
        userRepository.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuyerLevel(Long id, Integer userLevel) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "采购者不存在");
        }
        user.setUserLevel(userLevel);
        userRepository.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBuyer(Long id, BuyerDTO buyerDTO, Long auditorId) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "采购者不存在");
        }

        // 更新用户状态
        if (buyerDTO.getAuditStatus() == 1) {
            // 审核通过，激活用户
            user.setStatus(UserStatus.ACTIVATED);
        } else if (buyerDTO.getAuditStatus() == 2) {
            // 审核拒绝，保持待审核状态或设置为禁用
            user.setStatus(UserStatus.PENDING);
        }
        userRepository.updateById(user);

        // 创建或更新审核记录
        LambdaQueryWrapper<UserAudit> auditWrapper = new LambdaQueryWrapper<>();
        auditWrapper.eq(UserAudit::getUserId, id);
        auditWrapper.orderByDesc(UserAudit::getCreateTime);
        auditWrapper.last("LIMIT 1");
        UserAudit userAudit = userAuditRepository.selectOne(auditWrapper);

        if (userAudit == null) {
            userAudit = new UserAudit();
            userAudit.setUserId(id);
        }

        userAudit.setAuditStatus(buyerDTO.getAuditStatus());
        userAudit.setAuditComment(buyerDTO.getAuditComment());
        userAudit.setAuditorId(auditorId);
        userAudit.setAuditTime(LocalDateTime.now());

        if (userAudit.getId() == null) {
            userAuditRepository.insert(userAudit);
        } else {
            userAuditRepository.updateById(userAudit);
        }
    }

    @Override
    public Page<BuyerVO> getPendingAuditList(Integer page, Integer pageSize, String username, String realName, String phone) {
        Page<User> userPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, UserStatus.PENDING);
        
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (realName != null && !realName.trim().isEmpty()) {
            wrapper.like(User::getRealName, realName);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            wrapper.like(User::getPhone, phone);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userRepository.selectPage(userPage, wrapper);
        
        Page<BuyerVO> voPage = new Page<>(page, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());
        
        return voPage;
    }

    /**
     * 转换为VO对象
     */
    private BuyerVO convertToVO(User user) {
        BuyerVO vo = new BuyerVO();
        BeanUtils.copyProperties(user, vo);

        // 解析地区信息
        if (user.getRegion() != null && !user.getRegion().isEmpty()) {
            try {
                Map<String, String> regionMap = objectMapper.readValue(user.getRegion(), HashMap.class);
                vo.setProvince(regionMap.get("province"));
                vo.setCity(regionMap.get("city"));
                vo.setDistrict(regionMap.get("district"));
            } catch (Exception e) {
                log.warn("解析地区信息失败: {}", user.getRegion(), e);
            }
        }

        // 设置用户等级名称（从会员等级表查询）
        if (user.getUserLevel() != null) {
            String levelName = getMemberLevelName(user.getUserLevel());
            vo.setUserLevelName(levelName != null ? levelName : "未知");
        }

        // 设置状态名称
        if (user.getStatus() != null) {
            switch (user.getStatus()) {
                case 0:
                    vo.setStatusName("待审核");
                    break;
                case 1:
                    vo.setStatusName("已激活");
                    break;
                case 2:
                    vo.setStatusName("已禁用");
                    break;
                default:
                    vo.setStatusName("未知");
            }
        }

        // 查询审核信息
        LambdaQueryWrapper<UserAudit> auditWrapper = new LambdaQueryWrapper<>();
        auditWrapper.eq(UserAudit::getUserId, user.getId());
        auditWrapper.orderByDesc(UserAudit::getCreateTime);
        auditWrapper.last("LIMIT 1");
        UserAudit userAudit = userAuditRepository.selectOne(auditWrapper);

        if (userAudit != null) {
            vo.setAuditStatus(userAudit.getAuditStatus());
            vo.setAuditComment(userAudit.getAuditComment());
            vo.setAuditorId(userAudit.getAuditorId());
            vo.setAuditTime(userAudit.getAuditTime());

            // 设置审核状态名称
            switch (userAudit.getAuditStatus()) {
                case 0:
                    vo.setAuditStatusName("待审核");
                    break;
                case 1:
                    vo.setAuditStatusName("已通过");
                    break;
                case 2:
                    vo.setAuditStatusName("已拒绝");
                    break;
                default:
                    vo.setAuditStatusName("未知");
            }
        } else {
            vo.setAuditStatus(0);
            vo.setAuditStatusName("待审核");
        }

        return vo;
    }

    /**
     * 获取会员等级名称（带缓存）
     *
     * @param levelId 等级ID（Integer类型，转换为Long）
     * @return 等级名称
     */
    private String getMemberLevelName(Integer levelId) {
        if (levelId == null) {
            return null;
        }
        
        // 将Integer转换为Long
        Long levelIdLong = levelId.longValue();
        
        // 如果缓存为空，初始化缓存
        if (memberLevelCache == null) {
            refreshMemberLevelCache();
        }
        
        // 从缓存中获取等级名称
        String levelName = memberLevelCache.get(levelIdLong);
        
        // 如果缓存中没有，尝试重新加载缓存（可能等级被删除了）
        if (levelName == null) {
            refreshMemberLevelCache();
            levelName = memberLevelCache.get(levelIdLong);
        }
        
        return levelName;
    }

    /**
     * 刷新会员等级缓存
     */
    private void refreshMemberLevelCache() {
        try {
            List<MemberLevelVO> levels = memberLevelService.getAllEnabledMemberLevels();
            memberLevelCache = levels.stream()
                    .collect(Collectors.toMap(
                            MemberLevelVO::getId,
                            MemberLevelVO::getLevelName,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.warn("加载会员等级缓存失败", e);
            memberLevelCache = new HashMap<>();
        }
    }
}


