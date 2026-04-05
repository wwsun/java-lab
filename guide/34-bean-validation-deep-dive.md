# Java 校验框架集成：Bean Validation (Hibernate Validator)

在 Node.js 中，我们常用 `Joi` 或 `Zod` 来定义 Schema 并校验请求体。在 Java 生态中，**Bean Validation (JSR 380)** 是事实上的标准，其实际实现通常是 **Hibernate Validator**。

## 1. 核心映射：声明式校验 vs 命令式校验

| 特性 | Node.js (命令式/半声明式) | Java (完全声明式) |
| :--- | :--- | :--- |
| **定义方式** | 定义 Schema 对象 (如 `z.object({ ... })`) | 在实体类属性上添加 **Annotation (注解)** |
| **校验逻辑** | `schema.parse(data)` | 框架根据注解自动执行校验 |
| **拦截位置** | 手动在 Controller 中调用校验 | 在 Controller 方法参数前加 `@Valid` |

## 2. 常用注解速查表

| 注解 | 说明 | 对应 Node.js 校验 |
| :--- | :--- | :--- |
| **`@NotBlank`** | 字符串不为 null 且去空格后长度 > 0 | `.string().nonempty().trim()` |
| **`@NotNull`** | 对象不为 null (常用于 Long, Integer) | `.notNull()` |
| **`@Email`** | 必须是合法的邮箱格式 | `.email()` |
| **`@Size(min, max)`** | 字符串或集合的长度/大小范围 | `.min(3).max(50)` |
| **`@Min` / `@Max`** | 数字的最小值/最大值 | `.min(18)` |
| **`@Pattern`** | 正则表达式匹配 | `.regex(/.../)` |

## 3. 运行指引：如何在本项目中验证校验

### 3.1 实体类配置
查看 `src/main/java/com/javalabs/entity/User.java`：
```java
@NotBlank(message = "用户名不能为空")
@Size(min = 3, max = 50, message = "用户名长度必须在 3 到 50 之间")
private String username;
```

### 3.2 控制器触发
查看 `src/main/java/com/javalabs/controller/UserController.java`：
```java
public Result<User> create(@Valid @RequestBody User user) {
    // 只有校验通过，才会进入方法体
    return Result.success(userService.createUser(user));
}
```

### 3.3 异常捕获
查看 `src/main/java/com/javalabs/exception/GlobalExceptionHandler.java`。当校验失败时，Spring 会抛出 `MethodArgumentNotValidException`，我们捕获它并返回友好的错误提示：
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
    // 提取并拼接所有字段的错误消息
}
```

## 4. 实践建议
1. **不要在 Service 层做基础校验**：参数格式、非空等校验应挡在 Controller 层，Service 层只负责业务逻辑校验（如：用户名是否已存在）。
2. **消息国际化 (Optional)**：`message` 属性可以指向配置文件，实现多语言错误提示。

---
**扩展阅读：**
- [Hibernate Validator 官方约束列表](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#validator-defineconstraints-spec)
- [Baeldung: Validation in Spring Boot](https://www.baeldung.com/spring-boot-bean-validation)
