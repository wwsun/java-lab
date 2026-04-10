# 02 - 书籍管理系统：后端核心逻辑深度导读

恭喜你！我们已经完成了一个具备工业级雏形的 **书籍管理系统**。本指南将带你深度剖析该系统的核心架构与实现细节，帮助你从“能跑通”进阶到“理解设计意图”。

---

## 🏗️ 1. 工业级项目分层架构

在 `com.javalabs` 包下，我们遵循了经典的 **四层架构**（与 Node.js 中的 MVC+Service 模式高度对应）：

| 层级 | 职责 | Node.js 类比 |
| :--- | :--- | :--- |
| **Controller** | 暴露 RESTful 接口，负责请求路由、参数接收 | `routes/` + `controllers/` |
| **Service** | 核心业务逻辑，控制事务 (Transaction) | `services/` |
| **Mapper** | 数据库操作 (MyBatis-Plus)，执行 SQL | `models/` (Sequelize/Prisma) |
| **Entity** | 数据库表对应的 Java 对象 | `entities/` |
| **DTO/Request** | 统一响应格式 (`Result`) 与 入参校验对象 | `dto/` (Joi/Zod validation) |

---

## 🛡️ 2. 安全与请求生命周期 (The Middleware)

当一个客户端请求（如 `POST /api/books/1/borrow`）进入系统时，它会经历以下旅程：

### 第一关：JWT 拦截器 (`JwtInterceptor.java`)
- **作用**: 类似于 Express 的 `authMiddleware`。
- **逻辑**: 
  1. 从 `Authorization` Header 中提取 `Bearer <token>`。
  2. 使用 `JwtUtils` 校验合法性。
  3. 将解析出的 `username` 和 `role` 存入 `request.setAttribute`，实现无状态认证。
- **Node 视角**: `req.user = decodedPayload; next();`

### 第二关：权限校验 (`@PreAuthorize`)
- **位置**: `BookController.java` 中的方法上方。
- **逻辑**: 自动检查当前登录用户的角色。例如 `@PreAuthorize("hasRole('ADMIN')")` 确保只有管理员能添加或删除书籍。

---

## 🧠 3. 核心业务逻辑实现

### 🔑 认证实现 (`AuthServiceImpl.java`)
1. **加密**: 注册/初始化时使用 `BCrypt` 对明文密码加盐哈希（Java 版 `BCrypt.hashpw`）。
2. **校验**: 登录时，将数据库中的哈希值与用户输入的明文对比。
3. **颁发**: 校验通过后，生成包含用户身份信息的 JWT 字符串。

### 📚 借阅逻辑与事务管理 (`BorrowRecordServiceImpl.java`)
这是系统中最核心的业务。借阅一本书包含多个数据库操作：
1. **查库存**: 检查 `Book` 表，库存是否 > 0。
2. **扣库存**: `UPDATE books SET stock = stock - 1 WHERE id = ?`。
3. **增记录**: `INSERT INTO borrow_records ...`。

**关键点：`@Transactional`**
- 如果插入记录失败（如数据库断电），已扣减的库存必须**回滚**。
- Java Spring 的声明式事务极大地简化了代码，只需一个注解即可保证 **ACID 原子性**。

---

## ⚡ 4. MyBatis-Plus：极简 CRUD

你会发现 `BookMapper.java` 几乎是空的：
```java
public interface BookMapper extends BaseMapper<Book> {}
```
但我们却能在 `BookService` 中直接调用 `selectById`, `insert`, `updateById` 等方法。
- **优势**: 自动注入通用 CRUD，无需手写繁琐的 SQL。
- **进阶**: 配合 `LambdaQueryWrapper`，可以实现类型安全的复杂查询，避免字符串拼写错误。

---

## 🎨 5. API 规范化与异常处理

### 统一响应格式 (`Result.java`)
为了前端开发者的幸福，我们所有接口都返回相同的 JSON 结构：
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 171234567890
}
```

### 全局异常捕获 (`GlobalExceptionHandler.java`)
不再需要在每一个 Controller 里写 `try-catch`。
- **原理**: 使用 Spring AOP (面向切面) 拦截整个系统的异常。
- **处理**: 
  - `ResourceNotFoundException` -> 返回 404 JSON。
  - `MethodArgumentNotValidException` -> 返回参数校验失败信息。
  - `Exception` -> 返回通用的 500 “服务器开小差了”。

---

## 🛠️ 动手实验：验证逻辑

1. **测试事务**: 
   故意在 `borrowBook` 方法的最后一行抛出一个 `RuntimeException`，观察书籍库存是否真的没有减少（事务回滚）。
2. **测试校验**:
   尝试新增一本书，但不填标题，观察 `GlobalExceptionHandler` 如何返回详细的错误描述。
3. **性能监控**:
   查看 `PerformanceInterceptor.java`。它会记录每个请求的执行时间，并在控制台打印，这在生产环境排查慢接口时非常有用。

---

**下一阶段建议**：
通过本系统的学习，你已经掌握了 Spring Boot 开发的核心链路。接下来请阅读 [40-alibaba-p3c-coding-guidelines.md](40-alibaba-p3c-coding-guidelines.md)，学习如何编写符合阿里巴巴规约的“大厂标准”代码。
