package com.shoppingmall.common.util;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 验证码工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
public class CaptchaUtil {

    /**
     * 验证码字符集（数字+字母，排除易混淆字符）
     */
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 4;

    /**
     * 图片宽度
     */
    private static final int IMAGE_WIDTH = 120;

    /**
     * 图片高度
     */
    private static final int IMAGE_HEIGHT = 40;

    /**
     * 生成验证码图片（Base64编码）
     *
     * @return 验证码结果（包含验证码字符串和Base64图片）
     */
    public static CaptchaResult generateCaptcha() {
        try {
            // 创建验证码生成器
            RandomGenerator randomGenerator = new RandomGenerator(CODE_CHARS, CODE_LENGTH);
            
            // 创建线段干扰的验证码
            LineCaptcha lineCaptcha = cn.hutool.captcha.CaptchaUtil.createLineCaptcha(IMAGE_WIDTH, IMAGE_HEIGHT);
            lineCaptcha.setGenerator(randomGenerator);
            
            // 设置字体
            lineCaptcha.setFont(new Font("Arial", Font.BOLD, 28));

            // 生成验证码并获取验证码文本
            lineCaptcha.createCode();
            String code = lineCaptcha.getCode();

            // 转换为Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            lineCaptcha.write(outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            
            return new CaptchaResult(code, base64Image);
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            throw new RuntimeException("生成验证码失败", e);
        }
    }

    /**
     * 验证码结果
     */
    public static class CaptchaResult {
        private String code;
        private String imageBase64;

        public CaptchaResult(String code, String imageBase64) {
            this.code = code;
            this.imageBase64 = imageBase64;
        }

        public String getCode() {
            return code;
        }

        public String getImageBase64() {
            return imageBase64;
        }
    }
}

