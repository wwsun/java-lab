package com.javalabs.service.impl;

import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.mapper.EmployeeMapper;
import com.javalabs.entity.Employee;
import com.javalabs.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 员工服务实现类 (已迁移至 MyBatis-Plus 持久化方案)
 * 移除内存 Map 存储，改为调用 EmployeeMapper
 */
@Slf4j
@Service
@RequiredArgsConstructor // Lombok 会自动生成包含 final 字段的构造器，实现构造器注入
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    @Override
    public List<Employee> getAllEmployees() {
        // selectList(null) 表示没有任何查询条件，即查询全部
        return employeeMapper.selectList(null);
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        // 注意：底层 ID 变更为 Long，这里做一次转换
        Employee employee = employeeMapper.selectById(Long.valueOf(id));
        return Optional.ofNullable(employee);
    }

    @Override
    public Employee createEmployee(Employee employee) {
        // 调用 MP 的 insert 方法
        employeeMapper.insert(employee);
        log.info("🌟 [DB 实战] 已持久化新员工：{}，自动回填 ID：{}", employee.getName(), employee.getId());
        return employee;
    }

    @Override
    public Employee updateEmployee(String id, Employee employee) {
        // 设置 ID 确保更新正确
        employee.setId(Long.valueOf(id));
        int rows = employeeMapper.updateById(employee);
        if (rows > 0) {
            return employee;
        }
        throw new ResourceNotFoundException("找不到 ID 为 " + id + " 的员工");
    }

    @Override
    public void deleteEmployee(String id) {
        employeeMapper.deleteById(Long.valueOf(id));
        log.info("🌟 [DB 实战] 已从数据库删除员工 ID：{}", id);
    }
}
