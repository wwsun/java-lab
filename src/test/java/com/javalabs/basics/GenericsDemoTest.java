package com.javalabs.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("泛型与类型擦除练习与验证")
class GenericsDemoTest {

    @Test
    @DisplayName("验证泛型盒子是否正确存储和读取数据")
    void shouldStoreAndRetrieveGenericsCorrectly() {
        // 1. Arrange: 准备测试数据
        String testData = "Hello Java Labs";
        GenericsDemo.Box<String> stringBox = new GenericsDemo.Box<>(testData);

        // 2. Act: 执行操作
        String content = stringBox.getContent();

        // 3. Assert: 验证结果
        assertEquals(testData, content);
    }

    @Test
    @DisplayName("验证类型擦除后的运行时判定限制")
    void shouldDemonstrateTypeErasureLimitations() {
        // 在 Java 中，如果你写：
        // List<String> list1 = new ArrayList<>();
        // List<Integer> list2 = new ArrayList<>();
        // if (list1.getClass() == list2.getClass()) { ... }
        // 结果会是 true！因为运行时它们都被擦除为 ArrayList。
        
        // 1. Arrange
        List<String> stringList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        // 2. Act & Assert
        // TS 中它们是不同类型，但在 Java 运行时，它们共享同一个 Class 实例
        assertSame(stringList.getClass(), intList.getClass(), "运行时泛型信息已丢失 (Erasure)");
    }

    @Test
    @DisplayName("验证 PECS 规则：生产者 (Producer Extends) 适用于读场景")
    void shouldDemonstratePECSProducerExtends() {
        // 1. Arrange
        List<Integer> intList = List.of(1, 2, 3);
        List<? extends Number> numbers = intList; // 允许，Integer 是 Number 的子类

        // 2. Act
        Number first = numbers.get(0); // 允许从泛型列表读取，因为其上界是 Number

        // 3. Assert
        assertEquals(1, first.intValue());
        
        // 注意：numbers.add(4); // 这行代码无法通过编译
    }

    @Test
    @DisplayName("验证 PECS 规则：消费者 (Consumer Super) 适用于写场景")
    void shouldDemonstratePECSConsumerSuper() {
        // 1. Arrange
        List<Number> numberList = new ArrayList<>();
        List<? super Integer> consumers = numberList; // 允许，Number 是 Integer 的父类

        // 2. Act
        consumers.add(10); // 允许向泛型列表写入，因为其下界是 Integer
        consumers.add(20);

        // 3. Assert
        assertEquals(2, numberList.size());
        assertEquals(10, numberList.get(0));
    }
}
