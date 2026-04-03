package com.javalabs;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示 ThreadPoolExecutor 的扩容逻辑与拒绝策略。
 * 核心：理解核心线程、队列与最大线程的优先顺序。
 */
public class ThreadPoolTuningDemo {

    /**
     * 演示：全流程饱和攻击
     * 参数设置：核心 2, 队列 3, 最大 4 (总承载 7 个任务，提交第 8 个时触发拒绝)
     */
    public void runSaturationTest() throws InterruptedException {
        // 自定义线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2,                      // 核心线程 (常驻)
                4,                      // 最大线程 (临时工+常驻)
                10, TimeUnit.SECONDS,   // 非核心存活时间
                new ArrayBlockingQueue<>(3), // 💡 阻塞队列 (等待位)
                new CustomThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy() // 💡 默认拒绝策略：报错
        );

        try {
            // 提交 8 个耗时任务，观察扩容过程
            for (int i = 1; i <= 8; i++) {
                int taskId = i;
                try {
                    pool.execute(() -> {
                        System.out.println("Task-" + taskId + " started by " + Thread.currentThread().getName());
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {} 
                        System.out.println("Task-" + taskId + " done.");
                    });
                    
                    // 打印当前池子的快照状态
                    System.out.printf("   [Submitted Task-%d] Pool Size: %d, Queue Size: %d\n", 
                        taskId, pool.getPoolSize(), pool.getQueue().size());
                        
                } catch (RejectedExecutionException e) {
                    System.err.println("   [Rejected Task-" + taskId + "] 池已饱和，队列已满！触发拒绝策略。");
                }
            }
        } finally {
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * 演示：CallerRunsPolicy 策略
     * 特性：主线程自己跑，产生天然降流限速效果。
     */
    public void runCallerRunsDemo() throws InterruptedException {
         ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.CallerRunsPolicy() // 💡 主线程执行
        );

        System.out.println("\n=== 演示 CallerRunsPolicy (拒绝方案：主线程亲劳) ===");
        try {
            for (int i = 1; i <= 3; i++) {
                int taskId = i;
                pool.execute(() -> {
                    System.out.println("Task-" + taskId + " running in " + Thread.currentThread().getName());
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                });
            }
        } finally {
            pool.shutdown();
        }
    }

    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "TuningWorker-" + count.getAndIncrement());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        var demo = new ThreadPoolTuningDemo();
        System.out.println("=== 1. 饱和测试 (2 Core, 3 Queue, 4 Max) ===");
        demo.runSaturationTest();
        
        Thread.sleep(100);
        demo.runCallerRunsDemo();
    }
}
