package com.javalabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalabs.entity.Employee;
import com.javalabs.interceptor.JwtInterceptor;
import com.javalabs.service.EmployeeService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 员工管理 REST 接口测试类
 * 使用 @WebMvcTest 仅启动 Web 层上下文，不启动整个 Spring Boot 环境 (快且聚焦)
 */
@WebMvcTest(controllers = EmployeeController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("EmployeeController RESTful CRUD 综合验证")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

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
    @DisplayName("测试 GET /api/employees - 应该返回员工列表")
    void shouldReturnEmployeeList() throws Exception {
        // Arrange
        List<Employee> mockList = List.of(new Employee("1", "Alice", "Dev", 8000, List.of()));
        when(employeeService.getAllEmployees()).thenReturn(mockList);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Alice"));
    }

    @Test
    @DisplayName("测试 POST /api/employees - 应该成功创建并返回 201 Created")
    void shouldCreateEmployee() throws Exception {
        // Arrange
        Employee newEmp = new Employee(null, "John", "Sales", 6000, List.of());
        Employee savedEmp = new Employee("123", "John", "Sales", 6000, List.of());
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(savedEmp);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("123"))
                .andExpect(jsonPath("$.data.name").value("John"));
    }

    @Test
    @DisplayName("测试 PUT /api/employees/{id} - 应该成功更新并返回信息")
    void shouldUpdateEmployee() throws Exception {
        // Arrange
        String id = "1";
        Employee updateReq = new Employee(null, "Alice Updated", "Manager", 9000, List.of());
        Employee updatedEmp = new Employee(id, "Alice Updated", "Manager", 9000, List.of());
        when(employeeService.updateEmployee(eq(id), any(Employee.class))).thenReturn(updatedEmp);

        // Act & Assert
        mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Alice Updated"));
    }

    @Test
    @DisplayName("测试 DELETE /api/employees/{id} - 应该成功删除并返回 204 No Content")
    void shouldDeleteEmployee() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.code").value(200));

        verify(employeeService, times(1)).deleteEmployee("1");
    }

    @Test
    @DisplayName("测试 GET /api/employees/{id} (NotExist) - 应该触发全局异常处理器并返回 404")
    void shouldReturn404WhenNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/employees/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
