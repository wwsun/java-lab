package com.javalabs.basics;

import java.util.concurrent.*;

/**
 * 演示 Runnable 与 Callable 的差异。
 * 重点：理解如何像 await Promise 一样获取任务结果 (Future)。
 */
public class CallableDemo {

    /**
     * 演示 Runnable (只管去做)
     */
    public void runSimpleTask() {
        Runnable task = () -> {
            System.out.println("Processing simple Runnable task...");
        };
        new Thread(task).start();
    }

    /**
     * 演示 Callable (需要返回数据)
     * 利用 Future 模式，像 JS 的 Promise.then() 或 await 一样工作。
     */
    public Integer calculateWithResult() throws ExecutionException, InterruptedException {
        // 创建一个单线程的取货机
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // 提交一个 Callable 任务，它会立刻返回一个 Future (取货凭据)
        Future<Integer> future = executor.submit(() -> {
            System.out.println("--- 正在进行复杂的后台计算 (模拟耗时) ---");
            Thread.sleep(500); // 模拟耗时
            return 42; 
        });

        System.out.println("主线程可以继续执行别的事，凭证在手...");

        // 获取结果。如果任务没算完，这里会“阻塞” (类似 Node.js 的 await)
        try {
            Integer result = future.get(); // 获取结果
            System.out.println("从 Future 凭证中拿到的结果: " + result);
            return result;
        } finally {
            executor.shutdown(); // 依然记得要“打烊”
        }
    }

    public static void main(String[] args) throws Exception {
        var demo = new CallableDemo();
        System.out.println("=== 1. Runnable Demo ===");
        demo.runSimpleTask();
        
        System.out.println("\n=== 2. Callable & Future Demo ===");
        demo.calculateWithResult();
    }
}
