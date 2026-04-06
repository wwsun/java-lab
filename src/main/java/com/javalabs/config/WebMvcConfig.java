package com.javalabs.config;

import com.javalabs.interceptor.JwtInterceptor;
import com.javalabs.interceptor.PerformanceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类
 * 用于注册拦截器、跨域配置等
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final PerformanceInterceptor performanceInterceptor;
    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 注册性能监控拦截器
        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/api/**");
                
        // 2. 注册 JWT 安全验证拦截器 (Week 3 核心)
        // 拦截所有 API 下的写操作或敏感操作，排除登录接口本身
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
