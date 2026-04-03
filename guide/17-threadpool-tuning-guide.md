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

---

## 4. 开发实践：详解 ThreadPoolTuningDemo 设计

在 [ThreadPoolTuningDemo.java](../src/main/java/com/javalabs/ThreadPoolTuningDemo.java) 中，我们通过两个实验完整复现了线程池的“压力流转”。

### 实验 A：全流程饱和攻击 (`runSaturationTest`)

这是最经典的学习案例。我们构建了一个特殊的线程池：
- **参数配置**：`core=2`, `queue=3`, `max=4`。
- **任务目标**：理解为什么 **“总承载能力为 7”**。

> [!TIP]
> **核心计算公式：**
> **线程池总承载量 (Total Capacity) = 最大线程数 (maximumPoolSize) + 队列容量 (workQueue capacity)**
> 在本例中：**4 (Max) + 3 (Queue) = 7**。提交第 8 个任务必爆。

**实验设计拆解 (状态流转)：**
1.  **任务 1-2 (填充核心)**：命中 `corePoolSize=2`。此时 `Pool Size` 增加到 2，任务立即由 `TuningWorker-1/2` 执行。
2.  **任务 3-5 (填充队列)**：核心已满。任务进入 `ArrayBlockingQueue(3)` 排队。此时 `Pool Size` 依然为 2，但 `Queue Size` 增加到 3。
3.  **任务 6-7 (触发扩容)**：队列也满了！池子触发**扩容**，启动非核心线程（临时工）。此时 `Pool Size` 增加到 4。
    - *心智对白：既然排队也排不下，老板赶紧多雇 2 个人来帮厨。*
4.  **任务 8 (触发拒绝)**：**彻底饱和**（4 干活位 + 3 等位 = 7）。第 8 个任务因为超出了总承载能力，触发默认的 `AbortPolicy`。

---

---

### 实验 B：拒绝策略切换 (`runCallerRunsDemo`)

当系统过载时，报错 (`AbortPolicy`) 并不是唯一选择。

**代码设计逻辑：**
- 我们切换到了 `CallerRunsPolicy`。
- **运行表现**：当池子无法承受新任务时，主线程（`main`）将不再异步派发，而是**亲自运行**该任务。
- **调优心法**：这在 Node.js 中类似“同步阻塞”。但在 Java 里，这是一种极佳的**天然降流**机制——主任务忙着跑任务，就没法继续提交新任务，从而给了线程池消化存量任务的时间。

### 实验 C：自定义 ThreadFactory 设计

在 [ThreadPoolTuningDemo.java](../src/main/java/com/javalabs/ThreadPoolTuningDemo.java) 的 L77 处，我们实现了一个自定义工厂。

**核心价值解密：**
- **赋予“灵魂” (线程命名)**：默认线程名 `pool-1-thread-1` 没有任何业务含义。使用 `TuningWorker-n` 命名，能让线上故障排查效率提升 10 倍。
- **线程安全性**：工厂内部必须使用 `AtomicInteger` 进行编号计数，防止多核环境下创建线程时编号出现“重号”。
- **统一配置**：在工厂内可以统一设置线程的优先级（Priority）和是否为守护线程（Daemon）。

> [!IMPORTANT]
> **专家锦囊：** 在生产环境下导出的 **Thread Dump** 日志中，自定义的线程名能让你瞬间定位是哪个业务模块（如：`Order-Pool`、`Sms-Pool`）正在堆积。

## 5. 给 Node.js 开发者的建议

- **资源受控**：Node.js 的 Event Loop 虽然是非阻塞的，但在密集计算时也会挂起。Java 线程池的核心目标是 **“资源限制”** —— 宁可报拒绝错误，也不要把服务器内存撑爆。
- **自定义队列**：始终记得配置有界队列（如长度 500），永远不要用无限队列。

---
> [!IMPORTANT]
> **实践小贴士：** 运行 Demo 时，请密切观察控制台打印的 `[Submitted] Pool Size` 变化。你会发现 Pool Size 并不是匀速增长的，而是先填满 Core，再填满 Queue，最后才跳到 Max。

**扩展阅读：**
- [Java ThreadPoolExecutor - Seven Parameters Explained](https://www.baeldung.com/java-threadpool-executor-service)
- [Uber Engineering: Effective Java Threading](https://eng.uber.com/java-threadpool-best-practices/)
