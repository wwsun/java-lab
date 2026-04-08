# 15 - Runnable 与 Callable：任务定义的两大标准

## 核心心智映射 (Core Mental Mapping)

在 Java 并发编程中，我们通过接口来抽象“任务”。最常用的两个标准是 `Runnable` 和 `Callable`。

| 维度         | Runnable                            | Callable<V>                  | 心智映射         |
| :----------- | :---------------------------------- | :--------------------------- | :--------------- |
| **对标 JS**  | `new Promise((resolve) => { ... })` | `async () => { return 42; }` | 任务是否有反馈   |
| **返回结果** | **无 (void)**                       | **有 (V)**                   | 结果值的类型化   |
| **异常处理** | 内部强制处理，不向上抛出            | **允许向上抛出异常**         | 异常链的传递     |
| **执行入口** | `run()` 方法                        | `call()` 方法                | 不同契约的方法名 |
| **获取方式** | 直接运行，无法得知结束              | 配合 `Future` 获取结果       | 异步与同步的桥梁 |

---

## 概念解释 (Conceptual Explanation)

### 1. Runnable (没有回报的劳动)

用于那些“只管去做，不求结果”的任务。比如刷新一个不重要的缓存、发送一条异步埋点。

### 2. Callable (有反馈的思考)

用于需要执行结果的任务。它支持泛型，可以返回任何类型。

### 3. Future (未来的期票)

当你把 `Callable` 交给线程池后，它不会立刻给你结果，而是给你一张“期票” (`Future`)。你可以拿着这张票，在未来的某时刻提取结果。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心接口签名

```java
@FunctionalInterface
public interface Runnable {
    void run();
}

@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

### 线程池提交

```java
ExecutorService pool = Executors.newFixedThreadPool(1);
// 提交 Runnable
pool.execute(() -> { ... });
// 提交 Callable，并得到 Future
Future<Integer> futureResult = pool.submit(() -> 42);
```

---

## 典型用法 (Typical Usage)

### 获取 Callable 结果

```java
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(2000); // 模拟耗时任务
    return 100;
});

// ... 处理其他逻辑

Integer result = future.get(); // 阻塞！直到任务完成
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `future.get()` 的行为：
它类似于 Node.js 中的 `await`。调用 `get()` 时，当前线程会进入**等待状态**。

- **优势**: 极大简化了异步结果同步获取的逻辑。
- **风险**: 如果任务永远不结束，代码会死锁在这里。
- **最佳实践**: 始终使用带有超时限制的获取方式：`future.get(5, TimeUnit.SECONDS)`。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当你在处理多个并行的远程 API 调用时：

> **最佳实践 Prompt**:
> "我需要同时从 3 个不同的 REST API 获取数据并进行过滤聚合。
>
> 1. 请帮我使用 `Callable` 来定义这三个异步任务。
> 2. 请展示如何使用线程池提交任务并获得 3 个 `Future` 对象。
> 3. 请说明如何使用 `CompletableFuture`（进一步进阶）来替代传统的 `Future.get()` 以避免阻塞主线程。"

```java
import java.util.concurrent.*;

public class FutureExample {
    public void executeTasks() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 提交 3 个任务
        Future<ApiResponse> future1 = executor.submit(new ApiTask("API-1"));
        Future<ApiResponse> future2 = executor.submit(new ApiTask("API-2"));
        Future<ApiResponse> future3 = executor.submit(new ApiTask("API-3"));

        // 获取结果（注意：这里 get() 会阻塞主线程，直到任务完成）
        ApiResponse res1 = future1.get();
        ApiResponse res2 = future2.get();
        ApiResponse res3 = future3.get();

        System.out.println("聚合结果: " + res1 + ", " + res2 + ", " + res3);

        executor.shutdown();
    }
}
```

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Baeldung: Runnable vs Callable in Java](https://www.baeldung.com/java-runnable-callable) - 简明扼要的对比。
2. [Effective Java (Item 79-81)](https://book.douban.com/subject/30240400/) - 并发工具类的最佳实践。
