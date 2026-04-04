package com.javalabs.basics;

import java.util.concurrent.*;

/**
 * 专门用于演示 IDEA 多线程调试技巧。
 * 建议：在 count++ 那一行打上 Suspend: Thread 断点。
 */
public class DebugMeDemo {

    private int count = 0;

    /**
     * 故意写错的累加逻辑，用于断点实验。
     * 在生产环境下这应该用 AtomicInteger，但这里我们用 int 来复现冲突。
     */
    public void runRaceCondition() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        System.out.println("=== 调试实验：准备手动复现数据丢失 ===");
        System.out.println("1. 请在下面的 count++ 行设置 Suspend: Thread 断点。");
        System.out.println("2. 使用 Debug 模式运行此 main 方法。");

        for (int i = 0; i < 2; i++) {
            pool.execute(() -> {
                String threadName = Thread.currentThread().getName();
                
                // 💡 断点位置
                int oldValue = count; 
                System.out.println(threadName + " 读取到旧值: " + oldValue);
                
                try { Thread.sleep(100); } catch (InterruptedException e) {}

                // 💡 把断点打在下面这一行！
                count = oldValue + 1; 
                
                System.out.println(threadName + " 提交了新值: " + count);
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("\n最终结果: " + count);
        System.out.println(count == 2 ? "✅ 运气不错，没丢数据。" : "❌ 实验成功！数据发生了覆盖。");
    }

    public static void main(String[] args) throws InterruptedException {
        new DebugMeDemo().runRaceCondition();
    }
}
