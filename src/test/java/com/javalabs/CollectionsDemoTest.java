package com.javalabs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java 核心集合（List, Set, Map）功能验证")
class CollectionsDemoTest {

    @Test
    @DisplayName("验证 ArrayList 的有序性与可重复性")
    void listShouldMaintainOrderAndAllowDuplicates() {
        // 1. Arrange & Act
        List<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Node.js");
        list.add("Java"); // 允许重复

        // 3. Assert
        assertEquals(3, list.size(), "列表中应该包含 3 个元素");
        assertEquals("Java", list.get(0), "第一个元素应为 Java");
        assertEquals("Node.js", list.get(1), "第二个元素应为 Node.js");
        assertEquals("Java", list.get(2), "第三个元素应为 Java (重复允许)");
    }

    @Test
    @DisplayName("验证 HashSet 的去重特性")
    void setShouldEnforceUniqueness() {
        // 1. Arrange
        Set<String> set = new HashSet<>();
        
        // 2. Act
        set.add("A");
        set.add("B");
        boolean wasAdded = set.add("A"); // 尝试再加一个重复项

        // 3. Assert
        assertFalse(wasAdded, "插入重复元素时应返回 false");
        assertEquals(2, set.size(), "去重后集合大小应为 2");
        assertTrue(set.contains("A"), "集合应包含 A");
    }

    @Test
    @DisplayName("验证 HashMap 的键值对存取与重复 Key 的覆盖逻辑")
    void mapShouldStoreAndOverwriteKeys() {
        // 1. Arrange
        Map<String, Integer> scores = new HashMap<>();

        // 2. Act
        scores.put("Alice", 90);
        scores.put("Bob", 85);
        Integer previous = scores.put("Alice", 100); // 重复 Key 会覆盖

        // 3. Assert
        assertEquals(2, scores.size(), "Map 大小应为 2 (Alice 被覆盖)");
        assertEquals(100, scores.get("Alice"), "Alice 的分值应更新为 100");
        assertEquals(90, previous, "put 方法应返回被覆盖的旧值 90");
    }

    @Test
    @DisplayName("体验 Java 9+ 的快捷只读集合创建 (List.of)")
    void shouldCreateImmutableCollections() {
        // 类比 JS: const list = Object.freeze(['A', 'B'])
        List<String> immutableList = List.of("A", "B", "C");
        
        assertEquals(3, immutableList.size());
        assertThrows(UnsupportedOperationException.class, () -> immutableList.add("D"), 
                     "只读列表在添加元素时应抛出异常");
    }
}
