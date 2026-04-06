package com.javalabs.security;

import com.javalabs.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器 (Week 3 核心)
 * 职责：拦截所有 HTTP 请求 -> 解析 Token -> 绑定身份至 Spring Security 上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 从 Header 中获取 Authorization: Bearer <Token>
        String authHeader = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            // 没有 Token，直接放行（交给后续 Filter 或 SecurityConfig 的拦截规则处理）
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 截取并解析 Token
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtils.parseToken(token);
            String username = claims.getSubject();
            String role = (String) claims.get("role");

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 3. 构建权限列表 (Spring Security 要求角色通常以 ROLE_ 开头)
                List<SimpleGrantedAuthority> authorities = 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                // 4. 创建认证对象 (由于用户确认暂不接入 UserDetailsService，此处第二个参数传 null 表示凭证已验过)
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                // 5. 存入上下文管理器 (SecurityContextHolder)
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("🛡️ Spring Security: 用户 {} [{}] 认证成功", username, role);
            }
        } catch (Exception e) {
            log.error("🚨 JWT 认证失败: {}", e.getMessage());
            // Token 验证失败时不抛异常，而是让上下文为空，后续由 SecurityConfig 的匿名规则拦截返回 403
        }

        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}
