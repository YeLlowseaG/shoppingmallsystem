package com.shoppingmall.common.config;

import com.shoppingmall.common.security.JwtAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/buyer/user/login",
                        "/api/buyer/user/register",
                        "/api/buyer/user/forgot-password",
                        "/api/buyer/product/**",
                        "/api/buyer/product-category/**",
                        "/api/common/**"
                );
    }
}

