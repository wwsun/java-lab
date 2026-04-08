# 07 - IntelliJ IDEA 高效生产力技巧 (Productivity Tips)

## 核心心智映射 (Core Mental Mapping)

如果你习惯了 VS Code 的 Snippets，你会发现 IDEA 的代码补全不仅是“查表”，而是带有“语境理解”的。它由两种核心机制组成：

| 机制 | 触发方式 | Node.js 对标 | 核心体验 |
| :--- | :--- | :--- | :--- |
| **Live Templates** | **前缀触发** (`sout`) | User Snippets | 输入简码后 Tab，生成代码块 |
| **Postfix Completion** | **后缀触发** (`.for`) | (部分插件支持) | 在表达式后直接点出逻辑，顺滑无比 |
| **Code Generation** | `Cmd + N` | 自动修复/脚手架 | 快速生成构造函数、Getter/Setter |
| **万能重构** | `Shift + F6` | Rename Symbol | 深度链接，修改一处，全局全类型安全修改 |

---

## 概念解释 (Conceptual Explanation)

### 1. Live Templates (实时模板)
这是一种基于关键字缩写的模板机制。例如输入 `main` 并按 `Tab`，它会立即展开为完整的 `public static void main` 函数。

### 2. Postfix Completion (后缀补全)
这是 IDEA 的“降维打击”功能。它允许你先写完表达式，再根据表达式的类型追加逻辑。例如 `list.for` 会根据 `list` 的类型自动生成对应的 `for-each` 循环。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 🚀 高频 Live Templates (输入后按 Tab)
-   **`psvm` / `main`**: 生成主函数入口。
-   **`sout`**: 生成 `System.out.println();`。
-   **`soutv`**: 快速打印变量值（自动携带变量名）。
-   **`ifn` / `inn`**: 生成 `if (obj == null)` / `if (obj != null)`。
-   **`fori`**: 经典的 `for` 循环。

### 🪄 惊艳的 Postfix (在表达式后输入)
-   `"hello".sout`: 打印。
-   `new Object().var`: **最推荐**。自动补全左侧的变量类型声明和变量名。
-   `list.for`: 生成增强 `for` 循环。
-   `condition.if`: 生成 `if` 块。

---

## 典型用法 (Typical Usage)

### 快速声明变量流程
在 Java 中手动写 `UserResponseDTO userResponseDTO = new UserResponseDTO();` 非常痛苦。
1.  输入 `new UserResponseDTO()`。
2.  输入 `.var` 并回车。
3.  IDEA 会自动推断左侧类型并起好名字，你只需确认即可。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `soutv` 的魔力：
如果你写了 `int userAge = 25;`，在下一行输入 `soutv`：
```java
System.out.println("userAge = " + userAge);
```
它不仅帮你省去了敲打印语句的时间，还贴心地帮你做了字符串拼接，非常适合快速 Debug。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当你在阅读一坨难以维护的代码，想要重构却怕改坏时：

> **最佳实践 Prompt**:
> "我正在使用 IntelliJ IDEA。我想重构这段代码：`[贴入代码]`。
> 1. 请指出哪些复杂的逻辑可以提取为独立的方法（Extract Method）。
> 2. 哪些硬编码的字符串应该提取为常量（Extract Constant）。
> 3. 请说明在 IntelliJ 中执行这些重构操作的快捷键，以及如何确保重构后所有调用点同步更新。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [JetBrains: Live Templates](https://www.jetbrains.com/help/idea/using-live-templates.html) - 让你的打字速度翻倍。
2. [Postfix Completion Examples](https://www.jetbrains.com/help/idea/postfix-completion.html) - 体验“点”出来的编程魅力。
