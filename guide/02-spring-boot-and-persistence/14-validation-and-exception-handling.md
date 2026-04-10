# 14 - 校验与全局异常处理：构建健壮的 REST API

## 核心心智映射 (Core Mental Mapping)

在 Node.js (Express/NestJS) 中，我们习惯于使用中间件或异常过滤器来统一处理错误和入参校验。Java Spring Boot 提供了声明式的方案，让你能以极低的代码量实现同样的功能。

| 场景 | Node.js (Joi / Zod / Nest) | Java (Bean Validation / Advice) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **入参校验** | `schema.parse(data)` | **Annotation (注解)** | 声明式校验，零侵入 |
| **校验拦截** | 手动调用或 Decorator | **`@Valid`** | 进入方法前自动拦截 |
| **全局拦截** | `app.use((err, req, res, next))` | **`@RestControllerAdvice`** | 业务逻辑外的“守护切面” |
| **错误捕获** | `if (err instanceof MyError)` | **`@ExceptionHandler`** | 精确匹配异常类 |
| **响应封装** | `res.json({ code, data })` | **`Result<T>` 封装类** | 统一全站响应格式 |

---

## 概念解释 (Conceptual Explanation)

### 1. Bean Validation (声明式校验)
不同于拼写繁琐的 `if` 语句，Java 使用注解直接定义在实体类（Entity/DTO）上。框架在收到请求后，会在进入业务逻辑前自动执行校验，发现问题直接“原地遣返”。

### 2. @RestControllerAdvice (全局增强)
这是一个基于 AOP（面向切面编程）的拦截器。它像是在所有 Controller 的方法外层套了一个巨大的 `try-catch` 块。
-   **解耦**: Controller 只需关注“快乐路径”，异常情况交给它统一处理。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 常用校验注解
-   **`@NotBlank`**: 字符串不为 null 且去空格后长度 > 0。
-   **`@NotNull`**: 对象不为 null。
-   **`@Size(min, max)`**: 字符串或集合的长度范围。
-   **`@Pattern`**: 正则表达式。

### 全局处理器结构
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }
}
```

---

## 典型用法 (Typical Usage)

### 1. 定义带校验的实体
```java
public class UserDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

### 2. 在 Controller 开启校验
```java
@PostMapping
public Result<User> create(@Valid @RequestBody UserDTO user) {
    // 如果校验失败，方法体根本不会被执行
    return Result.success(userService.save(user));
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 `GlobalExceptionHandler.java`:
它捕获了 `MethodArgumentNotValidException`（这是校验失败跑出的标准异常）。
-   它遍历了所有报错字段。
-   将报错信息拼接成对前端友好的字符串。
-   最后包裹在 `Result.error()` 中返回。
这保证了无论 API 是因为参数不对、业务逻辑冲突还是系统崩溃，前端收到的 JSON 结构始终是稳定的。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

手动写各种异常捕获方法是搬砖活。

> **最佳实践 Prompt**:
> "我需要为一个『订单支付』接口增加错误处理。
> 1. 请帮我定义一个 `OrderPaymentDTO`，包含订单号（非空）、支付金额（最小 0.01）和支付渠道（正则匹配）。
> 2. 请在 `GlobalExceptionHandler` 中增加一个处理 `InsufficientBalanceException` 的方法，返回 400 状态码及错误信息。
> 3. 请确保所有校验失败的消息能通过 JSON 数组的形式返回给前端，方便前端定位具体的字段。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Baeldung: Guide to Validation in Spring Boot](https://www.baeldung.com/spring-boot-bean-validation) - 校验框架全手册。
2. [Spring Docs: Exception Handling](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html) - 官方对全局处理器的深度解析。
3. [Hibernate Validator: Constraints Reference](https://docs.jboss.org/hibernate/validator/current/reference/en-US/html_single/#validator-defineconstraints-spec) - 所有可用校验注解速查。
