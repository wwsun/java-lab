# 17 - 线程池深度调优：ThreadPoolExecutor 七大参数全解析

## 核心心智映射 (Core Mental Mapping)

Node.js 的 Event Loop 虽然高效，但在密集任务下也会饱和。Java 的线程池则提供了一套高度可控的“资源限制”方案。

| 概念阶段     | Node.js (单线程)   | Java (线程池)            | 心智映射               |
| :----------- | :----------------- | :----------------------- | :--------------------- |
| **任务到达** | 放入事件队列       | 先看核心线程是否已满     | 第一响应层             |
| **等待区**   | 唯一的事件队列     | **有界阻塞队列 (Queue)** | 挂起区的缓冲能力       |
| **应急措施** | (无，只能等待)     | **最大线程 (Max)**       | 应对流量突刺的“临时工” |
| **彻底饱和** | 内存溢出或响应极慢 | **拒绝策略 (Reject)**    | 系统的“熔断保护”       |

---

## 概念解释 (Conceptual Explanation)

### 1. 任务的“一生” (生命周期)

当一个新任务提交到线程池时，它的命运遵循以下顺序：

1.  **核心线程 (Core)**: 还没满？直接雇个正式工开干。
2.  **阻塞队列 (Queue)**: 核心满了？去排队位等着。
3.  **最大线程 (Max)**: 队列也满了？赶紧雇个临时工。
4.  **拒绝策略 (Reject)**: 临时工也满了？对不起，概不接待。

### 2. 为什么禁止使用 Executors 快捷方法？

`Executors.newFixedThreadPool()` 默认使用 `LinkedBlockingQueue`，其长度是 `Integer.MAX_VALUE`。这意味着队列几乎永远不会满，导致任务无限堆积，最终撑爆内存 (OOM)。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 七大核心参数

```java
public ThreadPoolExecutor(
    int corePoolSize,        // 核心线程数 (常驻员工)
    int maximumPoolSize,     // 最大线程数 (总编制)
    long keepAliveTime,      // 非核心线程存活时间 (临时工试用期)
    TimeUnit unit,           // 时间单位
    BlockingQueue<Runnable> workQueue, // 任务队列 (等待区)
    ThreadFactory threadFactory,       // 线程工厂 (起个好听的名字)
    RejectedExecutionHandler handler   // 拒绝策略 (熔断方案)
)
```

---

## 典型用法 (Typical Usage)

### 1. CPU 密集型 (计算多)

- **建议**: `corePoolSize = CPU核心数 + 1`。目的是减少线程上下文切换。

### 2. I/O 密集型 (等待多)

- **建议**: `corePoolSize = CPU核心数 * 2` (或更多)。目的是让 CPU 在等待 I/O 时去处理其他请求。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察“总承载能力”计算公式：
**总承载量 = maximumPoolSize + workQueue 容量**

假设配置为：`core=2, max=4, queue=3`。

- 提交第 1-2 个任务：开启核心线程。
- 提交第 3-5 个任务：进入队列。
- 提交第 6-7 个任务：开启最大线程（正式工+临时工）。
- 提交第 8 个任务：**拒绝执行**！

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

手动调优线程池参数非常考验经验。

> **最佳实践 Prompt**:
> "我有一个处理图片缩略图的服务，由于是 CPU 密集型任务，每张图处理需 200ms。服务器有 4 核 CPU，16G 内存。
>
> 1. 请帮我计算并给出最合理的 `ThreadPoolExecutor` 初始化参数。
> 2. 请建议应该使用哪种拒绝策略（如 `CallerRunsPolicy`）以防止任务请求丢失。
> 3. 请展示如何使用自定义 `ThreadFactory` 为这些线程命名，以便于在报错日志中识别该业务模块。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Baeldung: ThreadPoolExecutor Explained](https://www.baeldung.com/java-threadpool-executor-service) - 深入参数细节。
2. [Uber Engineering: Effective Java Threading](https://eng.uber.com/java-threadpool-best-practices/) - 大规模生产环境下线程池的调优心经。
