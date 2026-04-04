package com.javalabs.basics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.javalabs.model.Employee;
import static com.javalabs.basics.StreamDemo.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java Stream API & Lambda 进阶实操与验证")
class StreamDemoTest {

    private List<Employee> mockEmployees;

    @BeforeEach
    void setUp() {
        // Arrange
        mockEmployees = List.of(
            new Employee("1", "Alice", "Dev", 8000, List.of("Java", "Spring")),
            new Employee("2", "Bob", "Dev", 9500, List.of("Java", "Docker")),
            new Employee("3", "Charlie", "Design", 7000, List.of("Figma", "UI")),
            new Employee("4", "David", "Design", 7500, List.of("Sketch", "UI"))
        );
    }

    @Test
    @DisplayName("验证分组功能：应该根据部门正确对员工进行分组")
    void testGroupingByDepartment() {
        // 1. Act
        Map<String, List<Employee>> byDept = groupByDepartment(mockEmployees);

        // 3. Assert
        assertEquals(2, byDept.size(), "应该有两个部门: Dev, Design");
        assertEquals(2, byDept.get("Dev").size(), "Dev 部门应该有 2 人");
        assertEquals(2, byDept.get("Design").size(), "Design 部门应该有 2 人");
    }

    @Test
    @DisplayName("验证 flatMap 功能：所有员工的技能应该被拍平并正确去重")
    void testFlatMapSkills() {
        // 1. Act
        List<String> skills = getAllUniqueSkills(mockEmployees);

        // 3. Assert
        assertTrue(skills.contains("Java"));
        assertTrue(skills.contains("Figma"));
        assertEquals(6, skills.size(), "去重后的技能总数应该是 6 (Java, Spring, Docker, Figma, UI, Sketch)");
    }

    @Test
    @DisplayName("验证 Optional 与 Stream 组合：正确找出指定部门薪水最高的员工")
    void testOptionalWithStreamMax() {
        // 1. Act
        Optional<Employee> topDev = getHighestPaidEmployeeInDept(mockEmployees, "Dev");
        Optional<Employee> nonExistent = getHighestPaidEmployeeInDept(mockEmployees, "Sales");

        // 3. Assert
        // Node.js/TS 类比：Optional.isPresent() 相当于 (obj !== undefined)
        assertTrue(topDev.isPresent());
        assertEquals("Bob", topDev.get().name(), "Dev 部门最高薪水应该是 Bob (9500)");
        
        // 测试空集处理
        assertFalse(nonExistent.isPresent(), "不存在的部门应返回空的 Optional");
        
        // 优雅处理默认值 (相当于 TS 的 ??)
        Employee defaultEmp = nonExistent.orElse(new Employee("0", "Default", "N/A", 0, List.of()));
        assertEquals("Default", defaultEmp.name());
    }

    @Test
    @DisplayName("综合练习：计算所有 Dev 部门员工的平均薪水并演示 mapToDouble")
    void testAverageSalaryInDev() {
        // 1. Act
        double avgSalary = mockEmployees.stream()
                .filter(e -> e.department().equals("Dev"))
                .mapToDouble(Employee::salary) // 转换为 DoubleStream 避免装箱开销
                .average()                     // 返回 OptionalDouble
                .orElse(0.0);

        // 3. Assert
        assertEquals(8750.0, avgSalary, "Dev 部门平均薪水应为 8750 ( (8000+9500)/2 )");
    }
}
