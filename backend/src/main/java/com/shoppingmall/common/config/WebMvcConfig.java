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
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/buyer/**")
                .excludePathPatterns(
                        "/api/buyer/user/login",
                        "/api/buyer/user/register",
                        "/api/buyer/user/forgot-password",
                        "/api/buyer/user/reset-password",
                        "/api/buyer/product/**",
                        "/api/buyer/product-category/**",
                        "/api/buyer/website/**",  // 网站内容模块允许游客访问
                        "/api/buyer/system/config/public",  // 系统公开配置接口允许游客访问
                        "/api/buyer/navigation/**"  // 导航菜单模块允许游客访问
                );

        // 管理员端JWT拦截器
        registry.addInterceptor(adminJwtAuthenticationInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns(
                        "/api/admin/user/login"
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

