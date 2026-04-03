# 18-多线程调试：如何在断点下精准操控并发逻辑

在 Node.js 中，由于是单线程事件循环，调试逻辑相对直观。但在 Java 的多核环境下，多个线程会同时冲过同一行代码。掌握 IDE 的“线程控制术”是排查并发 Bug 的必备技能。

## 1. 核心秘诀：Suspend Policy (悬挂策略)

默认情况下，IntelliJ IDEA 的断点会挂起 **所有线程 (All)**。这在调试并发时是致命的，因为它会掩盖竞态冲突。

### 💡 关键操作：
1.  在代码行号处点击，设置一个断点。
2.  **右键点击** 该断点（那个红色小圆点）。
3.  将 `Suspend` 策略从 `All` 改为 **`Thread`**。
4.  点击 `Make Default`（建议：以后所有并发调试都用此设置）。

**为什么要这么做？**
- `All`：JVM 时间静止，所有线程都停了。
- `Thread`：只有走到这一行的那个线程停了，**其他线程依然在后台全速运行。**

---

## 2. 线程切换：Debugger 里的“监控室”

当你挂起了多个线程后，如何在它们之间穿梭？

- **Frames / Threads 窗口**：在 Debugger 选项卡中，有一个下拉列表或侧边栏，列出了当前所有存活的线程。
- **切换堆栈**：点击不同的线程名字（如 `TuningWorker-1`），你可以看到由于每个线程当前所处的执行位置不同，它们的局部变量、调用链路完全不同。

---

## 3. 开发实践：模拟一个经典的 Race Condition

在 [DebugMeDemo.java](../src/main/java/com/javalabs/DebugMeDemo.java) 中，我们将演示如何手动制造“数据覆盖”。

### 实操步骤：
1.  在 `count++` 那一行打上 `Suspend: Thread` 断点。
2.  启动 Debug 模式。
3.  **第一步**：线程 A 到达断点，观察获取到的旧值（假设是 0），停住它。
4.  **第二步**：在线程列表里，切换到线程 B，让线程 B 跑完这一行并提交（此时共享变量变成了 1）。
5.  **第三步**：切回线程 A，强行让 A 继续运行。
6.  **结果**：你会惊奇地发现，由于 A 拿的是旧值 0，它计算出的 `0+1` 依然是 1，最后写回 1。
    - **现象**：两个线程都执行了 `++`，但结果却是 1 而不是 2！

---

## 4. 给 Node.js 开发者的建议

- **上帝视角**：在 Node.js 中你只能调试当前的执行流。在 Java 中，你可以同时把两个线程挂在同一行，观察它们对同一块内存的竞争。
- **防止超时**：调试多线程时，如果涉及数据库连接或网络请求，挂起太久可能会导致连接泄露或超时断开。

---
> [!IMPORTANT]
> **实践提示：** 接下来请打开 `DebugMeDemo.java`，按照本指南尝试手动复现一次数据丢失。

**参考资料**：
- [IntelliJ IDEA Debugging Multithreaded Applications](https://www.jetbrains.com/help/idea/debug-multithreaded-applications.html)
- [Baeldung: Guide to Debugging with IntelliJ IDEA](https://www.baeldung.com/intellij-idea-debugging)
