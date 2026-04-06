package com.javalabs.security;

import com.javalabs.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流过滤器 (Week 3 核心)
 * 职责：防止恶意爆破和过度访问
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // 内存存储：IP_Path -> 访问次数 (生产环境请务必替换为 Redis)
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    
    // 清理周期 (1分钟)
    private long lastClearTime = System.currentTimeMillis();
    
    // 限流门槛
    private static final int GENERAL_LIMIT = 20; // 普通接口：20次/分钟
    private static final int AUTH_LIMIT = 5;     // 认证接口 (Login/Register)：5次/分钟

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        String key = ip + ":" + path;

        // 定时清理 (模拟固定窗口)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClearTime > TimeUnit.MINUTES.toMillis(1)) {
            requestCounts.clear();
            lastClearTime = currentTime;
        }

        // 判定限制上限
        int limit = path.contains("/api/auth/") ? AUTH_LIMIT : GENERAL_LIMIT;
        
        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > limit) {
            log.warn("🚨 触发限流！IP: {}, 路径: {}, 当前次数: {}", ip, path, count.get());
            renderErrorResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void renderErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429); // Too Many Requests
        response.setContentType("application/json;charset=UTF-8");
        
        Result<Void> result = Result.error(429, "Too many requests. Please try again later.");
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}
