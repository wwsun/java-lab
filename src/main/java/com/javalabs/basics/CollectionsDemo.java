package com.javalabs.basics;

import com.javalabs.entity.Employee;
import java.util.*;

/**
 * 演示三种最常用的 Java 集合：List, Set, Map。
 * 
 * 对应心智模型：
 * - List -> JavaScript Array (有序)
 * - Set  -> JavaScript Set   (不重复)
 * - Map  -> JavaScript Map / Object (键值对)
 */
public class CollectionsDemo {

    public static void showList() {
        // 1. 创建并添加 (类比 JS: const list = ['Java', 'Node'])
        // 现在我们不仅存 String，还可以直接存 Employee 对象了！
        List<Employee> list = new ArrayList<>();
        list.add(new Employee("1", "Java", "Core", 0, List.of()));
        list.add(new Employee("2", "Node.js", "Web", 0, List.of()));
        list.add(new Employee("1", "Java", "Core", 0, List.of())); // Record 默认重写了 equals，所以这里能被正确识别

        // 2. 获取
        Employee first = list.get(0);
        System.out.println("First employee name: " + first.name());

        // 3. 遍历 (类比 JS: list.forEach)
        // IDEA 技巧：输入 list.for 然后按 Tab
        for (Employee e : list) {
            System.out.println("Employee: " + e.name() + " (" + e.department() + ")");
        }
    }

    public static void showSet() {
        // 1. 创建并添加 (类比 JS: const set = new Set())
        Set<String> set = new HashSet<>();
        set.add("A");
        set.add("B");
        set.add("A"); // 自动去重

        // 2. Set 的特点是无序且唯一
        System.out.println("Set items (unordered): " + set);
        System.out.println("Set size: " + set.size()); // 应该是 2
    }

    public static void showMap() {
        // 1. 创建并添加 (类比 JS: const map = new Map() 或 obj = {})
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", "1001");
        userMap.put("name", "Alice");
        userMap.put("role", "Developer");

        // 2. 获取 (类比 JS: map.get('name') 或 obj.name)
        String name = userMap.get("name");
        System.out.println("User name from map: " + name);

        // 3. 遍历 Map 的一种常用方式 (类比 JS: Object.entries)
        for (Map.Entry<String, String> entry : userMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }

    public static void main(String[] args) {
        System.out.println("--- List Demo ---");
        showList();
        
        System.out.println("\n--- Set Demo ---");
        showSet();
        
        System.out.println("\n--- Map Demo ---");
        showMap();
    }
}
