package com.javalabs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示 Java 并发基础：线程、线程池与原子性。
 */
public class ConcurrencyDemo {

    // 1. 用于演示线程不安全的计数器
    private int unsafeCounter = 0;

    // 2. 用于演示线程安全的原子计数器
    private final AtomicInteger safeCounter = new AtomicInteger(0);

    // 3. 用于演示 synchronized 同步锁的计数器
    private int synchronizedCounter = 0;

    /**
     * 使用 synchronized 关键字保证同步
     * 类比：给厨房门加了一把锁，一次只能进一个厨师。
     */
    public synchronized void incrementSynchronized() {
        synchronizedCounter++;
    }

    public int getSynchronizedCounter() {
        return synchronizedCounter;
    }

    /**
     * 演示基础线程创建 (Legacy Way)
     * 在生产环境中，我们极少直接 new Thread()
     */
    public void basicThread() {
        // 类比 Node.js 的异步任务，但它是真正的系统级线程
        Thread t1 = new Thread(() -> {
            System.out.println("Hello from Thread: " + Thread.currentThread().getName());
        });
        t1.start();
    }

    /**
     * 演示工业级做法：线程池 (ExecutorService)
     * 好处：重用线程，控制并发上限，避免资源耗尽。
     */
    public void poolDemo() {
        // 创建一个固定为 2 个线程的池 (固定招募 2 个厨师)
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 5; i++) {
                int finalI = i;
                pool.submit(() -> {
                    System.out.println("Processing Task " + finalI + " by " + Thread.currentThread().getName());
                });
            }
        } finally {
            // JDK 17 及以下版本必须手动关闭，否则 JVM 进程可能无法退出
            pool.shutdown();
        }
    }

    /**
     * 演示竞态条件 (Race Condition)：多线程下的数据损坏。
     * 每个线程加 1000 次，5 个线程理论应该是 5000。
     */
    public int runRaceCondition(int threadCount, int iterations) throws InterruptedException {
        unsafeCounter = 0;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    // ❌ 重点！这里的 ++ 不是原子的，分三步：读->加->写
                    // 多线程交替执行会导致部分修改被覆盖。
                    unsafeCounter++; 
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return unsafeCounter;
    }

    /**
     * 演示如何使用原子类解决并发冲突 (Lock-free)
     */
    public int runAtomicCounter(int threadCount, int iterations) throws InterruptedException {
        safeCounter.set(0);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    // ✅ 它是线程安全的，底层使用 CAS (Compare-and-Swap) 硬件指令保证原子性。
                    safeCounter.incrementAndGet();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return safeCounter.get();
    }

    /**
     * 演示如何使用 synchronized 解决并发冲突 (Lock-based)
     */
    public int runSynchronizedCounter(int threadCount, int iterations) throws InterruptedException {
        synchronizedCounter = 0;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < iterations; j++) {
                        incrementSynchronized(); // 每一个操作都在锁的保护下
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return synchronizedCounter;
    }
}
