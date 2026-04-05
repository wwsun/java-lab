package com.javalabs.config;

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册性能监控拦截器，并拦截所有 /api/** 下的请求
        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/api/**");
    }
}
