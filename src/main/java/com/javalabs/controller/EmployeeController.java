package com.javalabs.controller;

import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.model.Employee;
import com.javalabs.model.Result;
import com.javalabs.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工管理 REST 控制器
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
    public Result<List<Employee>> getAll() {
        return Result.success(employeeService.getAllEmployees());
    }

    /**
     * GET /api/employees/{id} - 根据 ID 获取员工
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable String id) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 为 " + id + " 的员工"));
        return Result.success(employee);
    }

    /**
     * POST /api/employees - 新增员工
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Employee> create(@RequestBody Employee employee) {
        Employee saved = employeeService.createEmployee(employee);
        return Result.success(saved);
    }

    /**
     * PUT /api/employees/{id} - 更新员工信息
     */
    @PutMapping("/{id}")
    public Result<Employee> update(@PathVariable String id, @RequestBody Employee employee) {
        Employee updated = employeeService.updateEmployee(id, employee);
        return Result.success(updated);
    }

    /**
     * DELETE /api/employees/{id} - 离职注销
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<Void> delete(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }
}
