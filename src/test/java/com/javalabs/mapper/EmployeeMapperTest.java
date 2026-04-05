package com.javalabs.mapper;

import com.javalabs.model.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 员工映射器 CRUD 实战测试
 * 
 * 对应路线图任务：**CRUD 测试**
 * 目标：验证 MyBatis-Plus 内置的基础方法逻辑。
 */
@SpringBootTest
@Transactional // 开启事务，测试完成后自动回滚，保持数据库整洁
class EmployeeMapperTest {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Test
    @DisplayName("测试完整的员工 CRUD 生命周期")
    void testEmployeeCRUD() {
        // --- 1. Arrange & Act: Insert (新增) ---
        Employee employee = new Employee();
        employee.setName("张三");
        employee.setDepartment("研发部");
        employee.setSalary(15000.0);

        int insertRows = employeeMapper.insert(employee);

        // Assert: 验证插入成功且 ID 已自动回填 (MyBatis-Plus 的核心特性)
        Assertions.assertEquals(1, insertRows, "插入行数应当为 1");
        Assertions.assertNotNull(employee.getId(), "插入后 ID 应当自动生成并回填到对象中");
        Long id = employee.getId();

        // --- 2. Act: Select (查询) ---
        Employee foundEmployee = employeeMapper.selectById(id);

        // Assert: 验证查询到的数据与插入的一致
        Assertions.assertNotNull(foundEmployee);
        Assertions.assertEquals("张三", foundEmployee.getName());
        Assertions.assertEquals("研发部", foundEmployee.getDepartment());

        // --- 3. Arrange & Act: Update (更新) ---
        foundEmployee.setSalary(18000.0);
        int updateRows = employeeMapper.updateById(foundEmployee);

        // Assert: 验证更新成功
        Assertions.assertEquals(1, updateRows, "更新行数应当为 1");
        Employee updatedEmployee = employeeMapper.selectById(id);
        Assertions.assertEquals(18000.0, updatedEmployee.getSalary(), "薪资应当已更新为 18000.0");

        // --- 4. Act: Delete (删除) ---
        int deleteRows = employeeMapper.deleteById(id);

        // Assert: 验证删除成功
        Assertions.assertEquals(1, deleteRows);
        Employee deletedEmployee = employeeMapper.selectById(id);
        Assertions.assertNull(deletedEmployee, "已删除的员工查询结果应当为 null");
    }
}
