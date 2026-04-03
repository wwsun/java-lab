package com.javalabs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("线程安全集合 (ConcurrentHashMap) 压力测试验证")
class ConcurrentCollectionsDemoTest {

    @Test
    @DisplayName("验证 ConcurrentHashMap 在 50 线程大压力下依然能保持数据完整性")
    void testSafeMapConsistency() throws InterruptedException {
        // 1. Arrange
        ConcurrentCollectionsDemo demo = new ConcurrentCollectionsDemo();
        int threads = 50;
        int countPerThread = 1000;
        int expectedSize = threads * countPerThread;

        // 2. Act
        int actualSize = demo.runSafeMap(threads, countPerThread);

        // 3. Assert
        System.out.println("ConcurrentHashMap Results: " + actualSize + " / " + expectedSize);
        assertEquals(expectedSize, actualSize, "ConcurrentHashMap 必须保证 50,000 条数据 1:1 落地成功！");
    }

    @Test
    @DisplayName("验证普通 HashMap 在并发写入下确实会发生大幅数据丢失 (反面案例)")
    void testUnsafeMapCorruption() throws InterruptedException {
        // 1. Arrange
        ConcurrentCollectionsDemo demo = new ConcurrentCollectionsDemo();
        int threads = 20; 
        int countPerThread = 500;
        int expectedSize = threads * countPerThread;

        // 2. Act
        int actualSize = demo.runUnsafeMap(threads, countPerThread);

        // 3. Assert
        System.out.println("Unsafe HashMap Results: " + actualSize + " / " + expectedSize);
        // 通常在高并发写入下，HashMap 的 size 会小于或不准
        assertTrue(actualSize < expectedSize, "普通 HashMap 在并发写入下必须发生脏数据丢失！");
    }
}
