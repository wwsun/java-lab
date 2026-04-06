package com.javalabs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 基础配置
 * Week 3: 开启基础安全配置，并提供密码加密器
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置过滤链：目前先放行所有请求，以便我们手动演示 JWT 逻辑
     * 随着 Week 3 深入，我们将在这里配置更加严格的权限控制
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 禁用 CSRF (JWT 无需)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 目前放行所有，稍后集成系统后改为精细化控制
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // 允许 H2 控制台
            
        return http.build();
    }

    /**
     * 注入 BCrypt 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
