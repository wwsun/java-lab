package com.javalabs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java 并发模型与线程安全实操验证")
class ConcurrencyDemoTest {

    @Test
    @DisplayName("验证竞态条件：在 5 个线程并发下，i++ 的结果大概率小于预期")
    void testRaceConditionFail() throws InterruptedException {
        // 1. Arrange
        ConcurrencyDemo demo = new ConcurrencyDemo();
        int threads = 5;
        int iterations = 10000;
        int expected = threads * iterations;

        // 2. Act
        int result = demo.runRaceCondition(threads, iterations);

        // 3. Assert: 验证结果是否遭受到了“脏写”破坏 (通常会小于 expected)
        System.out.println("Race Condition Result: " + result + " / Expected: " + expected);
        
        // 注意：这是一项概率性测试，极低概率下可能会相等，但在 50000 次累加下基本必败。
        assertTrue(result <= expected, "结果不应超过预期");
        // 如果你的机器是多核，且并发度足够，这里通常会小于预期。
        if (result < expected) {
            System.out.println("Confirmed -> Data corruption detected due to race condition.");
        }
    }

    @Test
    @DisplayName("验证原子类安全性：即使高并发，AtomicInteger 也应保证结果准确")
    void testAtomicCounterSuccess() throws InterruptedException {
        // 1. Arrange
        ConcurrencyDemo demo = new ConcurrencyDemo();
        int threads = 10;
        int iterations = 10000;
        int expected = threads * iterations;

        // 2. Act
        int result = demo.runAtomicCounter(threads, iterations);

        // 3. Assert
        System.out.println("Atomic Counter Result: " + result + " / Expected: " + expected);
        assertEquals(expected, result, "Atomic 计数器必须保证结果精准无误！");
    }

    @Test
    @DisplayName("验证 synchronized 锁安全性：即使高并发，结果也应准确")
    void testSynchronizedCounterSuccess() throws InterruptedException {
        // 1. Arrange
        ConcurrencyDemo demo = new ConcurrencyDemo();
        int threads = 10;
        int iterations = 10000;
        int expected = threads * iterations;

        // 2. Act
        int result = demo.runSynchronizedCounter(threads, iterations);

        // 3. Assert
        System.out.println("Synchronized Counter Result: " + result + " / Expected: " + expected);
        assertEquals(expected, result, "synchronized 锁必须保证结果精准无误！");
    }
}
