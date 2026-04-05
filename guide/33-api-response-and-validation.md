# Java Web 进阶：统一响应封装与 Bean Validation

在完成了 MyBatis-Plus 的基础 CRUD 后，我们进入了"工程化"的关键阶段。在 Node.js 中，我们习惯于通过中间件或统一的 JSON 格式返回数据。在 Java 中，我们通过**泛型包装类**、**全局异常处理 (AOP)** 和 **Bean Validation** 来实现同样的工程标准。

## 1. 核心概念映射：从 Node.js 到 Java

| 概念 | Node.js (Express/NestJS) | Java (Spring Boot) |
| :--- | :--- | :--- |
| **响应封装** | `res.json({ code: 200, data, msg: 'ok' })` | `Result<T>` 泛型类 |
| **入参校验** | `Joi`, `Zod`, `class-validator` | `Bean Validation` (Hibernate Validator) |
| **全局错误捕获** | `app.use((err, req, res, next) => ...)` | `@RestControllerAdvice` + `@ExceptionHandler` |
| **校验触发** | 手动调用 `schema.validate()` 或 Decorator | 控制器方法参数前的 `@Valid` 或 `@Validated` |

---

## 2. 设计思路详解

### 2.1 统一响应对象 `Result<T>`
为了让前端开发者调用 API 时有统一的心智模型，我们定义了 `com.javalabs.model.Result<T>`。
- **泛型 `<T>`**：类比 TS 中的 `Result<T>`，确保 `data` 字段在不同接口下具有正确的类型提示。
- **状态码 (code)**：对标 HTTP 状态码或自定义业务码。
- **静态工厂方法**：提供 `success()` 和 `error()`，通过链式调用快速构建响应。

### 2.2 声明式校验 Bean Validation
不同于 Node.js 中繁琐的 `if (!username) return 400`，Java 使用**注解**进行声明式校验。
- **常用注解**：`@NotBlank` (非空且非空白), `@Email` (合法的邮件格式), `@Size` (长度限制), `@NotNull` (非 null)。
- **拦截时机**：Spring 在参数解析阶段发现违规即抛出 `MethodArgumentNotValidException`。

### 2.3 全局异常处理 (横切关注点)
利用 `@RestControllerAdvice`，我们将分散在各处的 `try-catch` 逻辑抽离到 `GlobalExceptionHandler`。
- **解耦**：Controller 逻辑只需关注"快乐路径"，异常情况交给全局处理器。
- **规范**：确保所有异常（包括校验失败）都以统一的 `Result` 格式返回，而不是 Spring 默认的白页错误。

---

## 3. 手把手运行指引

本项目已集成了上述所有功能，你可以通过以下步骤体验：

### 3.1 运行测试用例 (推荐)
我们为 `UserController` 和 `TaskController` 编写了详细的测试，覆盖了合法与非法数据的场景。
```bash
# 运行用户校验测试
mvn test -Dtest=UserControllerTest

# 运行任务校验测试
mvn test -Dtest=TaskControllerTest
```

### 3.2 关键代码路径
- **Result 封装**：`src/main/java/com/javalabs/model/Result.java`
- **异常处理器**：`src/main/java/com/javalabs/exception/GlobalExceptionHandler.java`
- **校验实体**：查看 `src/main/java/com/javalabs/entity/User.java` 中的注解
- **触发点**：查看 `src/main/java/com/javalabs/controller/UserController.java` 中的 `@Valid`

### 3.3 动手验证建议
尝试修改 `UserControllerTest` 中的数据，例如将邮箱改为合法格式，观察测试结果的变化。

---

## 4. 扩展阅读
1. [Baeldung: Spring Boot Bean Validation](https://www.baeldung.com/spring-boot-bean-validation) - 深入了解更多校验注解的使用。
2. [Spring 官方文档：Error Handling](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.error-handling) - 官方对异常处理的最佳实践。
3. [MyBatis-Plus 官网](https://baomidou.com/) - 了解更多 Mapper 接口的自动代码生成技巧。
