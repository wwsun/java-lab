package com.javalabs.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 性能监控拦截器 (Spring MVC Interceptor)
 * 位于 Spring 框架内部，负责记录 Controller 层的执行详情
 */
@Slf4j
@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            
            startTimeThreadLocal.set(System.currentTimeMillis());
            log.info("    >>> [Interceptor 预处理] 执行者: {}#{}", controllerName, methodName);
        }
        return true; // 继续执行后续流程
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
                           ModelAndView modelAndView) {
        log.info("    <<< [Interceptor 后处理] 逻辑执行完毕，准备渲染视图（如果有）");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, 
                                Exception ex) {
        long startTime = startTimeThreadLocal.get();
        long duration = System.currentTimeMillis() - startTime;
        startTimeThreadLocal.remove();
        
        log.info("    <<< [Interceptor 完成] API 核心逻辑耗时: {}ms", duration);
        if (ex != null) {
            log.error("    !!! [Interceptor 异常] 检测到异常: {}", ex.getMessage());
        }
    }
}
