package com.javalabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalabs.entity.User;
import com.javalabs.interceptor.JwtInterceptor;
import com.javalabs.service.UserService;
import com.javalabs.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户控制器测试类
 * 重点验证 Bean Validation 校验逻辑
 */
@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("UserController 参数校验与 CRUD 验证")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtInterceptor jwtInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // 强制拦截器放行，以便测试 Controller 逻辑
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("测试 POST /api/users (有效数据) - 应该返回 201 Success")
    void shouldCreateUserWhenDataIsValid() throws Exception {
        // Arrange (准备阶段)
        User validUser = new User(null, "javaman", "java@test.com", "password123", null, null, null, null);
        User savedUser = new User(1L, "javaman", "java@test.com", "password123", null, null, null, null);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        // Act (执行阶段) & Assert (断言阶段)
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("javaman"));
    }

    @Test
    @DisplayName("测试 POST /api/users (无效邮箱) - 应该返回 400 Bad Request")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // Arrange
        User invalidUser = new User(null, "javaman", "invalid-email", "password123", null, null, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("校验失败: 邮箱格式不正确"));
    }

    @Test
    @DisplayName("测试 POST /api/users (短密码) - 应该返回 400 Bad Request")
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        // Arrange
        User invalidUser = new User(null, "javaman", "java@test.com", "123", null, null, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("校验失败: 密码长度至少为 6 位"));
    }
}
