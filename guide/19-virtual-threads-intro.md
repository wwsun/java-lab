# 19-Java 21 虚拟线程：百万级并发的秘密武器

Java 21 引入的虚拟线程 (Project Loom) 是 Java 史上最重要的变革之一。它彻底解决了“同步阻塞代码难以处理高并发”的痛点。

## 1. 核心矛盾：平台线程 vs 虚拟线程

在 Java 21 之前，Java 线程是 **平台线程 (Platform Threads)**：
- **1:1 映射**：一个 Java 线程对应一个操作系统的内核线程。
- **代价昂贵**：内核线程创建慢、占用内存大（约 1MB/线程）、上下文切换代价高。
- **瓶颈**：由于内存限制，一台普通服务器最多只能同时支撑几千个线程。

在 Java 21 之后，引入了 **虚拟线程 (Virtual Threads)**：
- **M:N 映射**：成千上万个虚拟线程可以映射到极少数的平台线程（载体线程）上。
- **廉价如纸**：虚拟线程非常轻量（约几百字节），创建极快，几乎不占什么内存。
- **底层的“魔法”**：当虚拟线程遇到阻塞 I/O（如数据库查询、`Thread.sleep`）时，JVM 会自动将该虚拟线程从载体线程上“卸载”，把 CPU 让给其他虚拟线程。

---

## 2. 给 Node.js 开发者的心智对标

如果您熟悉 Node.js 的事件循环，可以这样理解：

- **Node.js (1:N)**：单线程事件循环。你必须写 `async/await` 非阻塞代码，否则整个进程都会被阻塞。
- **Java 21 (M:N)**：JVM 背后其实也有一个类似事件循环的调度器。但它对开发者透明。你可以用 **“同步写法”** 写代码，遇到阻塞时，JVM 自动把它变成类似异步的执行，你不需要写任何回调！

| 特性 | Node.js (Async/Await) | Java 21 (Virtual Threads) |
| :--- | :--- | :--- |
| **编程模型** | 异步非阻塞（容易产生回调地狱） | **同步线性（直观好懂）** |
| **I/O 阻塞处理** | 手动处理 Promise | **底层自动切换（透明）** |
| **并发单位** | 事件循环 / Coroutines | **虚拟线程 (Thread)** |

---

## 3. 如何使用虚拟线程？

Java 21 为我们提供了极其直观的 API：

```java
// 方式 A：直接创建一个虚拟线程
Thread.ofVirtual().start(() -> {
    System.out.println("Hello from Virtual Thread!");
});

// 方式 B：创建一个“为每个任务开启一个虚拟线程”的线程池 (推荐)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        // 这里的阻塞代码不会拖慢整个系统！
        Thread.sleep(Duration.ofSeconds(1));
        return "Done";
    });
}
```

## 4. 这里的“陷阱”与准则

1.  **不需要池化**：以前平台线程很贵，所以我们要用 `FixedThreadPool` 复用；虚拟线程极便宜，**随用随建，用完即弃**，永远不要为了复用而把它们塞进池子。
2.  **避免长时间 CPU 计算**：虚拟线程是为 I/O 密集型设计的。如果是 CPU 疯狂计算（如加解密、视频编码），它依然会占用载体线程，体现不出优势。

---
> [!IMPORTANT]
> **实践小贴士：** 接下来我们将通过 `VirtualThreadDemo.java` 真枪实弹地开启 **10 万个并发线程**，看看传统线程池和虚拟线程谁会先挂掉。

**参考资料**：
- [JDK 21 Release Notes - Virtual Threads](https://openjdk.org/jeps/444)
- [Baeldung: Guide to Java Virtual Threads](https://www.baeldung.com/java-virtual-threads)
