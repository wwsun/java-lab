# 13 - Java 现代时间 API

## 核心心智映射 (Core Mental Mapping)

如果你习惯了 JavaScript 中的 `Date` 对象或 `dayjs` / `moment` 库，你会发现 Java 8 之后推出的 `java.time` 包非常亲切。它解决了老旧 API 的诸多痛点，核心设计哲学是：**不可变性 (Immutable)** 和 **线程安全**。

| 功能场景         | Node.js (Day.js)       | Java (java.time.\*)       | 核心特性                   |
| :--------------- | :--------------------- | :------------------------ | :------------------------- |
| **本地日期时间** | `dayjs()`              | `LocalDateTime`           | 无时区概念，最常用         |
| **仅日期/时间**  | `format('YYYY-MM-DD')` | `LocalDate` / `LocalTime` | 精确模型，防止脏数据       |
| **绝对时间戳**   | `Date.now()`           | **`Instant`**             | 机器时间，UTC 0 时区       |
| **格式化器**     | 格式化字符串           | `DateTimeFormatter`       | 线程安全，预置多种标准格式 |
| **时间长度**     | `duration` 插件        | `Duration` / `Period`     | 处理秒级/日级间隔          |

---

## 概念解释 (Conceptual Explanation)

### 1. 不可变性 (Immutability)

与 JS 的 `new Date()` 不同，Java 现代时间 API 的所有修改操作都会返回一个**新对象**。这避免了在多线程环境下时间被意外修改的风险。

### 2. 人类时间 (Human Time) vs 机器时间 (Machine Time)

- **人类时间 (`LocalDateTime`)**: 对应现实中的挂钟时间，受时区影响。
- **机器时间 (`Instant`)**: 对应从 Unix 纪元开始的纳秒数。它是全球统一的“真理之源”，建议在数据库中作为审计字段（如 `created_at`）首选。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心类库

- `LocalDate.now()`: 获取当前日期（2024-05-20）。
- `LocalDateTime.now()`: 获取当前日期时间（2024-05-20T13:14:00）。
- `Instant.now()`: 获取当前 UTC 时间戳。
- `Duration.between(start, end)`: 计算两个时间点的间隔。

---

## 典型用法 (Typical Usage)

### 1. 格式化与解析

```java
var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
// 解析
var dateTime = LocalDateTime.parse("2024-05-20 13:14:00", formatter);
// 输出
String formatted = dateTime.format(formatter);
```

### 2. 时间计算

```java
var now = LocalDate.now();
var nextWeek = now.plusWeeks(1); // 返回新对象
var lastYear = now.minusYears(1);
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `Instant` 转本地时间：

```java
LocalDateTime local = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
```

在分布式系统中，服务器可能分布在不同时区。最佳实践是：**内部存储与传输全部使用 `Instant` (UTC)，仅在展示给用户时，通过用户的 `ZoneId` 转为 `LocalDateTime`**。这能彻底解决夏令时和跨时区计算的 Bug。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Java 的时间计算非常严谨，尤其是涉及“每个月的第三个周五”这种复杂需求时：

> **最佳实践 Prompt**:
> "我需要计算从今天起，下一个季度的第一个工作日是哪一天。
>
> 1. 请使用 `java.time` API。
> 2. 请利用 `TemporalAdjusters` 来实现复杂的日期偏移逻辑。
> 3. 请说明如何跳过周末。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Baeldung: Java 8 Date and Time API Guide](https://www.baeldung.com/java-8-date-time-intro) - 包含大量实用案例。
2. [Oracle Docs: Date Time Package](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) - 官方 API 手册。
