package com.javalabs.service;

import com.javalabs.model.Employee;
import java.util.List;
import java.util.Optional;

/**
 * 员工管理业务接口
 */
public interface EmployeeService {
    List<Employee> getAllEmployees();
    
    Optional<Employee> getEmployeeById(String id);
    
    Employee createEmployee(Employee employee);
    
    Employee updateEmployee(String id, Employee employee);
    
    void deleteEmployee(String id);
}
