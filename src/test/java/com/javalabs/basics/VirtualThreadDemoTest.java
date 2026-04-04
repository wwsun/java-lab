package com.javalabs.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java 21 虚拟线程 (Virtual Threads) 性能测试验证")
class VirtualThreadDemoTest {

    @Test
    @DisplayName("验证虚拟线程在 10,000 个高并发任务下的极速处理能力")
    void testVirtualThreadSpeed() throws InterruptedException {
        // 1. Arrange
        VirtualThreadDemo demo = new VirtualThreadDemo();
        
        // 2. Act
        long elapsedTime = demo.runVirtualThreads();

        // 3. Assert
        // 期待值：由于万核齐发，总执行时间应当接近单次阻塞时间 (100ms) + 额外调度开销
        // 传统的线程池处理 10000 个任务需耗时约 10 秒 (10000 / 100 * 100ms)
        assertTrue(elapsedTime < 2000, "虚拟线程执行速度必须远快于传统线程池的 10 秒限制！当前耗时: " + elapsedTime + "ms");
    }

    @Test
    @DisplayName("验证虚拟线程执行器能正确并行处理任务")
    void testVirtualThreadParallelism() throws InterruptedException {
        AtomicInteger completedTasks = new AtomicInteger(0);
        int totalTasks = 1000;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < totalTasks; i++) {
                executor.submit(() -> {
                    completedTasks.incrementAndGet();
                });
            }
        } // 自动等待所有任务完成

        assertEquals(totalTasks, completedTasks.get(), "必须确保所有 1,000 个虚拟线程任务都已成功执行并提交数据更改。");
    }
}
