package com.javalabs.controller;

import com.javalabs.dto.request.LoginRequest;
import com.javalabs.dto.Result;
import com.javalabs.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     * 
     * @param loginRequest 登录信息
     * @return 包含 Token 的统一返回对象 (NodeJS 类比: res.json)
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> data = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return Result.success(data);
    }
}
