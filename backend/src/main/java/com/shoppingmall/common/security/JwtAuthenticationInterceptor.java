package com.shoppingmall.common.security;

import com.shoppingmall.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 排除登录、注册等公开接口
        String uri = request.getRequestURI();
        if (uri.contains("/login") || uri.contains("/register") || uri.contains("/forgot-password")) {
            return true;
        }

        // 获取Token
        String token = getTokenFromRequest(request);
        
        // 如果商品详情等公开接口，支持可选认证：有token就验证并设置userId，没有token就允许通过
        if (uri.contains("/product/") || uri.contains("/product-category/") || 
            uri.contains("/website/") || uri.contains("/navigation/")) {
            // 可选认证：如果有token就验证并设置userId
            if (token != null) {
                try {
                    if (jwtUtil.validateToken(token)) {
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        request.setAttribute("userId", userId);
                    }
                    // 如果token无效，不抛出异常，允许继续访问（作为游客）
                } catch (Exception e) {
                    // token无效时，不抛出异常，允许继续访问（作为游客）
                }
            }
            return true;
        }

        // 其他接口必须登录
        if (token == null) {
            throw new BusinessException(401, "未登录，请先登录");
        }

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "Token已过期，请重新登录");
        }

        // 将用户ID存储到request中，供后续使用
        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}









































