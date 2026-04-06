package com.javalabs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工实体类
 * 由原本的 Record 迁移为 MyBatis-Plus 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("employees")
public class Employee {

    /**
     * 自增主键 (使用 Long 类型对标 BIGINT)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 部门
     */
    private String department;

    /**
     * 薪资
     */
    private Double salary;

    /**
     * 技能列表 (目前不持久化，仅用于基础语法演示)
     */
    @TableField(exist = false)
    private List<String> skills;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 手动提供一个与 Record 类似的构造器，保持对基础语法演示代码的兼容。
     */
    public Employee(String id, String name, String department, double salary, List<String> skills) {
        this.id = (id != null && !id.equals("0")) ? Long.valueOf(id) : null;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.skills = skills;
    }

    // --- 兼容旧代码：Record 风格的访问器（MyBatis-Plus 不需要，但为了减少现有业务逻辑改动） ---
    public Long id() { return id; }
    public String name() { return name; }
    public String department() { return department; }
    public double salary() { return salary != null ? salary : 0.0; }
    public List<String> skills() { return skills != null ? skills : List.of(); }

    /**
     * 静态辅助方法，类比旧版的 Employee.simple(name)
     */
    public static Employee simple(String name) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setDepartment("Unknown");
        employee.setSalary(0.0);
        employee.setSkills(List.of());
        return employee;
    }
}
