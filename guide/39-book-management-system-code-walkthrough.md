# 39-书籍管理系统：后端核心逻辑深度导读

恭喜你！我们已经完成了一个具备工业级雏形的 **书籍管理系统**。为了帮你理清这些文件之间的血缘关系，请按照本指南推荐的顺序进行阅读。

---

## 🧭 阅读建议：由内而外

就像剥洋葱一样，Java 工程的逻辑是由“数据库模型”向“外部接口”一层层包裹的。

### 第一步：数据模型 (The Schema)
理解系统处理什么样的数据。
1. **[schema.sql](../src/main/resources/schema.sql)**: 定义了 `categories` (分类), `books` (书籍), `borrow_records` (借阅) 及增强后的 `users`。
2. **[Book.java](../src/main/java/com/javalabs/entity/Book.java)**: 注意 MyBatis-Plus 的注解应用。

### 第二步：核心业务 (The Heart)
理解业务逻辑的核心——**事务**与**认证**。
1. **[AuthServiceImpl.java](../src/main/java/com/javalabs/service/impl/AuthServiceImpl.java)**
   - **关键逻辑**: 校验 BCrypt 密码 -> 调用 `JwtUtils` 签发 Token。
   - **Node.js 类比**: 类似于登录接口中调用 `bcrypt.compare` 和 `jwt.sign`。
2. **[BorrowRecordServiceImpl.java](../src/main/java/com/javalabs/service/impl/BorrowRecordServiceImpl.java)**
   - **核心点**: `@Transactional` 注解。
   - **逻辑**: 如果库存足够，先扣减书籍库存，再插入借阅记录。如果其中一步失败，整个操作回滚（ACID 特性）。

### 第三步：安全关卡 (The Middleware)
理解流量进入 Controller 之前是如何被过滤的。
1. **[JwtInterceptor.java](../src/main/java/com/javalabs/interceptor/JwtInterceptor.java)**
   - **角色**: 这就是你的 **Auth Middleware**。它解析 `Authorization: Bearer <token>` 并将用户信息存入 `request` 属性中。
2. **[WebMvcConfig.java](../src/main/java/com/javalabs/config/WebMvcConfig.java)**
   - **配置**: 设定哪些路径需要校验（如 `/api/**`），哪些不需要（如 `/api/auth/login`）。

### 第四步：对外接口 (The Controller)
1. **[AuthController.java](../src/main/java/com/javalabs/controller/AuthController.java)**: 登录入口点。
2. **[BookController.java](../src/main/java/com/javalabs/controller/BookController.java)**: 业务入口。注意它如何通过 `request.getAttribute("role")` 实现简易的人生权限校验。

---

## 🛠️ 运行与验证

在完成阅读后，你可以尝试以下操作：

1. **编译运行**:
   ```bash
   mvn spring-boot:run
   ```
2. **测试登录**:
   使用 `data.sql` 中预置的账号：`admin` / `password123`。
3. **查看测试**:
   [JwtUtilsTest.java](../src/test/java/com/javalabs/util/JwtUtilsTest.java) 演示了如何在代码中生成和验证一个 Token。

---

## 💡 转型思考：Node.js vs Java
- **无状态**: 全靠 JWT，服务器不存 Session（符合现代云原生设计）。
- **同步阻塞**: 与 Node.js 的事件循环不同，这里的每一行代码都是按序执行的，直到数据库返回结果。
- **类型安全**: 每一个从数据库出来的字段都有确定的类型，极大减少了运行时错误。

---

**下一阶段建议**：
阅读 [40-alibaba-p3c-coding-guidelines.md](40-alibaba-p3c-coding-guidelines.md) 了解如何将代码提升到工业级标准。
