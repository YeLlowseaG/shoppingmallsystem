package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.*;
import com.shoppingmall.service.user.UserService;
import com.shoppingmall.vo.LoginVO;
import com.shoppingmall.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器（采购者端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@RestController
@RequestMapping("/api/buyer/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功，账户已激活，可以立即使用");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    public Result<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        userService.forgotPassword(forgotPasswordDTO);
        return Result.success("密码重置信息已发送到您的邮箱，请查收");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserInfoVO userInfo = userService.getUserInfo(userId);
        return Result.success("获取成功", userInfo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UserInfoDTO userInfoDTO) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserInfo(userId, userInfoDTO);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(
            HttpServletRequest request,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }
}

