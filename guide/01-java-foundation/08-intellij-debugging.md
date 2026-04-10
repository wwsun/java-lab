# 08 - IntelliJ IDEA 调试杀手锏

## 核心心智映射 (Core Mental Mapping)

如果你习惯了 VS Code 的调试器，你会发现 IDEA 的 Debugger 就像是一把精准的手术刀。Java 的强类型静态编译特性，赋予了调试器非常深刻的上下文感知能力。

| 调试场景 | Node.js (VS Code) | Java (IntelliJ IDEA) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **基础调试** | 断点 & Variables | 断点 & Variables | IDEA 支持级联跳转和深度堆栈查看 |
| **修改变量** | Console 赋值 | **Set Value (F2)** | 运行时直接修改内存中的变量 |
| **代码跳转** | 单步进入/跳出 | **Drop Frame** | IDEA 可以“时光倒流”，重新跑一遍当前方法 |
| **实时评估** | Debug Console | **Evaluate Expression** | 支持完整类型补全的实时代码执行 |
| **热更新** | Nodemon / HMR | **HotSwap** | 不停进程，改完编译即生效 |

---

## 概念解释 (Conceptual Explanation)

### 1. 断点 (Breakpoint)
不仅是停在某一行。IDEA 支持：
- **行断点**: 最常用。
- **方法断点**: 停在方法入口或出口。
- **异常断点**: 当代码抛出特定异常（如 `NullPointerException`）时自动停下。

### 2. 堆栈帧 (Frames)
每一个方法调用都会在堆栈中生成一个“帧”。IDEA 允许你在帧之间自由穿梭，查看调用链路。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 调试核心快捷键
-   **F8 (Step Over)**: 下一步，不进方法。
-   **F7 (Step Into)**: 进方法。
-   **Shift + F8 (Step Out)**: 出方法。
-   **Alt + F8 (Evaluate Expression)**: **最强 API**，可以在断点处运行任何合法的 Java 代码。
-   **F9 (Resume)**: 恢复执行直到下一个断点。

---

## 典型用法 (Typical Usage)

### 时光倒流：Drop Frame
如果你在调试时错过了关键逻辑（比如 `if` 走错了分支），不需要重启！
-   **操作**: 在 `Frames` 窗口右键当前方法，选择 **Drop Frame**。
-   **效果**: 程序指针会退回到当前方法**调用前**的状态，你可以再次单步进入。

### 精准打击：Condition Breakpoint
如果循环 10,000 次，只有第 9527 次会报 Bug：
-   **操作**: 断点红点处右键，在 `Condition` 框输入 `i == 9527`。
-   **效果**: 只有条件满足时调试器才会拦截。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察如何使用 **Evaluate Expression** 调试 Stream：
当你在处理集合时，可以通过 `Alt + F8` 打开窗口并输入：
```java
list.stream().filter(Objects::nonNull).count()
```
IDEA 会立即计算出结果。这在你不确定中间数据转换是否正确时非常有用，避免了反复加 `println` 再重启应用的低效循环。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当遇到复杂的并发或逻辑 Bug，自己理不清堆栈时：

> **最佳实践 Prompt**:
> "我遇到了一个复杂的逻辑 Bug，这是目前的调用堆栈截图/文字：`[贴入 Frames 或 Exception 堆栈]`。
> 1. 请帮我定位可能的根因（Root Cause）。
> 2. 请告诉我应该在哪些关键类和方法上设置『条件断点』，以及具体的条件表达式是什么。
> 3. 如果需要模拟某种异常场景，请告诉我如何在 Evaluate Expression 中通过代码触发它。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [JetBrains: Debugging your first Java application](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) - 官方视频带教。
2. [Mastering IntelliJ IDEA Debugger](https://blog.jetbrains.com/idea/2023/04/mastering-intellij-idea-debugger/) - 深度技巧进阶。
