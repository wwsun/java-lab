package com.javalabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalabs.entity.Task;
import com.javalabs.interceptor.JwtInterceptor;
import com.javalabs.service.TaskService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 任务控制器测试类
 */
@WebMvcTest(controllers = TaskController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("TaskController 参数校验验证")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtInterceptor jwtInterceptor;

    @MockBean
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() throws Exception {
        // 强制拦截器放行
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("测试 POST /api/tasks (缺失必填字段) - 应该返回 400")
    void shouldReturn400WhenTaskDataIsInvalid() throws Exception {
        // Arrange: 缺失 userId 和 title
        Task invalidTask = new Task();
        invalidTask.setStatus("PENDING");

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("任务标题不能为空")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("任务所属用户 ID 不能为空")));
    }

    @Test
    @DisplayName("测试 POST /api/tasks (有效数据) - 应该返回 201 Success")
    void shouldCreateTaskWhenDataIsValid() throws Exception {
        // Arrange
        Task validTask = new Task(null, 1L, "New Task", "Description", "PENDING", null, null);
        Task savedTask = new Task(100L, 1L, "New Task", "Description", "PENDING", null, null);
        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(100));
    }
}
