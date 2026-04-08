# 10 - Java 异常处理： Checked vs Unchecked Exception

## 核心心智映射 (Core Mental Mapping)

在 Node.js 中，异常处理相对简单，通常只有一种 `Error` 类型。Java 则将异常视为一门严谨的“契约艺术”，强制你思考：“如果这里出错了，系统该怎么办？”

| 领域 | Node.js / TypeScript | Java Exception | 心智映射 |
| :--- | :--- | :--- | :--- |
| **运行时错** | `ReferenceError` / `TypeError` | `RuntimeException` | 逻辑 Bug，编译器不强制你处理 |
| **外部环境错** | `fs.readFile` 回调中的 `err` | **`Checked Exception`** | **Java 特有**。环境不可控（如文件缺失），编译器强制你处理 |
| **致命错误** | `process.exit(1)` | `Error` | 灾难性问题（如内存溢出），不建议捕获 |
| **资源释放** | `finally { fd.close() }` | **Try-with-resources** | 自动化的资源清理柜台 |

---

## 概念解释 (Conceptual Explanation)

### 1. 异常金字塔
-   **Throwable**: 所有异常的祖先。
    -   **Error**: 严重的系统级问题（如 `OOM`），通常应用层无法处理。
    -   **Exception**: 序可处理的问题。
        -   **Checked Exception**: 受检异常。方法必须显式声明或捕获它，否则代码过不了编译。
        -   **RuntimeException (Unchecked)**: 程序员的逻辑错误（如空指针 `NPE`）。

### 2. 自动资源管理 (Try-with-resources)
Java 7 引入的语法糖。只要一个类实现了 `AutoCloseable` 接口（如文件流、数据库连接），你在 `try(...)` 中声明它，Java 会在结束时自动调用 `close()`，告别繁琐的 `finally`。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 基础语法
```java
public void processFile() throws IOException { // 1. 声明可能抛出的受检异常
    try {
        // 业务逻辑
    } catch (IOException e) { // 2. 针对性捕获
        log.error("读取失败: ", e);
        throw new BusinessException("文件操作失败"); // 3. 转化为业务异常抛出
    }
}
```

### 常用 RuntimeException
- `NullPointerException`: 尝试访问 null。
- `IllegalArgumentException`: 方法入参不合法。
- `ArithmeticException`: 数学错误（如除以 0）。

---

## 典型用法 (Typical Usage)

### 1. Try-with-resources 范例
```java
try (var stream = new FileInputStream("config.properties")) {
    // 逻辑处理
} catch (IOException e) {
    // 异常处理，stream 会被自动关闭
}
```

### 2. 定义业务异常 (Business Exception)
```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("用户 ID 为 " + id + " 的记录不存在");
    }
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察“早抛出，晚捕获”原则：
在 Service 层，如果我们发现用户不存在，应该立即使用 `throw new UserNotFoundException(id)`。不要在 Service 层写 `try-catch`。

**为什么？**
因为异常应该一直传递到 **Controller** 或 **全局异常处理器**。只有在最外层，我们才知道该给前端返回 `404` 还是特定的 JSON 错误码。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Java 的受检异常往往会让代码显得臃肿。

> **最佳实践 Prompt**:
> "我正在写一个文件处理逻辑，Java 强制我捕获 `IOException`。
> 1. 请帮我定义一个统一的 `AppException` (Unchecked) 来包裹这类底层异常。
> 2. 请展示如何使用 Try-with-resources 优雅地重写这段传统的 `finally` 资源关闭代码：`[贴入代码]`。
> 3. 请说明如何在 Spring Boot 中通过 `@RestControllerAdvice` 捕获这些自定义异常并返回规范的 JSON。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Baeldung: Exception Handling in Java](https://www.baeldung.com/java-exceptions) - 涵盖了从基础到最佳实践的所有内容。
2. [Oracle: Exceptions Tutorial](https://docs.oracle.com/javase/tutorial/essential/exceptions/index.html) - 官方视角理解设计哲学。
