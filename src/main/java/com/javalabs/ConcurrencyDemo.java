package com.javalabs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示 Java 并发入门：从线程启动到脏写起底。
 * 类比模型：Java 多线程 = 餐厅里的多名厨师共用一个案板。
 */
public class ConcurrencyDemo {

    // 1. 案板上的小白菜：普通计数器 (存在读-改-写冲突)
    private int unsafeCounter = 0;

    // 2. 智能案板：原子计数器 (自带检测机制，防止脏写)
    private final AtomicInteger safeCounter = new AtomicInteger(0);

    // 3. 带门禁的案板：同步锁计数器 (排队进入)
    private int synchronizedCounter = 0;

    /**
     * 这里通过 synchronized 关键字给方法加了“门禁”。
     * 同一时刻只能有一个线程进入。
     */
    public synchronized void incrementSynchronized() {
        synchronizedCounter++;
    }

    public int getSynchronizedCounter() {
        return synchronizedCounter;
    }

    /**
     * [第一部分] 演示：最原始的线程启动
     */
    public void basicThread() {
        // 创建厨师 t1，并分配任务逻辑
        Thread t1 = new Thread(() -> {
            System.out.println("Hello from Thread: " + Thread.currentThread().getName());
        });
        t1.start(); // 呼叫厨师开始干活
    }

    /**
     * [第二部分] 演示：工业级厨师团队 (线程池)
     * 在酒楼里，我们不会每来一个菜就招个厨师，而是用固定编制的厨师团队。
     */
    public void poolDemo() {
        // 招募 3 个固定编制的厨师 (FixedThreadPool)
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            // 塞进来 5 个炒菜任务
            for (int i = 0; i < 5; i++) {
                int finalI = i;

                // 提交任务到处理队列。由于只有 3 个厨师，剩下的 2 个菜得排队等位。
                pool.submit(() -> {
                    System.out.println("Processing Task " + finalI + " by " + Thread.currentThread().getName());
                });
            }
        } finally {
            // 餐厅打烊：必须显式调用 shutdown，否则进程永远不会结束
            pool.shutdown();
        }
    }

    /**
     * [第三部分] 演示：复现脏写现象 (Race Condition)
     * 5 个厨师同时改这一个数，为什么最后结果不对？
     */
    public int runRaceCondition(int threadCount, int iterations) throws InterruptedException {
        unsafeCounter = 0;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    // ❌ 翻车现场：这里的 ++ 被拆成了 读、改、写 三步。
                    // 两个厨师可能同时读到 10，然后都改成了 11 并刷回案板。
                    unsafeCounter++; 
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS); // 等待所有大厨干完手里的活
        return unsafeCounter;
    }

    /**
     * [第四部分] 演示：原子类方案 (AtomicInteger)
     * 利用“先问一眼”的策略：写回前先问“数字被别人动过吗？”，动过就重新算。
     */
    public int runAtomicCounter(int threadCount, int iterations) throws InterruptedException {
        safeCounter.set(0);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    // ✅ 智能原子递增
                    safeCounter.incrementAndGet();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return safeCounter.get();
    }

    /**
     * [第五部分] 演示：锁方案 (Synchronized)
     * 老老实实排队。
     */
    public int runSynchronizedCounter(int threadCount, int iterations) throws InterruptedException {
        synchronizedCounter = 0;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    incrementSynchronized(); 
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return synchronizedCounter;
    }

    public static void main(String[] args) throws InterruptedException {
        var demo = new ConcurrencyDemo();
        
        System.out.println("=== 1. 基础线程演示 ===");
        demo.basicThread();
        Thread.sleep(100); 

        System.out.println("\n=== 2. 线程池队列演示 ===");
        demo.poolDemo();
        Thread.sleep(100);

        int threads = 5;
        int iterations = 10000;
        System.out.printf("\n=== 3. 性能与安全全量盘点 (并发线程:%d, 每线程迭代:%d) ===\n", threads, iterations);

        // 演示：数据丢失（1+1 < 2 的现场）
        int unsafe = demo.runRaceCondition(threads, iterations);
        System.out.println("❌ 脏写冲突 (unsafe++): " + unsafe + " (期望值: " + (threads * iterations) + ")");

        // 演示：原子操作成功
        int atomic = demo.runAtomicCounter(threads, iterations);
        System.out.println("✅ 智能原子类 (Atomic): " + atomic);

        // 演示：同步锁成功
        int sync = demo.runSynchronizedCounter(threads, iterations);
        System.out.println("✅ 独占锁模式 (Sync): " + sync);
    }
}
