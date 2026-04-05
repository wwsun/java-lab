# 34-ORM 的性能噩梦：N+1 查询问题及其解决方案

对于从 Node.js/TypeScript 转型的开发者，N+1 问题并不陌生。在 **Prisma** 中，如果你忘了写 `include: { tasks: true }`；在 **TypeORM** 中，如果你忘了在查询时加上 `{ relations: ["tasks"] }`，系统就会陷入循环查询的泥潭。

在 Java 的 MyBatis-Plus 中，由于它默认不提供全自动的关联映射，理解 N+1 的成因及解决方法更为关键。

---

## 1. 什么是 N+1 问题？

### 场景设定
假设我们有 2 个用户：`Sun` 和 `Moon`。
- `Sun` 有 3 个任务。
- `Moon` 有 2 个任务。

如果我们想查出所有用户及其任务，**错误的逻辑**（N+1）是：
1. **1 次查询**：`SELECT * FROM users` -> 得到 [Sun, Moon]。
2. **N 次查询**：遍历结果集，为每个用户执行一次：
   - 为 Sun 查任务：`SELECT * FROM tasks WHERE user_id = 1`
   - 为 Moon 查任务：`SELECT * FROM tasks WHERE user_id = 2`

**结论**：如果数据库有 1000 个用户，这段代码会发射 **1001 条 SQL**，极大地拖慢了数据库响应！

---

## 2. MyBatis-Plus 的解决方案：联表查询 (Join)

在 Java 生态中，解决一对多关联最稳妥的方式是使用 **XML Mapper** 配合 **ResultMap**。

### 核心步骤

1. **实体类增强**：在 `User` 类中增加 `List<Task> tasks` 字段，并标注 `@TableField(exist = false)`。
2. **定义 ResultMap**：在 [UserMapper.xml](../src/main/resources/mapper/UserMapper.xml) 中定义映射规则，使用 `<collection>` 标签告诉 MyBatis 如何组装任务列表。
3. **编写 SQL**：使用 `LEFT JOIN` 一次性查出所有数据。

```sql
SELECT u.*, t.id AS task_id, t.title ... 
FROM users u 
LEFT JOIN tasks t ON u.id = t.user_id
```

---

## 3. 实战对比

打开 [NPlusOneDemoTest.java](../src/test/java/com/javalabs/mapper/NPlusOneDemoTest.java)，您可以观察两个测试场景：

- **场景 A**：模拟了错误的循环查询。运行后，在控制台的日志中，您会看到密密麻麻的 `Preparing: SELECT ... FROM tasks` 日志。
- **场景 B**：使用了我们定义的联表查询。无论用户有多少，控制台只会打印 **1 条 SQL**。

---

## 4. Node.js 概念映射

| 概念 | Node.js (Prisma/TypeORM) | Java (MyBatis-Plus) |
| :--- | :--- | :--- |
| **关联查询** | `include` 或 `relations` | `ResultMap` + `collection` |
| **延迟加载** | 默认支持 (Lazy Loading) | 需在 XML 配置 `fetchType="lazy"` |
| **立即加载** | Eager Loading | 联表查询 (Join) |

---

### 如何运行测试？

```bash
mvn test -Dtest=NPlusOneDemoTest
```

**观察重点**：
1. `reproduceNPlusOneProblem` 的 SQL 密集度。
2. `solveByJointQuery` 的 SQL 只有 1 条，且通过 `id` 进行数据分包。

---

### 📚 扩展阅读
1. [MyBatis-Plus 联表查询的最佳实践](https://baomidou.com/pages/62ebcc/)
2. [高性能 SQL：子查询 vs JOIN 的取舍](https://blog.codinghorror.com/sql-join-v-subquery-its-not-that-obvious/)
