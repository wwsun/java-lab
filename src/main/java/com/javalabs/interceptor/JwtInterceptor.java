package com.javalabs.interceptor;

import com.javalabs.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器 (类比 Node.js 中的 Auth Middleware)
 * 拦截请求并校验 Header 中的 Authorization Bearer Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求 (CORS 预检)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 获取 Authorization Header
        String authHeader = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("🚨 未授权访问：缺失 Token，路径: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 401, \"message\": \"Unauthorized: Missing or invalid Token\"}");
            return false;
        }

        // 2. 截取并解析 Token
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtils.parseToken(token);
            
            // 3. 将解析后的信息存入 request，方便后续 Controller 使用 (类比 req.user = payload)
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", claims.get("role"));
            
            log.debug("✅ Token 验证通过, 用户: {}, 角色: {}", claims.getSubject(), claims.get("role"));
            return true;
            
        } catch (Exception e) {
            log.error("🚨 Token 验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 401, \"message\": \"Unauthorized: Token expired or tampered\"}");
            return false;
        }
    }
}
