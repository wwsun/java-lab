package com.javalabs.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("线程池调优 (ThreadPoolExecutor) 状态流转验证")
class ThreadPoolTuningDemoTest {

    @Test
    @DisplayName("测试任务饱和时触发 AbortPolicy 抛出拒绝异常")
    void testSaturationRejection() {
        // 1. Arrange: 核心 1, 队列 1, 最大 1 (相当于任务处理能力为 2)
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.AbortPolicy()
        );

        // 2. Act: 提交 3 个耗时任务，第 3 个由于核心(1)和队列(1)都满了且最大线程(1)也满了，必然报错
        assertThrows(RejectedExecutionException.class, () -> {
            for (int i = 0; i < 3; i++) {
                pool.execute(() -> {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                });
            }
        });

        // 3. Clean up
        pool.shutdown();
    }

    @Test
    @DisplayName("测试核心线程池的懒加载特性：即使设置了核心线程，任务也会优先填充核心线程")
    void testCoreThreadCreation() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2, 4, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10)
        );

        // 提交 1 个任务
        pool.execute(() -> {});

        // 验证：即使这时没有任何阻塞，也会创建一个核心线程来运行
        assertEquals(1, pool.getPoolSize(), "提交第 1 个任务时应创建第 1 个核心线程");

        pool.shutdown();
    }
}
