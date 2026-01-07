package com.shoppingmall.common.security;

import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.entity.AdminUser;
import com.shoppingmall.repository.permission.AdminUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员JWT认证拦截器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AdminUserRepository adminUserRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 排除管理员登录接口
        String uri = request.getRequestURI();
        if (uri.contains("/admin/user/login")) {
            return true;
        }

        // 获取Token
        String token = getTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未登录，请先登录");
        }

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "Token已过期，请重新登录");
        }

        // 从Token中获取管理员ID
        Long adminId;
        try {
            adminId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new BusinessException(401, "Token无效，请重新登录");
        }

        // 验证管理员是否存在且状态正常
        AdminUser adminUser = adminUserRepository.selectById(adminId);
        if (adminUser == null || adminUser.getDeleted() == 1) {
            throw new BusinessException(401, "管理员不存在或已被删除");
        }

        if (adminUser.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 将管理员ID存储到request中，供后续使用
        request.setAttribute("adminId", adminId);
        request.setAttribute("adminUsername", adminUser.getUsername());

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











































































