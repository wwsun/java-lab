# 14 - 并发入门：从“餐厅厨师”理论到线程安全实战

## 核心心智映射 (Core Mental Mapping)

Java 的多线程并不是为了“让代码变复杂”，而是为了“让 CPU 别闲着”。

| 领域 | Node.js (单线程事件循环) | Java (多线程模型) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **执行模型** | **单厨师**：处理快餐，遇到重活（I/O）交给后台 | **多厨师**：大型酒楼，多个核心同时开火 | 任务并行处理能力 |
| **共享资源** | 默认隔离，通过 IPC 通信 | **共享内存**：所有人看同一个案板 | 资源竞争与加锁 |
| **阻塞后果** | 整个进程卡死 (CPU 密集型) | 仅当前线程阻塞，其他线程继续运行 | 系统的健壮性 |
| **并发手段** | `Worker Threads` (重量级) | `Thread` / `Thread Pool` / `Virtual Threads` | 灵活的并发抽象 |

---

## 概念解释 (Conceptual Explanation)

### 1. 并发 (Concurrency) vs 并行 (Parallelism)
-   **并发**: 多个任务在同一时间段内交替执行（类似一个厨师在两个锅之间快速来回）。
-   **并行**: 多个任务在同一时刻真正同时执行（两个厨师各炒一个锅）。

### 2. 线程安全 (Thread Safety)
当多个线程访问同一个对象时，如果不用考虑它们的执行顺序，代码依然能得到正确的结果，就叫线程安全。
> **i++ 陷阱**: 这是由于 `i++` 非原子操作（包含读、加、写三步），在并发下会丢失修改。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 线程池 (ExecutorService)
千万不要直接 `new Thread()`，使用线程池来复用资源：
```java
ExecutorService pool = Executors.newFixedThreadPool(10);
pool.submit(() -> System.out.println("Hello from Thread!"));
```

### 线程安全利器
-   **`synchronized`**: 独占锁，保证一段代码在同一时刻只能有一个线程进入。
-   **`AtomicInteger`**: 利用 CAS 无锁技术实现高性能数字累加。
-   **`ConcurrentHashMap`**: 高性能、线程安全的字典。

---

## 典型用法 (Typical Usage)

### 什么时候需要多线程？
1.  **I/O 密集型**: 例如爬虫、文件读取。主线程下发任务，线程池负责等待网络放回。
2.  **CPU 密集型**: 视频转码、大规模数学加密，分而治之，榨干多核效能。
3.  **异步操作**: 用户下单后，主流程直接返回，后台异步发送通知邮件。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察以下 **`i++` 翻车现场**：
```java
// 50,000 次累加结果竟然变成了 14,000 多
for (int i = 0; i < 50000; i++) {
    executor.submit(() -> count++);
}
```
**原因解析**:
1. 厨师 A 读到计数器是 10。
2. 厨师 B 也读到 10。
3. 厨师 A 计算出 11 并写回。
4. 厨师 B 也计算出 11 并写回。
**结果**: 两次累加，结果只增加了 1。这就是典型的**竞态条件 (Race Condition)**。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

并发 Bug 是最难重现的（Heisenbug）。

> **最佳实践 Prompt**:
> "我写了一段多线程代码来处理订单，但在高并发测试下出现了金额不一致的问题。`[贴入代码]`。
> 1. 请帮我分析哪些变量在多线程共享时存在线程安全隐患。
> 2. 请建议应该使用 `synchronized`、`ReentrantLock` 还是原子类（Atomic）来修复它。
> 3. 请说明在高并发下如何进行压力测试以复现该问题。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Java Concurrency in Practice](https://jcip.net/) - 《Java 并发编程实战》，并发领域的圣经。
2. [Baeldung: ThreadPoolExecutor in Java](https://www.baeldung.com/java-threadpool-executor-service) - 实战指南。
3. [小林 coding：线程安全是什么？](https://xiaolincoding.com/os/4_process/process_safe.html) - 通俗易懂的并发原理。
