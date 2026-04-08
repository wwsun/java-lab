# 19 - Java 21 虚拟线程：百万级并发的秘密武器

## 核心心智映射 (Core Mental Mapping)

Java 21 引入的虚拟线程 (Project Loom) 是 Java 史上最重要的变革之一。它彻底解决了“同步阻塞代码难以处理极高并发”的痛点。

| 领域 | Node.js (Async/Await) | Java 21 (Virtual Threads) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **编程模型** | 异步非阻塞 (回调/Promise) | **同步线性 (顺序执行)** | 逻辑简单度 |
| **I/O 阻塞** | 事件驱动，手动 yield | **底层透明切换** | 阻塞对开发者的感知 |
| **并发单位** | Event Loop / Coroutines | **虚拟线程 (Thread)** | 轻量化单元 |
| **资源消耗** | 极低 | **极低 (如纸片般廉价)** | 内存占用 |
| **调度器** | JavaScript 内核 | **JVM 调度器 (M:N)** | 谁在负责任务切换 |

---

## 概念解释 (Conceptual Explanation)

### 1. 平台线程 vs 虚拟线程
-   **平台线程 (Platform Threads)**: 1:1 映射操作系统内核线程。贵！一个线程约占 1MB 内存，几千个就把内存耗尽了。
-   **虚拟线程 (Virtual Threads)**: M:N 映射。几万个虚拟线程可以映射到几个载体线程上。轻！一个仅约几百字节。

### 2. 魔法来源：自动卸载 (Unmounting)
当虚拟线程遇到阻塞 I/O（如数据库查询）时，JVM 会自动将该虚拟线程从真实 CPU 线程（载体线程）上“卸载”，把 CPU 让给其他虚拟线程运行。这一切对开发者完全透明，你不需要写任何特殊的 `async` 关键字。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 开启虚拟线程
```java
// 方法 A：直接启动
Thread.ofVirtual().start(() -> {
    System.out.println("Hello from Virtual Thread!");
});

// 方法 B：创建虚拟线程池 (推荐)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        Thread.sleep(Duration.ofSeconds(1)); // 阻塞不会拖累系统
        return "Done";
    });
}
```

---

## 典型用法 (Typical Usage)

### 处理海量短连接
在传统的 Spring Boot 应用中，如果是 I/O 密集型（如微服务之间频繁调用），只需将 Tomcat 的线程池替换为虚拟线程执行器，系统吞吐量往往能有数倍提升，而内存占用反而下降。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察以下代码如何处理 **100,000 个并发任务**:
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 100_000).forEach(i -> {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1)); // 所有人都在睡 1 秒
            return i;
        });
    });
}
```
**结果**: 整个过程可能只占用几十个物理核心线程，耗时仅略多于 1 秒。
**对比**: 如果使用传统的 `FixedThreadPool`，开启 10 万个线程会直接导致内存溢出 (OOM)；如果只开 100 个线程，则需要 1000 秒才能处理完。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

虚拟线程最适合老项目的“无感升级”。

> **最佳实践 Prompt**:
> "我有一个处理大量 Webhook 回调的旧 Java 8 服务，目前由于 I/O 等待导致线程池经常被占满，响应极慢。
> 1. 请帮我检查代码中是否存在阻塞操作。
> 2. 请展示如何将现有的 `FixedThreadPool` 替换为 Java 21 的虚拟线程。
> 3. 请说明在升级虚拟线程后，为什么不再需要对线程池进行复杂的核数计算和参数调优。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [JEP 444: Virtual Threads](https://openjdk.org/jeps/444) - 官方标准提案，必读。
2. [Baeldung: Guide to Java Virtual Threads](https://www.baeldung.com/java-virtual-threads) - 包含性能对比测试。
3. [InfoQ: Project Loom: Modern Concurrency for Java](https://www.infoq.com/articles/java-virtual-threads/) - 深度解析执行模型。
