package com.javalabs.service;

import java.util.Map;

/**
 * 认证与授权业务接口
 * 负责用户登录、Token 签发及验证
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 原始密码
     * @return 登录结果映射，包含 token 和用户信息
     */
    Map<String, Object> login(String username, String password);
}
