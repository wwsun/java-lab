# 14-Java Lambda 核心：从对象到逻辑的终极简化

在 Java 8 之前，Java 被嘲讽为“啰嗦的语言”。如果你想给一个方法传递一段逻辑（比如：如何过滤数据），你必须创建一个**对象**。Lambda 的出现，让 Java 终于可以像 Node.js 一样直接传递**逻辑**。

## 1. 什么是 Lambda？ (起源)

想象一下，你要在[LambdaDemo.java](../src/main/java/com/javalabs/LambdaDemo.java)中执行一个任务。

**史前时代 (匿名内部类)**：
```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
```
你被迫写了 5 行代码，仅仅为了表达中间那 1 行打印逻辑。

**现代 Lambda 时代**：
```java
Runnable r = () -> System.out.println("Hello");
```
这不仅是语法糖，更是一种**心智模型**的转变：我们将逻辑视为“一等公民”。

## 2. 语法 1:1 拆解图

Lambda 的结构极其固定：`(参数) -> { 逻辑 }`

| 变体 | 示例 | 说明 |
| :--- | :--- | :--- |
| **无参单行** | `() -> System.out.println("Go!")` | 最简单形式，自动 return |
| **单参简洁版** | `row -> row.length()` | 无需括号，无需 return 关键字 |
| **多参数形式** | `(a, b) -> a + b` | 适合计算逻辑 |
| **多行复杂版** | `(x) -> { log.info(x); return x * 2; }` | 必须带大括号和显式 return |

## 3. 函数式接口：Lambda 的“寄生载体”

Java 不允许函数脱离类独立存在。Lambda 必须匹配一个 **“函数式接口”**（即：只有一个抽象方法的接口）。

- **`@FunctionalInterface`**：这不是强制的，但它像是一个合同，保证该接口永远只能有一个方法，从而支持 Lambda。

### 常见的“四大天王”接口 (必背)
我们在 `LambdaDemo.functionalInterfaces()` 中演示了它们：
1.  **`Predicate<T>`** (断言)：`(t) -> boolean`。用于 `filter` 过滤。
2.  **`Consumer<T>`** (消费者)：`(t) -> void`。用于 `forEach` 处理。
3.  **`Function<T, R>`** (函数)：`(t) -> R`。用于 `map` 转换。
4.  **`Supplier<T>`** (提供者)：`() -> t`。用于工厂模式。

## 4. 深度剖析：Effectively Final (内存模型陷阱)

这是 Node.js 开发者最困惑的地方。为什么不能修改外部变量？

```java
int count = 0;
Runnable r = () -> count++; // ❌ 报错
```

**底层真相**：
Java 的 Lambda 是通过“值捕获”实现的。为了解决多线程并发下数据的一致性问题，Java 强制要求捕获的局部变量必须是 **Fact (事实上) 不可变**的。如果你在外面改了变量，Lambda 里的那份副本就“失效”了。

**解法**：在 `LambdaDemo.bypassFinalLimit()` 中，我们使用了 **数组包装**。这是因为虽然数组内容变了，但数组的引用（地址）依然是 `final` 的。

## 5. 方法引用：极致的化繁为简

当你发现 Lambda 只是在“转发参数”给另一个方法时，可以使用 `::`。

- `s -> System.out.println(s)` ➡️ `System.out::println`
- `s -> s.length()` ➡️ `String::length`

---
> [!IMPORTANT]
> **实践小贴士：** 不要为了追求极致缩减而牺牲可读性。如果逻辑超过 3 行，建议通过 `extract method` 将其提取为普通方法，然后使用方法引用调用。

**参考资料**：
- [Java 8 Lambda Expressions (Oracle)](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)
- [Baeldung: Guide to Java 8 Lambda Expressions](https://www.baeldung.com/java-8-lambda-expressions-tips)
