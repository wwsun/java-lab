# 17-线程池深度调优：ThreadPoolExecutor 七大参数全解析

为什么阿里的 Java 开发手册禁止使用 `Executors.newFixedThreadPool()`？因为它默认的阻塞队列长度是 `Integer.MAX_VALUE`，在高并发下会导致 OOM (内存溢出)。

## 1. 核心流程图：任务的一生

当一个新任务提交到线程池时，它的命运遵循以下顺序：

1.  **核心线程 (Core)**：如果核心线程还没满，创建一个新线程直接执行（即使有空闲的核心线程，也会首选创建新线程直到达到核心数）。
2.  **阻塞队列 (Queue)**：如果核心线程已满，任务被丢入队列。**这是最关键的一点：普通情况下，满队列 > 扩容。**
3.  **最大线程 (Max)**：如果队列也满了，且当前线程数 < 最大线程数，则创建非核心线程（临时工）来支援。
4.  **拒绝策略 (Reject)**：如果最大线程也满了，且队列也满了，线程池将根据拒绝策略开始“投诉”或“丢弃”。

## 2. 七大核心参数定义

| 参数名 | 说明 | 调优建议 |
| :--- | :--- | :--- |
| `corePoolSize` | 核心线程数 | **常驻员工**。CPU 密集型设为 N+1，IO 密集型设为 2N。 |
| `maximumPoolSize` | 最大线程数 | **总编制**。包含常驻员工和临时支援。 |
| `keepAliveTime` | 非核心线程存活时间 | **试用期**。当临时工闲下来多久后被解雇。 |
| `unit` | 时间单位 | `TimeUnit.SECONDS` 等。 |
| `workQueue` | 任务队列 | **等待位**。建议使用有界的 `ArrayBlockingQueue`。 |
| `threadFactory` | 线程工厂 | 给线程起个好听的名字 (便于排查 Dump)。 |
| `handler` | 拒绝策略 | **兜底方案**。当处理不过来时的应对逻辑。 |

## 3. 四种拒绝策略 (Handler)

1.  **`AbortPolicy` (默认)**：直接抛出异常，让主流程知道挂了（最安全，强制关注）。
2.  **`CallerRunsPolicy`**：谁派的任务谁执行。主线程自己去跑，这会天然起到“降速”作用。
3.  **`DiscardPolicy`**：悄悄丢弃，不报任何错。极其危险。
4.  **`DiscardOldestPolicy`**：丢弃队列里等得最久的任务，尝试腾位置。

## 4. 给 Node.js 开发者的建议

- **资源受控**：Node.js 的 Event Loop 虽然是非阻塞的，但在密集计算时也会挂起。Java 线程池的核心目标是 **“资源限制”** —— 宁可报拒绝错误，也不要把服务器内存撑爆。
- **自定义队列**：始终记得配置有界队列（如长度 500），永远不要用无限队列。

---
> [!IMPORTANT]
> **实践小贴解：** 接下来我们将通过 `ThreadPoolTuningDemo` 复现任务从核心线程 -> 队列排队 -> 最大线程扩容的惊险全过程。

**参考资料**：
- [Java ThreadPoolExecutor - Seven Parameters Explained](https://www.baeldung.com/java-threadpool-executor-service)
- [Uber Engineering: Effective Java Threading](https://eng.uber.com/java-threadpool-best-practices/)
