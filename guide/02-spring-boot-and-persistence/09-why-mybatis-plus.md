# 09 - 为什么选择 MyBatis-Plus？：Java 持久层的王者之选

## 核心心智映射 (Core Mental Mapping)

对于从 Node.js 环境转型的开发者来说，MyBatis-Plus (简称 MP) 是目前 Java 生态中平衡“开发效率”和“SQL 控制力”的最佳方案。

| 领域 | Node.js (Knex.js / Prisma) | Java (MyBatis-Plus) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **底层 SQL** | Knex (查询构建器) | MyBatis (XML 映射) | 对 SQL 的终极掌控 |
| **全自动 ORM** | Prisma / Sequelize | **MyBatis-Plus** | 零代码实现基础 CRUD |
| **条件构造** | `where: { id: 1 }` | **LambdaQueryWrapper** | 类型安全的链式查询 |
| **生命周期钩子** | Mongoose Timestamps | **自动填充 (Auto Fill)** | 自动维护审计字段 |
| **分页插件** | 手动计算 limit/offset | **分页拦截器** | 物理分页的自动化 |

---

## 概念解释 (Conceptual Explanation)

### 1. 只做增强，不做改变
MyBatis-Plus 是在 MyBatis 基础上的封装。如果你需要极其复杂的 SQL 优化，可以随时回退到 MyBatis 的 XML 模式；而对于 90% 的常规 CRUD，MP 让你一行代码搞定。

### 2. BaseMapper 与 IService
-   **BaseMapper<T>**: 属于持久层。只要继承它，你的 Mapper 就拥有了增删改查的全套秘籍。
-   **IService<T>**: 属于业务层方案。它封装了更高级的业务操作（如批量保存、链式查询）。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 重要注解
-   **`@TableName("users")`**: 定义实体对应哪张表。
-   **`@TableId(type = IdType.AUTO)`**: 定义主键策略（自增、雪花算法等）。
-   **`@TableField(fill = FieldFill.INSERT)`**: 定义自动填充时机。

### 类型安全的 Lambda 查询
```java
// 对标 JS: prisma.user.findMany({ where: { status: 'ACTIVE' } })
userMapper.selectList(new LambdaQueryWrapper<User>()
    .eq(User::getStatus, "ACTIVE")
    .orderByDesc(User::getCreatedAt));
```

---

## 典型用法 (Typical Usage)

### 标准开发“三步走”
1.  **Entity**: 使用 Lombok 和 MP 注解描述表结构。
2.  **Mapper**: 继承 `BaseMapper<User>`。
3.  **Service**: 接口继承 `IService<User>`，实现类继承 `ServiceImpl<UserMapper, User>`。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **基础 CRUD 演示**:
由于 `EmployeeMapper` 继承了 `BaseMapper<Employee>`，你在 `EmployeeController` 中无需编写任何 SQL 或 XML，就能直接调用 `employeeService.list()` 或 `employeeService.save(employee)`。MP 会在运行时根据实体类的注解自动“脑补”出对应的 SQL 语句并执行。这种设计让 Java 项目的开发速度直追 Node.js。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

MyBatis-Plus 的样板代码非常规律，是 AI 生成的最佳温床。

> **最佳实践 Prompt**:
> "我需要为一个『图书借阅记录』功能生成持久层代码。
> 1. 表名为 `borrow_records`，包含 `user_id`, `book_id`, `borrow_date`, `return_date`。
> 2. 请生成对应的 Java Entity（使用 Lombok 和 `@TableName`）。
> 3. 请生成对应的 Mapper 接口（继承 `BaseMapper`）。
> 4. 请生成 Service 接口和实现类，并包含一个方法：根据 `user_id` 查询所有未归还的记录（`return_date` 为空）。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MyBatis-Plus 官方文档](https://baomidou.com/) - 建议重点看“脚本化配置”和“条件构造器”章节。
2. [Baeldung: Guide to MyBatis](https://www.baeldung.com/mybatis) - 了解 MP 底层的运行基石。
3. [Spring Data JPA vs MyBatis-Plus](https://juejin.cn/post/6844903960012550157) - 深度对比两者的设计哲学差异。
