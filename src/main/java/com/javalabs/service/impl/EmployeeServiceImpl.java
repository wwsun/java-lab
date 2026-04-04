package com.javalabs.service.impl;

import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.model.Employee;
import com.javalabs.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 员工管理服务实现类
 * 使用 ConcurrentHashMap 模拟数据库存储
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    // 内存数据库 (模拟)
    private final Map<String, Employee> employeeStorage = new ConcurrentHashMap<>();

    public EmployeeServiceImpl() {
        // 初始化一些 Mock 数据
        createEmployee(new Employee(null, "张三", "开发部", 15000, List.of("Java", "Spring")));
        createEmployee(new Employee(null, "李四", "测试部", 12000, List.of("JUnit", "Selenium")));
    }

    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeStorage.values());
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        return Optional.ofNullable(employeeStorage.get(id));
    }

    @Override
    public Employee createEmployee(Employee employee) {
        // 模拟 ID 生成 (UUID)
        String id = (employee.id() == null || employee.id().isBlank()) 
                    ? UUID.randomUUID().toString().substring(0, 8) 
                    : employee.id();
        
        Employee newEmployee = new Employee(
            id,
            employee.name(),
            employee.department(),
            employee.salary(),
            employee.skills()
        );
        
        employeeStorage.put(id, newEmployee);
        return newEmployee;
    }

    @Override
    public Employee updateEmployee(String id, Employee employee) {
        if (!employeeStorage.containsKey(id)) {
            throw new ResourceNotFoundException("员工 ID 为 " + id + " 的记录不存在，无法更新");
        }
        
        Employee updatedEmployee = new Employee(
            id, // 保持 ID 不变
            employee.name(),
            employee.department(),
            employee.salary(),
            employee.skills()
        );
        
        employeeStorage.put(id, updatedEmployee);
        return updatedEmployee;
    }

    @Override
    public void deleteEmployee(String id) {
        if (!employeeStorage.containsKey(id)) {
            throw new ResourceNotFoundException("员工 ID 为 " + id + " 的记录不存在，无法删除");
        }
        employeeStorage.remove(id);
    }
}
