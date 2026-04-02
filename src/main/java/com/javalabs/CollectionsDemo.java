package com.javalabs;

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
        List<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Node.js");
        list.add("TypeScript");
        list.add("Java"); // 允许重复

        // 2. 获取与索引 (类比 JS: list[1])
        String first = list.get(0);
        System.out.println("First element: " + first);

        // 3. 遍历 (类比 JS: list.forEach)
        // IDEA 技巧：输入 list.for 然后按 Tab
        for (String s : list) {
            System.out.println("List item: " + s);
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
