package com.javalabs.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * 请求日志过滤器 (Servlet Filter)
 * 位于所有中间件的最外层，负责记录原始请求信息和总耗时
 */
@Slf4j
@Component
@Order(1) // 确保它第一个执行
public class RequestLogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        // 将 request 强制转换为 HttpServletRequest，以获取 HTTP 特有的方法
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        long startTime = System.currentTimeMillis();
        
        log.info(">>> [Filter 开始] HTTP {} {}", method, uri);
        
        try {
            // 继续执行后续的 Filter 或 Servlet (Spring MVC)
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("<<< [Filter 结束] HTTP {} {} | 耗时: {}ms", method, uri, duration);
        }
    }
}
