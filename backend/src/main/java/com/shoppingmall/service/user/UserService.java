package com.shoppingmall.service.user;

import com.shoppingmall.dto.*;
import com.shoppingmall.vo.LoginVO;
import com.shoppingmall.vo.UserInfoVO;

/**
 * 用户服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（Token和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 忘记密码
     *
     * @param forgotPasswordDTO 忘记密码信息
     */
    void forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param userInfoDTO 用户信息
     */
    void updateUserInfo(Long userId, UserInfoDTO userInfoDTO);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改支付密码
     *
     * @param userId 用户ID
     * @param oldPaymentPassword 旧支付密码（如果未设置过支付密码，则使用登录密码）
     * @param newPaymentPassword 新支付密码
     */
    void changePaymentPassword(Long userId, String oldPaymentPassword, String newPaymentPassword);

    /**
     * 重置密码（通过验证码）
     *
     * @param resetPasswordDTO 重置密码信息
     */
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}

