package com.javalabs.controller;

import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.model.Employee;
import com.javalabs.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工管理 REST 控制器
 * 使用 Lombok @RequiredArgsConstructor 自动生成包含 EmployeeService 的构造函数 (推荐的 DI 方式)
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * GET /api/employees - 获取所有员工
     */
    @GetMapping
    public List<Employee> getAll() {
        return employeeService.getAllEmployees();
    }

    /**
     * GET /api/employees/{id} - 根据 ID 获取员工
     */
    @GetMapping("/{id}")
    public Employee getById(@PathVariable String id) {
        return employeeService.getEmployeeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 为 " + id + " 的员工"));
    }

    /**
     * POST /api/employees - 新增员工 (返回 201 Created)
     */
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        Employee saved = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/employees/{id} - 更新员工信息
     */
    @PutMapping("/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        return employeeService.updateEmployee(id, employee);
    }

    /**
     * DELETE /api/employees/{id} - 离职注销 (返回 204 No Content)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        employeeService.deleteEmployee(id);
    }
}
