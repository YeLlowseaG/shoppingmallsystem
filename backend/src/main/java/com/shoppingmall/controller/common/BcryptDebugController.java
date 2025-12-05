package com.shoppingmall.controller.common;

import com.shoppingmall.common.util.EncryptUtil;
import com.shoppingmall.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * BCrypt调试控制器
 * 用于生成和验证BCrypt密码哈希（仅用于开发调试）
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
@Slf4j
@RestController
@RequestMapping("/api/common/bcrypt-debug")
public class BcryptDebugController {

    /**
     * 生成BCrypt哈希
     */
    @PostMapping("/generate")
    public Result<String> generateHash(@RequestParam String password) {
        try {
            String hash = EncryptUtil.bcryptEncode(password);
            log.info("生成BCrypt哈希 - 密码: {}, 哈希: {}", password, hash);
            return Result.success(hash);
        } catch (Exception e) {
            log.error("生成BCrypt哈希失败", e);
            return Result.error(500, "生成失败: " + e.getMessage());
        }
    }

    /**
     * 验证BCrypt哈希
     */
    @PostMapping("/verify")
    public Result<Boolean> verifyHash(
            @RequestParam String password,
            @RequestParam String hash) {
        try {
            boolean matches = EncryptUtil.bcryptMatches(password, hash);
            log.info("验证BCrypt哈希 - 密码: {}, 哈希: {}, 结果: {}", password, hash, matches);
            return Result.success(matches);
        } catch (Exception e) {
            log.error("验证BCrypt哈希失败", e);
            return Result.error(500, "验证失败: " + e.getMessage());
        }
    }

    /**
     * 生成并验证（用于测试）
     */
    @PostMapping("/test")
    public Result<String> test(@RequestParam String password) {
        try {
            String hash = EncryptUtil.bcryptEncode(password);
            boolean matches = EncryptUtil.bcryptMatches(password, hash);
            
            String result = String.format(
                "密码: %s\n生成的哈希: %s\n验证结果: %s",
                password, hash, matches
            );
            
            log.info("BCrypt测试 - {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("BCrypt测试失败", e);
            return Result.error(500, "测试失败: " + e.getMessage());
        }
    }
}

