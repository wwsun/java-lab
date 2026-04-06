package com.javalabs.service.impl;

import com.javalabs.entity.User;
import com.javalabs.service.AuthService;
import com.javalabs.service.UserService;
import com.javalabs.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证服务实现类 (Week 3 核心)
 * 整合 PasswordEncoder 和 JwtUtils
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. 根据用户名查找用户 (NodeJS 类比: User.findOne({ username }))
        Optional<User> userOpt = userService.getUserByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名或密码错误 (用户未找到)");
        }
        
        User user = userOpt.get();

        // 2. 校验密码 (NodeJS 概念: bcrypt.compare)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("🚨 登录失败：用户 {} 密码尝试不正确", username);
            throw new RuntimeException("用户名或密码错误 (密码不匹配)");
        }

        // 3. 校验通过，签发 JWT
        String token = jwtUtils.createToken(user.getUsername(), user.getRole());
        log.info("🔑 用户 {} 登录成功，Token 已签发", username);

        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        
        return result;
    }
}
