package com.shoppingmall.common.config;

import com.shoppingmall.common.security.AdminJwtAuthenticationInterceptor;
import com.shoppingmall.common.security.JwtAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    private final AdminJwtAuthenticationInterceptor adminJwtAuthenticationInterceptor;

    @Value("${file.upload.path:${user.home}/uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 采购者端JWT拦截器
        // 注意：商品详情等接口虽然允许游客访问，但也会经过拦截器进行可选认证
        // 拦截器会检查token，如果有token就设置userId，如果没有token就允许通过（作为游客）
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/buyer/**")
                .excludePathPatterns(
                        "/api/buyer/user/login",
                        "/api/buyer/user/register",
                        "/api/buyer/user/forgot-password",
                        "/api/buyer/user/reset-password",
                        "/api/buyer/system/config/public",  // 系统公开配置接口允许游客访问
                        "/api/buyer/payment/alipay/notify", // 支付宝异步回调接口
                        "/api/buyer/payment/alipay/return", // 支付宝同步回调接口
                        "/api/buyer/payment/wechat/notify", // 微信异步回调接口
                        "/api/buyer/payment/wechat/return",  // 微信同步回调接口
                        "/api/buyer/member/deposit/payment/callback"  // 预存款充值回调接口（兼容旧接口）
                        // 商品详情、分类、网站内容、导航等接口不再排除，由拦截器支持可选认证
                        // 拦截器会检查token，如果有token就设置userId，如果没有token就允许通过
                );

        // 管理员端JWT拦截器
        registry.addInterceptor(adminJwtAuthenticationInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns(
                        "/api/admin/user/login",
                        "/api/admin/product/template/**",  // 模板下载不需要登录
                        "/api/admin/erp/**"  // 临时：ERP接口测试用，后续需要加回认证
                );

        // 公共接口不需要拦截
        // /api/common/** 路径不配置拦截器
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}

