# 18 - 多线程调试：如何在断点下精准操控并发逻辑

## 核心心智映射 (Core Mental Mapping)

在 Node.js 中，由于是单线程事件循环，调试逻辑相对直观。但在 Java 的多核环境下，多个线程会同时冲过同一行代码。掌握 IDE 的“线程控制术”是排查并发 Bug 的必备技能。

| 领域 | Node.js (单线程) | Java (多核并发) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **执行流** | 顺序执行，单点控制 | 并行执行，多个“我”在奔跑 | 掌控多个平行的灵魂 |
| **断点挂起** | 执行流停在当前行 | 默认挂起所有线程 (Stop the world) | 挂起策略决定是否“时间静止” |
| **上下文切换** | 只能按单线流程走 | 可以在不同线程间手动切换 | “监控室”视角切换 |
| **并发复现** | 依赖特定的异步顺序 | 可以人工制造 Race Condition | 手动干扰执行顺序 |

---

## 概念解释 (Conceptual Explanation)

### 1. 悬挂策略 (Suspend Policy)
这是多线程调试的灵魂。
-   **Suspend: All**: 只要有一个线程命中断点，JVM 所有线程全部停下。**场景**: 调试全局逻辑。
-   **Suspend: Thread**: 只有命中断点的那个线程停下，其他线程继续运行。**场景**: 专门用来调试线程竞争问题。

### 2. 线程上下文 (Thread Context)
在断点停留处，如果你切换线程，所有的局部变量、调用堆栈都会瞬间切换。这就像在不同的平行世界中穿梭。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 断点右键菜单
1.  在行号处打一个普通断点。
2.  **右键点击断点小红点**。
3.  勾选 `Suspend: Thread`。
4.  点击 `Make Default`（强烈建议，这是并发调试的最佳伴侣）。

---

## 典型用法 (Typical Usage)

### 情景：模拟一个 Race Condition (数据丢失)
1.  在 `count++` 那一行打上 `Suspend: Thread` 断点。
2.  **启动线程 A**: 到达断点，查看 count 值为 0，停住它。
3.  **切换到线程 B**: 在另一个核心运行 B，让 B 完整跑完这一行并写回。此时 count 的真实值为 1。
4.  **切回线程 A**: 强制让 A 继续运行。
5.  **结果**: 此时 A 拿的是旧值 0，执行 `0 + 1` 后写回 1。你会发现 count 最终是 1 而非 2！

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 Debugger 中的 **Threads 窗口**:
它列出了当前所有存活的线程（如：`main`, `pool-1-thread-1`, `TuningWorker-2`）。
通过点击不同的线程名，你可以看到由于每个线程当前所处的逻辑环节不同，它们的堆栈信息完全不同。这是定位死锁（Deadlock）和资源竞争的核心手段。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当并发 Bug 无法稳定复现时：

> **最佳实践 Prompt**:
> "我有一段代码在多线程下偶尔会出现数据不一致的问题。`[贴入代码]`。
> 1. 请帮我分析代码逻辑，并告诉我在哪几行打上 `Suspend: Thread` 断点最合适。
> 2. 请描述一套手动的『多线程调试剧本』：我应该先停住哪一个线程，让哪一个线程由于什么原因先通过，从而复现场景。
> 3. 请说明如何在获取到线程转储 (Thread Dump) 后，请你帮我分析其中的死锁信息。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [IntelliJ IDEA: Debug Multithreaded Applications](https://www.jetbrains.com/help/idea/debug-multithreaded-applications.html) - 官方视频指南。
2. [Baeldung: Debugging with IntelliJ IDEA](https://www.baeldung.com/intellij-idea-debugging) - 综合性的调试技巧总结。
3. [Oracle: Analyzing Thread Dumps](https://docs.oracle.com/en/java/javase/11/troubleshoot/troubleshoot-multithreaded-applications.html) - 系统级问题的必杀技。
