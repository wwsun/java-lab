package com.javalabs.model;

import java.util.List;

/**
 * 独立的 Employee 数据实体类 (Record)
 * 
 * 优势：
 * 1. 所有的类（StreamDemo, CollectionsDemo）都可以平等地直接复用它。
 * 2. 符合 Java 的单一职责原则 (Single Responsibility Principle)。
 */
public record Employee(
    String id, 
    String name, 
    String department, 
    double salary, 
    List<String> skills
) {
    // 可以在 Record 里定义一个简化版的构造器，用于 CollectionsDemo 的简单演示
    public static Employee simple(String name) {
        return new Employee("0", name, "Unknown", 0.0, List.of());
    }
}
