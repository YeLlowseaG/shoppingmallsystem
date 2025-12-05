package com.shoppingmall.controller.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.shoppingmall.common.util.CaptchaUtil;
import com.shoppingmall.common.util.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
@RestController
@RequestMapping("/api/common/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    @Qualifier("captchaCache")
    private final Cache<String, String> captchaCache;

    /**
     * 生成验证码
     * 
     * @return 验证码图片（Base64）和验证码ID
     */
    @GetMapping("/generate")
    public Result<Map<String, String>> generateCaptcha() {
        try {
            // 生成验证码
            CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
            
            // 生成验证码ID（用于后续验证）
            String captchaId = UUID.randomUUID().toString().replace("-", "");
            
            // 将验证码存储到缓存中（key: captchaId, value: code）
            captchaCache.put(captchaId, captchaResult.getCode());
            
            Map<String, String> result = new HashMap<>();
            result.put("captchaId", captchaId);
            result.put("captchaImage", captchaResult.getImageBase64());
            
            return Result.success("生成成功", result);
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return Result.error(500, "生成验证码失败");
        }
    }

    /**
     * 验证验证码
     * 
     * @param captchaId 验证码ID
     * @param code 验证码值
     * @return 验证结果
     */
    public boolean verifyCaptcha(String captchaId, String code) {
        if (captchaId == null || code == null) {
            return false;
        }
        
        // 从缓存中获取验证码
        String cachedCode = captchaCache.getIfPresent(captchaId);
        if (cachedCode == null) {
            return false;
        }
        
        // 验证码验证（不区分大小写）
        boolean isValid = cachedCode.equalsIgnoreCase(code);
        
        // 验证后删除验证码（一次性使用）
        if (isValid) {
            captchaCache.invalidate(captchaId);
        }
        
        return isValid;
    }
}

