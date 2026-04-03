# 13-并发模型：Node.js Event Loop vs Java Thread Model

作为 Node.js 开发者，理解 Java 并发的最大障碍通常不是语法，而是“心智模型”。

## 1. 核心架构对比

### Node.js：单人旋转餐厅 (Event Loop)
- **模型**：单线程处理请求，遇到 I/O 就交给内核，自己继续处理下一个。
- **优点**：极高的 I/O 并发，没有线程切换开销，没有竞态条件。
- **代价**：一旦有一个请求在做 CPU 密集型计算（如加密、解密），整个服务器就会卡死。

### Java：厨师团队 (Thread-per-request)
- **模型**：为每个请求分配一个独立的线程。如果线程遇到 I/O，它会“阻塞”在那里等待。
- **优点**：能够充分利用多核 CPU。即便一个线程在计算，其他厨师依然能干活。
- **代价**：线程占用内存较多（每个约 1MB），过多的线程会导致上下文切换 (Context Switch) 消耗极大。

## 2. 为什么 Java 不怕“阻塞”？

在 Node.js 中，“阻塞”是死罪。但在 Java 中，这是标准操作。
- **原因**：Java 有成百上千个线程。一个线程睡了，操作系统会自动调度另一个线程继续工作。
- **心智映射**：
    - Node.js 的 `fs.readFile(path, cb)` 是异步回调。
    - Java 的 `Files.readString(path)` 是同步阻塞，但它跑在独立的线程里，互不干扰。

## 3. 异步演进：从 Callback 到 CompletableFuture

虽然 Java 提倡同步写法，但它也有强大的异步工具：
- **`CompletableFuture`**：类比 JS 的 **`Promise`**。
- **`thenApply()` / `thenCompose()`**：类比 JS 的 **`.then()`**。
- **`supplyAsync(() -> ...)`**：类比 JS 的 **`new Promise((resolve) => ...)`**。

## 4. Java 21 的大杀器：虚拟线程 (Virtual Threads)

如果你觉得 1000 个线程就内存溢出了怎么办？Java 21 推出了**虚拟线程**。
- **原理**：像 Go 的 Goroutine 一样，虚拟线程非常轻量（几 KB）。
- **效果**：你可以像写同步代码一样简单，却能支撑百万级别的并发，完美融合了 Node.js 的低成本和 Java 的多核优势。

---
> [!TIP]
> **本周重点：** 我们先掌握传统的 **线程池 (ExecutorService)**。这是目前大多数 Java 线上业务的基石。

**参考资料**：
- [Project Loom (Virtual Threads) JEP 444](https://openjdk.org/jeps/444)
- [Node.js Event Loop Explained](https://nodejs.org/en/docs/guides/event-loop-timers-and-nexttick/)
