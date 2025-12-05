package com.shoppingmall.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.security.JwtUtil;
import com.shoppingmall.common.util.EncryptUtil;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.controller.common.CaptchaController;
import com.shoppingmall.dto.*;
import com.shoppingmall.entity.User;
import com.shoppingmall.entity.UserAudit;
import com.shoppingmall.repository.user.UserAuditRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.user.UserService;
import com.shoppingmall.vo.LoginVO;
import com.shoppingmall.vo.UserInfoVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAuditRepository userAuditRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final CaptchaController captchaController;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        // 验证验证码
        if (!captchaController.verifyCaptcha(registerDTO.getCaptchaId(), registerDTO.getCaptcha())) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        // 验证密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userRepository.selectCount(usernameWrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, registerDTO.getEmail());
        if (userRepository.selectCount(emailWrapper) > 0) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(EncryptUtil.bcryptEncode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setGender(registerDTO.getGender());
        user.setPhone(registerDTO.getPhone());
        user.setAddress(registerDTO.getAddress());
        user.setUserLevel(com.shoppingmall.common.constant.UserLevel.NORMAL);
        user.setStatus(com.shoppingmall.common.constant.UserStatus.PENDING);

        // 构建地区JSON
        Map<String, String> regionMap = new HashMap<>();
        regionMap.put("province", registerDTO.getProvince());
        regionMap.put("city", registerDTO.getCity());
        regionMap.put("district", registerDTO.getDistrict());
        try {
            user.setRegion(objectMapper.writeValueAsString(regionMap));
        } catch (Exception e) {
            log.error("构建地区JSON失败", e);
            throw new BusinessException(500, "注册失败，请重试");
        }

        // 保存用户
        userRepository.insert(user);

        // 创建审核记录
        UserAudit audit = new UserAudit();
        audit.setUserId(user.getId());
        audit.setAuditStatus(com.shoppingmall.common.constant.AuditStatus.PENDING);
        userAuditRepository.insert(audit);

        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userRepository.selectOne(wrapper);

        if (user == null) {
            log.warn("用户登录失败：用户名不存在 - {}", loginDTO.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 记录调试信息
        log.debug("用户登录验证 - 用户名: {}, 用户ID: {}, 状态: {}, 密码哈希长度: {}", 
                loginDTO.getUsername(), user.getId(), user.getStatus(), 
                user.getPassword() != null ? user.getPassword().length() : 0);

        // 验证密码
        boolean passwordMatches = EncryptUtil.bcryptMatches(loginDTO.getPassword(), user.getPassword());
        if (!passwordMatches) {
            log.warn("用户登录失败：密码错误 - 用户名: {}, 输入的密码: {}, 存储的密码哈希: {}", 
                    loginDTO.getUsername(), loginDTO.getPassword(), 
                    user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "..." : "null");
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 检查用户状态
        if (!com.shoppingmall.common.constant.UserStatus.ACTIVATED.equals(user.getStatus())) {
            log.warn("用户登录失败：账户未激活 - 用户名: {}, 状态: {}", loginDTO.getUsername(), user.getStatus());
            throw new BusinessException(403, "账户未激活，请联系管理员审核");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 构建用户信息
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);
        userInfo.setPhone(StringUtil.maskPhone(user.getPhone()));

        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userInfo);

        log.info("用户登录成功: {}", user.getUsername());
        return loginVO;
    }

    @Override
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, forgotPasswordDTO.getUsername());
        User user = userRepository.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(404, "该用户不存在！");
        }

        // TODO: 发送密码重置邮件到用户邮箱
        // 这里暂时只记录日志，后续实现邮件发送功能
        log.info("用户{}申请密码重置，邮箱: {}", user.getUsername(), user.getEmail());
        
        // 实际应该发送邮件，这里先抛出异常提示需要实现邮件功能
        // throw new BusinessException(500, "密码重置功能暂未实现，请联系管理员");
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);

        // 解析地区信息
        if (StringUtil.isNotBlank(user.getRegion())) {
            try {
                Map<String, String> regionMap = objectMapper.readValue(user.getRegion(), Map.class);
                userInfo.setProvince(regionMap.get("province"));
                userInfo.setCity(regionMap.get("city"));
                userInfo.setDistrict(regionMap.get("district"));
            } catch (Exception e) {
                log.error("解析地区信息失败", e);
            }
        }

        // 脱敏处理
        userInfo.setPhone(StringUtil.maskPhone(user.getPhone()));
        userInfo.setEmail(StringUtil.maskEmail(user.getEmail()));

        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserInfoDTO userInfoDTO) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 更新用户信息
        if (StringUtil.isNotBlank(userInfoDTO.getRealName())) {
            user.setRealName(userInfoDTO.getRealName());
        }
        if (userInfoDTO.getGender() != null) {
            user.setGender(userInfoDTO.getGender());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getPhone())) {
            user.setPhone(userInfoDTO.getPhone());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getAddress())) {
            user.setAddress(userInfoDTO.getAddress());
        }

        // 更新地区信息
        if (StringUtil.isNotBlank(userInfoDTO.getProvince()) &&
            StringUtil.isNotBlank(userInfoDTO.getCity()) &&
            StringUtil.isNotBlank(userInfoDTO.getDistrict())) {
            Map<String, String> regionMap = new HashMap<>();
            regionMap.put("province", userInfoDTO.getProvince());
            regionMap.put("city", userInfoDTO.getCity());
            regionMap.put("district", userInfoDTO.getDistrict());
            try {
                user.setRegion(objectMapper.writeValueAsString(regionMap));
            } catch (Exception e) {
                log.error("构建地区JSON失败", e);
            }
        }

        userRepository.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证旧密码
        if (!EncryptUtil.bcryptMatches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        // 更新密码
        user.setPassword(EncryptUtil.bcryptEncode(newPassword));
        userRepository.updateById(user);

        log.info("用户密码修改成功: userId={}", userId);
    }
}

