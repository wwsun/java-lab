package com.javalabs;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 演示 Java 21 虚拟线程 (Virtual Threads) vs 传统平台线程池。
 * 实战目标：模拟 10,000 个高并发 I/O 阻塞请求。
 */
public class VirtualThreadDemo {

    private static final int TASK_COUNT = 10000; // 并发任务总数
    private static final int SLEEP_MS = 100;    // 模拟 I/O 耗时 (100ms)

    /**
     * 对照组：使用传统的固定线程池 (Platform Threads)
     */
    public long runPlatformThreadPool() throws InterruptedException {
        System.out.println("\n--- [对照组] 传统平台线程池 (Fixed: 100) 开始 ---");
        Instant start = Instant.now();

        try (ExecutorService executor = Executors.newFixedThreadPool(100)) {
            IntStream.range(0, TASK_COUNT).forEach(i -> {
                executor.submit(() -> {
                    try {
                        // 模拟 I/O 阻塞
                        Thread.sleep(Duration.ofMillis(SLEEP_MS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
            // ExecutorService 在 try-with-resources 中会自动调用 shutdown() 和 awaitTermination() (Java 19+)
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.printf("平台线程池执行完了 %d 个任务，耗时: %d ms\n", TASK_COUNT, timeElapsed);
        return timeElapsed;
    }

    /**
     * 实验组：使用 Java 21 虚拟线程 (Virtual Threads)
     */
    public long runVirtualThreads() throws InterruptedException {
        System.out.println("\n--- [实验组] Java 21 虚拟线程 (Thread-per-Task) 开始 ---");
        Instant start = Instant.now();

        // 💡 Executors.newVirtualThreadPerTaskExecutor() 是 Java 21 的核心 API
        // 它为每个提交的任务创建一个轻量级的虚拟线程
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, TASK_COUNT).forEach(i -> {
                executor.submit(() -> {
                    try {
                        // 即使这里发生了阻塞，JVM 也会自动挂起虚拟线程，让出资源
                        Thread.sleep(Duration.ofMillis(SLEEP_MS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.printf("虚拟线程执行完了 %d 个任务，耗时: %d ms\n", TASK_COUNT, timeElapsed);
        return timeElapsed;
    }

    public static void main(String[] args) throws InterruptedException {
        var demo = new VirtualThreadDemo();
        
        // 1. 运行平台线程池 (受限于 100 个物理线程)
        demo.runPlatformThreadPool();
        
        // 2. 运行虚拟线程 (不再受限于线程池大小)
        demo.runVirtualThreads();
        
        System.out.println("\n💡 结论：虚拟线程在处理阻塞 I/O 时，真正实现了“百倍吞吐”！");
    }
}
