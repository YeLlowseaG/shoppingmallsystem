package com.shoppingmall.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.security.JwtUtil;
import com.shoppingmall.common.util.EncryptUtil;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.controller.common.CaptchaController;
import com.shoppingmall.dto.*;
import com.shoppingmall.entity.User;
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
import com.shoppingmall.entity.PasswordResetCode;
import com.shoppingmall.repository.user.PasswordResetCodeRepository;
import com.shoppingmall.service.EmailService;
import com.shoppingmall.dto.ResetPasswordDTO;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

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
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final CaptchaController captchaController;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final EmailService emailService;

    @Value("${app.password.reset.token-expire-minutes:30}")
    private int tokenExpireMinutes;

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
        // 注册后直接激活，无需审核
        user.setStatus(com.shoppingmall.common.constant.UserStatus.ACTIVATED);

        // 设置出生日期（从年、月、日组合）
        if (registerDTO.getBirthYear() != null && registerDTO.getBirthMonth() != null && registerDTO.getBirthDay() != null) {
            try {
                java.time.LocalDate birthday = java.time.LocalDate.of(
                    registerDTO.getBirthYear(),
                    registerDTO.getBirthMonth(),
                    registerDTO.getBirthDay()
                );
                user.setBirthday(birthday);
            } catch (Exception e) {
                log.warn("出生日期格式错误: {}-{}-{}", registerDTO.getBirthYear(), registerDTO.getBirthMonth(), registerDTO.getBirthDay(), e);
            }
        }

        // 设置其他可选字段
        user.setOperator(registerDTO.getOperator());
        user.setFixedPhone(registerDTO.getFixedPhone());
        user.setZipCode(registerDTO.getZipCode());
        user.setSecurityQuestion(registerDTO.getSecurityQuestion());
        user.setSecurityAnswer(registerDTO.getSecurityAnswer());
        user.setWangwang(registerDTO.getWangwang());

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

        // 注册后直接激活，不再创建审核记录
        log.info("用户注册成功并自动激活: {}", user.getUsername());
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

        // 检查用户状态（只检查是否禁用，不再检查是否激活）
        if (com.shoppingmall.common.constant.UserStatus.DISABLED.equals(user.getStatus())) {
            log.warn("用户登录失败：账户已禁用 - 用户名: {}, 状态: {}", loginDTO.getUsername(), user.getStatus());
            throw new BusinessException(403, "账户已禁用，请联系管理员");
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
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        // 验证邮箱必填
        if (forgotPasswordDTO.getEmail() == null || forgotPasswordDTO.getEmail().trim().isEmpty()) {
            throw new BusinessException(400, "请输入邮箱");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, forgotPasswordDTO.getUsername());
        User user = userRepository.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(404, "该用户不存在！");
        }

        // 验证邮箱是否匹配
        if (user.getEmail() == null || !user.getEmail().equals(forgotPasswordDTO.getEmail().trim())) {
            throw new BusinessException(400, "您填写的邮箱与注册时的不一致，请重新填写");
        }

        // 生成重置验证码（使用UUID，取16位大写字母数字）
        String resetCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        
        // 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(tokenExpireMinutes);
        
        // 创建密码重置记录
        PasswordResetCode passwordResetCode = new PasswordResetCode();
        passwordResetCode.setUserId(user.getId());
        passwordResetCode.setUsername(user.getUsername());
        passwordResetCode.setEmail(user.getEmail());
        passwordResetCode.setCode(resetCode);
        passwordResetCode.setCodeType("RESET_PASSWORD");
        passwordResetCode.setStatus(0); // 0-未使用
        passwordResetCode.setExpireTime(expireTime);
        
        // 发送邮件并获取邮件内容
        try {
            String emailContent = emailService.sendPasswordResetEmail(
                user.getEmail(), 
                user.getUsername(), 
                resetCode,
                tokenExpireMinutes
            );
            passwordResetCode.setEmailContent(emailContent);
            
            // 保存到数据库
            passwordResetCodeRepository.insert(passwordResetCode);
            
            log.info("密码重置验证码已发送: userId={}, username={}, email={}, code={}, expireTime={}", 
                    user.getId(), user.getUsername(), user.getEmail(), resetCode, expireTime);
        } catch (Exception e) {
            log.error("发送密码重置邮件失败: userId={}, username={}, email={}", 
                    user.getId(), user.getUsername(), user.getEmail(), e);
            throw new BusinessException(500, "邮件发送失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 验证密码一致性
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 查询重置验证码记录
        LambdaQueryWrapper<PasswordResetCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PasswordResetCode::getCode, resetPasswordDTO.getCode().trim().toUpperCase());
        wrapper.eq(PasswordResetCode::getStatus, 0); // 0-未使用
        wrapper.eq(PasswordResetCode::getCodeType, "RESET_PASSWORD");
        PasswordResetCode passwordResetCode = passwordResetCodeRepository.selectOne(wrapper);

        if (passwordResetCode == null) {
            throw new BusinessException(400, "验证码无效或已使用");
        }

        // 验证是否过期
        if (passwordResetCode.getExpireTime().isBefore(LocalDateTime.now())) {
            // 标记为已过期
            passwordResetCode.setStatus(2); // 2-已过期
            passwordResetCodeRepository.updateById(passwordResetCode);
            throw new BusinessException(400, "验证码已过期，请重新申请");
        }

        // 查询用户
        User user = userRepository.selectById(passwordResetCode.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 更新用户密码
        user.setPassword(EncryptUtil.bcryptEncode(resetPasswordDTO.getNewPassword()));
        userRepository.updateById(user);

        // 标记验证码为已使用
        passwordResetCode.setStatus(1); // 1-已使用
        passwordResetCode.setUsedTime(LocalDateTime.now());
        passwordResetCodeRepository.updateById(passwordResetCode);

        log.info("用户通过验证码重置密码成功: userId={}, username={}, code={}", 
                user.getId(), user.getUsername(), resetPasswordDTO.getCode());
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

        // 注意：个人信息页面需要完整信息用于编辑，所以不进行脱敏处理
        // 如果需要脱敏，可以在其他接口中处理
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());

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
        if (StringUtil.isNotBlank(userInfoDTO.getEmail())) {
            user.setEmail(userInfoDTO.getEmail());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getPhone())) {
            user.setPhone(userInfoDTO.getPhone());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getAddress())) {
            user.setAddress(userInfoDTO.getAddress());
        }
        if (userInfoDTO.getBirthday() != null) {
            user.setBirthday(userInfoDTO.getBirthday());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getZipCode())) {
            user.setZipCode(userInfoDTO.getZipCode());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getFixedPhone())) {
            user.setFixedPhone(userInfoDTO.getFixedPhone());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getSecurityQuestion())) {
            user.setSecurityQuestion(userInfoDTO.getSecurityQuestion());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getSecurityAnswer())) {
            user.setSecurityAnswer(userInfoDTO.getSecurityAnswer());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getWangwang())) {
            user.setWangwang(userInfoDTO.getWangwang());
        }
        if (StringUtil.isNotBlank(userInfoDTO.getOperator())) {
            user.setOperator(userInfoDTO.getOperator());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePaymentPassword(Long userId, String oldPaymentPassword, String newPaymentPassword) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证原支付密码
        // 如果用户没有设置过支付密码，则使用登录密码作为默认支付密码
        boolean passwordValid = false;
        if (user.getPaymentPassword() == null || user.getPaymentPassword().isEmpty()) {
            // 未设置过支付密码，使用登录密码验证
            passwordValid = EncryptUtil.bcryptMatches(oldPaymentPassword, user.getPassword());
        } else {
            // 已设置过支付密码，使用支付密码验证
            passwordValid = EncryptUtil.bcryptMatches(oldPaymentPassword, user.getPaymentPassword());
        }

        if (!passwordValid) {
            throw new BusinessException(400, "原支付密码错误");
        }

        // 更新支付密码
        user.setPaymentPassword(EncryptUtil.bcryptEncode(newPaymentPassword));
        userRepository.updateById(user);

        log.info("用户支付密码修改成功: userId={}", userId);
    }
}

