# 07 - Stream API：像处理 JS 数组一样处理 Java 集合

## 核心心智映射 (Core Mental Mapping)

作为深谙 Node.js 开发的全栈程序员，`filter`, `map`, `reduce` 是处理数据的“三板斧”。Java 的 **Stream API** 完全对标这一心智模型，但更为严谨且高效。

| Node.js `Array.prototype` | Java `java.util.stream.Stream` | 核心差异 |
| :--- | :--- | :--- |
| `Array.filter(fn)` | `stream().filter(Predicate)` | Java 需要显式 Lambda 或方法引用 |
| `Array.map(fn)` | `stream().map(Function)` | Java 提供 `mapToInt` 等原始类型流优化性能 |
| `Array.reduce(fn, init)` | `stream().reduce(init, BinaryOp)` | Java 常用 `Collectors` 快速规约而非手写累加 |
| **执行时机** | **立即执行** (创建新数组) | **惰性执行** (仅在调用终结操作时才运行) |

---

## 概念解释 (Conceptual Explanation)

### 1. 惰性求值 (Lazy Evaluation)
在 JS 中，每个 `.map()` 都会生成一个新数组。在 Java 中，所有的中间操作（如 `filter`, `map`）都只是在“声明”流水线。只有当你调用了 **终端操作**（如 `toList()`, `sum()`, `count()`）时，整个流才会一次性地处理所有数据。这在处理大数据集时具有极高的性能优势。

### 2. 中间操作 vs 终端操作
- **中间操作**: 返回值仍然是一个 `Stream`，可以链式调用（如 `filter`, `distinct`, `sorted`）。
- **终端操作**: 返回一个非流的结果或副作用（如 `collect`, `forEach`, `reduce`）。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 获取流
```java
List<String> list = List.of("a", "b", "c");
list.stream(); // 从集合获取流
```

### 常用中间操作
- `filter(p -> p.startsWith("a"))`: 过滤。
- `map(String::toUpperCase)`: 转换（此处使用方法引用）。
- `distinct()`: 去重。

### 常用终端操作
- `toList()`: (Java 16+) 直接收集到列表。
- `collect(Collectors.toMap(...))`: 收集到 Map。
- `findFirst()`: 获取第一个元素，返回 `Optional`。

---

## 典型用法 (Typical Usage)

### 场景：筛选活跃用户并提取用户名

```java
List<String> activeUserNames = users.stream()
    .filter(User::isActive)
    .map(User::getName)
    .toList();
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察以下对比示例：使用 Java 的 **Record**（类比 TS 的 Type/Interface）配合 Stream。

```java
record Employee(String name, boolean isActive, int salary) {}

int totalSalary = employees.stream()
    .filter(Employee::isActive)           // 1. 过滤在职员工
    .mapToInt(Employee::salary)           // 2. 映射为数值流 (避免装箱损耗)
    .sum();                               // 3. 直接求和 (终端操作)
```

1.  **`Employee::isActive`**: 这是方法引用，等同于 `e -> e.isActive()`，更简洁。
2.  **`mapToInt`**: 如果处理的是数字，Java 强攻性能，提供了原始类型流，避免了 `Integer` 对象的创建。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Java 的 Stream 语法在处理嵌套或级联转换时非常强大，但也容易写得冗长。

> **最佳实践 Prompt**:
> "我有一组 `Order` 对象，每个订单包含多个 `OrderItem`。
> 请帮我用 Java 21 Stream API 实现：筛选出订单总额大于 100 的订单，并提取其中所有商品的唯一 ID 列表。
> 请尽量使用方法引用（Method Reference），并说明如何使用 `flatMap` 展平集合。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Baeldung: The Java 8 Stream API Tutorial](https://www.baeldung.com/java-8-streams) - Stream 的百科全书。
2. [Modern Java Recipes](https://www.oreilly.com/library/view/modern-java-recipes/9781491973165/) - 强烈推荐，书中关于流的组合操作非常有启发性。
