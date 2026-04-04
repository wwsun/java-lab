package com.javalabs.basics;

import com.javalabs.model.Employee;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 演示 Java Stream API 的综合使用。
 * 
 * 核心公式：数据源 -> 中间操作 (Lazy) -> 终端操作 (Eager)
 */
public class StreamDemo {

    /**
     * 演示如何按部门对员工进行分组。
     * 相当于 JS: employees.reduce((acc, current) => { ... }, {})
     */
    public static Map<String, List<Employee>> groupByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::department));
    }

    /**
     * 演示 flatMap：将所有员工掌握的所有技能去重后整理成一个列表。
     * 相当于 JS: [...new Set(employees.flatMap(e => e.skills))]
     */
    public static List<String> getAllUniqueSkills(List<Employee> employees) {
        return employees.stream()
                .flatMap(e -> e.skills().stream()) // 将嵌套的技能 List 拍平
                .distinct()                        // 去重
                .collect(Collectors.toList());
    }

    /**
     * 演示 Optional 与 Stream 配合：获取部门最高薪水。
     * 展示如何安全地处理可能为空的集合。
     */
    public static Optional<Employee> getHighestPaidEmployeeInDept(List<Employee> employees, String deptName) {
        return employees.stream()
                .filter(e -> e.department().equals(deptName))
                .max(Comparator.comparingDouble(Employee::salary));
    }

    public static void main(String[] args) {
        var employees = List.of(
            new Employee("1", "Alice", "Dev", 8000, List.of("Java", "Spring")),
            new Employee("2", "Bob", "Dev", 9000, List.of("Java", "Docker")),
            new Employee("3", "Charlie", "Design", 7000, List.of("Figma", "UI"))
        );

        System.out.println("All unique skills: " + getAllUniqueSkills(employees));
        System.out.println("Groups by department: " + groupByDepartment(employees).keySet());
    }
}
