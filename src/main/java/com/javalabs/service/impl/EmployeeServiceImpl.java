package com.javalabs.service.impl;

import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.model.Employee;
import com.javalabs.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 员工服务实现类
 * 演示 Spring Bean 的生命周期
 */
@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    // 使用内存 Map 模拟数据库
    private final Map<String, Employee> repository = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("🌟 [IoC 验证] EmployeeServiceImpl 实例已由容器创建！");
        // 修正：增加符合 Record 定义的 skills 列表参数
        repository.put("1", new Employee("1", "Sun", "Dev", 50000.0, List.of("Java", "Spring")));
        log.info("🌟 [IoC 验证] @PostConstruct 钩子触发：预置测试数据加载完毕。");
    }

    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        return Optional.ofNullable(repository.get(id));
    }

    @Override
    public Employee createEmployee(Employee employee) {
        repository.put(employee.id(), employee);
        return employee;
    }

    @Override
    public Employee updateEmployee(String id, Employee employee) {
        if (repository.containsKey(id)) {
            repository.put(id, employee);
            return employee;
        }
        throw new ResourceNotFoundException("找不到 ID 为 " + id + " 的员工");
    }

    @Override
    public void deleteEmployee(String id) {
        repository.remove(id);
    }
}
