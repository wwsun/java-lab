# 14 - Java Lambda 核心：从对象到逻辑的终极简化

## 核心心智映射 (Core Mental Mapping)

在 Java 8 之前，Java 因为啰嗦而备受嘲讽。如果你想传递一段逻辑（比如如何过滤数据），你必须创建一个**对象**。Lambda 的出现，让 Java 终于可以像 Node.js 一样直接传递**逻辑**。

| 领域 | Node.js / TypeScript | Java Lambda | 心智映射 |
| :--- | :--- | :--- | :--- |
| **匿名函数** | `(x) => x * 2` | `x -> x * 2` | 核心语法几乎相同 |
| **函数地位** | “一等公民” | “函数式接口”的实例 | Java 仍需通过接口载体实现 |
| **闭包限制** | 随意修改外部变量 | **Effectively Final** | Java 强制要求捕获变量不可变 |
| **语法糖** | (无直接对标) | **方法引用 (::)** | 极致的参数自动转发 |

---

## 概念解释 (Conceptual Explanation)

### 1. 函数式接口 (Functional Interface)
Java 不允许函数脱离类独立存在。Lambda 必须匹配一个“只有一个抽象方法”的接口。
> **Tip**: 加上 `@FunctionalInterface` 注解可以强制编译器帮你校验。

### 2. Effectively Final (事实不可变)
这是 Node.js 开发者最困惑的地方。如果你在 Lambda 里引用了外部的局部变量，你不能修改它。
*   **原因**: Java 的 Lambda 是通过“值捕获”实现的。为了保证线程安全和数据一致性，Java 强制要求捕获的变量必须是事实不可变的。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心语法
-   **无参**: `() -> System.out.println("Go")`
-   **单参**: `x -> x * x` (无需括号)
-   **多参**: `(a, b) -> a + b`
-   **多行**: `(x) -> { log.info(x); return x; }` (需大括号和 return)

### 常用“四大天王”接口
1.  **`Predicate<T>`**: `(t) -> boolean`。用于 `filter` 过滤。
2.  **`Consumer<T>`**: `(t) -> void`。用于 `forEach` 处理。
3.  **`Function<T, R>`**: `(t) -> R`。用于 `map` 转换。
4.  **`Supplier<T>`**: `() -> t`。用于工厂模式。

---

## 典型用法 (Typical Usage)

### 1. 集合遍历
```java
list.forEach(item -> System.out.println(item));
```

### 2. 方法引用 (极致简化)
如果你发现 Lambda 只是在“转发参数”，直接用 `::`：
-   `s -> System.out.println(s)` ➡️ `System.out::println`
-   `s -> s.length()` ➡️ `String::length`

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 Lambda 如何取代传统的**匿名内部类**：
```java
// 旧时代：啰嗦的对象
new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Running");
    }
}).start();

// Lambda 时代：纯粹的逻辑
new Thread(() -> System.out.println("Running")).start();
```
你会发现，所有的样板代码（`new Runnable`, `@Override`, `void run`）都在 Lambda 简洁的 `() ->` 语法中被隐式推断了。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当你在阅读臃肿的老项目代码时：

> **最佳实践 Prompt**:
> "我正在阅读一段 Java 8 之前的旧代码，里面充满了匿名内部类。`[贴入代码]`。
> 1. 请帮我识别哪些地方可以用 Lambda 表达式重构。
> 2. 请指出哪些地方可以使用性能更高的『方法引用』。
> 3. 请检查是否存在 Effectively Final 冲突，并建议修复方案。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Oracle: Lambda Expressions Tutorial](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) - 官方对 Lambda 设计的深度解析。
2. [Baeldung: Lambda Tips and Best Practices](https://www.baeldung.com/java-8-lambda-expressions-tips) - 避坑进阶。
