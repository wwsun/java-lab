# 11-Java 现代时间 API (java.time.*)

在 Java 8 之后，官方由 Joda-Time 库启发，重构并推出了一套全链条的时间处理 API。这套 API 最大的特点是：**不可变性 (Immutable)** 和 **线程安全**。

## 1. 核心心智映射 (Node.js vs Java)

| 功能场景 | JavaScript (Day.js习惯) | Java (java.time.*) |
| --- | --- | --- |
| 本地时间 (无时区) | `dayjs()` | **`LocalDateTime`** |
| 仅日期 | `dayjs().format('YYYY-MM-DD')` | **`LocalDate`** |
| 仅时间 | `dayjs().format('HH:mm:ss')` | **`LocalTime`** |
| 绝对时间/时间戳 | `Date.now()` / `new Date()` | **`Instant`** |
| 格式化器 | `"YYYY-MM-DD HH:mm:ss"` | **`DateTimeFormatter`** |
| 时间段 (天/月/年) | 依赖 `durations` 插件 | **`Period`** |
| 时间段 (秒/纳秒) | 依赖 `durations` 插件 | **`Duration`** |

## 2. 为什么不再使用 `java.util.Date`？

1.  **可变性 (Mutable)**：`Date` 可以随意 `setTime()`，这在并发环境下由于可能被不同线程修改而变得极其不安全。
2.  **月份索引坑**：老款 API 的月份是从 `0` 开始的（0=1月），而 `LocalDate` 是从 `1` 开始的。
3.  **时区混乱**：老款 API 在内部混淆了绝对时间与本地偏移，新的 `ZonedDateTime` 明确了时区规则。

## 3. 常见业务场景示例

### 日期格式化与解析 (Formatter)
```java
var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
// 解析
var dateTime = LocalDateTime.parse("2024-05-20 13:14:00", formatter);
// 输出
String formatted = dateTime.format(formatter);
```

### 绝对时间 (Instant) —— “真理之源”
在分布式系统中，`Instant` 是最重要的类。它代表 UTC 0 时区的绝对时间点。
*   **对应 Node.js**：`new Date().toISOString()` 或 `Date.now()`。
*   **最佳实践**：**存库用 `Instant`，展示用 `LocalDateTime`**。这样无论你的服务器在上海还是硅谷，时间逻辑永远不会出错。

```java
// 获取当前毫秒戳 (类比 JS: Date.now())
long timestamp = Instant.now().toEpochMilli();

// Instant 转本地时间 (需指定时区)
LocalDateTime local = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
```

### 时间计算 (Plus/Minus)
```java
var now = LocalDate.now();
// 一周后
var nextWeek = now.plusWeeks(1);
// 三个月前
var lastQuarter = now.minusMonths(3);
```

### 计算间隔 (ChronoUnit)
```java
long daysBetween = ChronoUnit.DAYS.between(d1, d2);
```

## 4. 最佳实践 (面向指挥 AI 的开发者)

1.  **首选 `LocalDateTime`**：用于表示在某特定地点的日期和时间（如“会议在周一 10:00举行”）。
2.  **日志与数据库记录选用 `Instant`**：这是 UTC 的绝对时间点，能够有效避免服务器迁移或时区切换带来的混乱。
3.  **拥抱不可变性**：在修改时间时，记住 `time.plusDays(1)` 返回的是**新对象**。

---
**参考资料**：
- [Baeldung: Java 8 Date and Time API Guide](https://www.baeldung.com/java-8-date-time-intro)
- [Oracle Docs: Date Time Package](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
