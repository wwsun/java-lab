# 06-IntelliJ IDEA 调试杀手锏 (IntelliJ Debugging Tips)

作为从轻量级编辑器（如 VS Code）转型过来的 Java 开发者，IDEA 的 Debugger 是你最强的武器。Java 是一种**强类型静态编译**语言，这使得它的调试器能做到非常精准的“手术级”操作。

## 1. 核心心智映射：VS Code vs IDEA

| 调试场景       | Node.js (VS Code)            | Java (IntelliJ IDEA)                             |
| -------------- | ---------------------------- | ------------------------------------------------ |
| **基础调试**   | 设置断点，看 Variables 面板  | 同样，但面板信息更深，支持级联跳转               |
| **修改变量**   | Console 里手动改 `obj.a = 2` | **Set Value** (F2) 或 **Evaluate Expression**    |
| **热替换代码** | 依赖 `nodemon` 或 HMR 重启   | **HotSwap** (不用重启进程，改完代码直接编译生效) |
| **回溯代码**   | 只能重启或手动跳转           | **Drop Frame** (时光倒流，让当前方法重新跑一遍)  |

## 2. 必须掌握的三个“杀手锏”

### A. Drop Frame (扔掉帧) —— 时光倒流

这是 Java 调试中最惊艳的功能。如果你刚刚在 Debug 过程中错过了一个关键逻辑（例如 `if` 走错了分支），你不需要重启应用。

- **操作**：在 Debugger 窗口左侧的 `Frames` 列表中，右键点击当前方法，选择 **Drop Frame**。
- **效果**：程序执行位置会回退到当前方法**调用前**的状态，你可以再次单步执行进入该方法。

### B. Evaluate Expression (计算表达式) —— 实时演练

在断点停留处，按 `Alt + F8`。

- **用途**：你可以输入任何合法的 Java 代码（包括复杂的 Stream API 调用或调用远程接口），IDEA 会在当前上下文中实时编译并给出结果。
- **Node.js 对比**：类似于 Chrome DevTools 的 Console，但它支持完整的类型推断和补全。

### C. Condition Breakpoints (条件断点) —— 精准打击

如果一个 `for` 循环循环了 10,000 次，但 bug 只出现在 `i == 9527` 的时候怎么办？

- **操作**：在断点红点处右键，在 `Condition` 框中输入 `i == 9527`。
- **效果**：只有满足条件时，调试器才会停下来。

## 3. 热替换 (Hot Swap)

在 Debug 模式运行中，如果你直接修改了某个方法的逻辑（只要没有增减类成员变量或修改方法签名），按下 `Ctrl+Shift+F9` (Recompile)，IDEA 会提示你 `Reload changed classes?`。

- 选择 **Yes** 之后，新的代码逻辑会立刻在运行中的 JVM 生效，**不需要重启服务**。

---

**推荐扩展资料**：

- [JetBrains 官方调试视频教程](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html)
- https://blog.jetbrains.com/idea/2025/04/debugging-java-code-in-intellij-idea/
