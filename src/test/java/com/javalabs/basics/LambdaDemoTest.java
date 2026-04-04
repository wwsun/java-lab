package com.javalabs.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java Lambda 核心特性验证")
class LambdaDemoTest {

    @Test
    @DisplayName("验证 Lambda 捕获 Effectively Final 变量的绕过方法")
    void testBypassFinalLimit() {
        // 1. Arrange
        LambdaDemo demo = new LambdaDemo();

        // 2. Act
        int result = demo.bypassFinalLimit();

        // 3. Assert
        assertEquals(2, result, "外部数组引用可以在 Lambda 内部修改，但要注意线程安全");
    }

    @Test
    @DisplayName("验证 Lambda 内置函数式接口的独立运行")
    void testFunctionalInterfaces() {
        // 1. Predicate 验证 (输入 String, 输出 boolean)
        Predicate<String> isEmpty = String::isEmpty;
        assertTrue(isEmpty.test(""), "空字符串应返回 true");
        assertFalse(isEmpty.test("Hello"), "非空字符串应返回 false");

        // 2. Function 验证 (输入 String, 输出 Integer)
        Function<String, Integer> wordCount = s -> s.split(" ").length;
        assertEquals(3, wordCount.apply("Hello World Java"), "单词计数应为 3");
    }
}
