package com.javalabs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 演示线程安全集合 (ConcurrentHashMap) 的实战效果。
 * 对比普通 HashMap 在高并发下的“溃败”。
 */
public class ConcurrentCollectionsDemo {

    /**
     * 演示：普通 HashMap 在多线程写时的崩溃
     * 注意：由于 HashMap 没有任何锁保护，多线程同时写会导致 size 不准，甚至内存报错。
     */
    public int runUnsafeMap(int threads, int countPerThread) throws InterruptedException {
        Map<String, Integer> unsafeMap = new HashMap<>(); // ❌ 危险的普通 Map
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < countPerThread; j++) {
                    // key 是线程 ID + 循环索引，理论上不会有冲突
                    unsafeMap.put(threadId + "-" + j, j);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        return unsafeMap.size();
    }

    /**
     * 演示：ConcurrentHashMap 的稳健性
     */
    public int runSafeMap(int threads, int countPerThread) throws InterruptedException {
        // ✅ 工业级高并发 Map
        Map<String, Integer> safeMap = new ConcurrentHashMap<>(); 
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < countPerThread; j++) {
                    safeMap.put(threadId + "-" + j, j);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        return safeMap.size();
    }

    /**
     * 演示：原子更新操作 (computeIfAbsent)
     * 类比 Node.js 的 LRU Cache 逻辑，防止“缓存穿透”。
     */
    public void atomicUpdateDemo() {
        Map<String, String> cache = new ConcurrentHashMap<>();
        
        // 如果 key 不存在，则执行计算逻辑并存入
        String value = cache.computeIfAbsent("config", key -> {
            System.out.println("--- 模拟从数据库加载配置 ---");
            return "Server-Port-8080";
        });
        
        System.out.println("First compute result: " + value);
        
        // 第二次获取，直接命中缓存，不会再打印加载日志
        String cachedValue = cache.computeIfAbsent("config", key -> "New-Port");
        System.out.println("Second compute (hit cache): " + cachedValue);
    }

    public static void main(String[] args) throws Exception {
        var demo = new ConcurrentCollectionsDemo();
        int threads = 50;
        int count = 1000;
        int expected = threads * count;

        System.out.println("=== 1. 高并发写入测试 (预期总量: " + expected + ") ===");
        
        int unsafeSize = demo.runUnsafeMap(threads, count);
        System.out.println("❌ Unsafe HashMap Size: " + unsafeSize + " (数据丢失率: " + ((expected - unsafeSize) * 100 / expected) + "%)");

        int safeSize = demo.runSafeMap(threads, count);
        System.out.println("✅ Safe ConcurrentHashMap Size: " + safeSize + " (完全精准)");

        System.out.println("\n=== 2. 原子更新操作演示 ===");
        demo.atomicUpdateDemo();
    }
}
