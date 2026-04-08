# 31 - ORM 的性能噩梦：N+1 查询问题及其解决方案

## 核心心智映射 (Core Mental Mapping)

如果你有 Node.js (Prisma/TypeORM) 开发经验，对 N+1 问题一定不陌生。在 Java 的 MyBatis 环境中，我们需要更显式地处理这种性能泥潭。

| 领域 | Node.js (Prisma / TypeORM) | Java (MyBatis-Plus) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **关联查询** | `include: { tasks: true }` | **ResultMap + <collection>** | 定义两张表的血缘关系 |
| **查询模式** | 默认支持延迟加载 (Lazy) | 需在 XML 中显式配置 | 预取策略的权衡 |
| **解决手段** | Eager Loading | **联表查询 (JOIN)** | 减少数据库连接开销 |
| **后果** | 数据库 CPU 飙升 | **连接池枯竭 / 慢查询** | 系统崩溃的隐患 |

---

## 概念解释 (Conceptual Explanation)

### 1. 什么是 N+1 问题？
假设你有 2 个用户（N=2），你想获取这 2 个用户的所有任务。
-   **1 次查询**: `SELECT * FROM users` -> 得到 [User1, User2]。
-   **N 次查询**: 遍历结果集，分别为 User1 和 User2 去查表：
    -   `SELECT * FROM tasks WHERE user_id = 1`
    -   `SELECT * FROM tasks WHERE user_id = 2`
**结论**: 如果有 100 个用户，代码会发起 101 条 SQL。这就像去超市买东西，每一次只买一件就结账回家，效率极低。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 持久层解决方案：ResultMap
在 MyBatis 的 XML 中，我们使用 `ResultMap` 来定义“父子关系”：
```xml
<resultMap id="UserWithTasksMap" type="User">
    <id property="id" column="id"/>
    <!-- 核心：一对多集合映射 -->
    <collection property="tasks" ofType="Task">
        <id property="id" column="task_id"/>
        <result property="title" column="task_title"/>
    </collection>
</resultMap>
```

---

## 典型用法 (Typical Usage)

### 联表查询 (JOIN)
通过一条主 SQL 直接带出所有子项数据：
```sql
SELECT u.*, t.id AS task_id, t.title AS task_title
FROM users u
LEFT JOIN tasks t ON u.id = t.user_id
```
**优点**: 无论用户有多少，数据库只执行 **1 条 SQL**，极大降低了连接开销。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 `NPlusOneDemoTest.java`:
我们设计了两组对照实验：
-   **实验 A**: 模拟了错误的循环查询。运行后，在日志中你会看到满屏的 `Preparing: SELECT ... FROM tasks`。
-   **实验 B**: 调用了我们编写的 `selectUserWithTasks`。控制台只会清晰地打印 **1 条 SQL**，且通过映射逻辑自动将结果集组装成了嵌套的 Java 对象。
这种通过日志直观感受 SQL 密集度的过程，是建立性能基线认知的最佳方式。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

排查 N+1 问题通常需要通过审计日志。

> **最佳实践 Prompt**:
> "我的 Spring Boot 项目在获取『部门及其下属员工』列表时响应极慢，通过 SQL 日志发现它在循环查询员工表。
> 1. 请帮我分析这是一个标准的 N+1 问题吗？
> 2. 请在 Mapper 的 XML 文件中定义一个 `ResultMap`，使用 `<collection>` 标签实现一对多关联。
> 3. 请生成对应的 `LEFT JOIN` 语句，并展示如何在 Service 层调用该方法以实现一次性加载。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MyBatis 官方文档：高级结果映射](https://mybatis.org/mybatis-3/zh/sqlmap-xml.html#Result_Maps) - 深入了解 nested results。
2. [What is the N+1 selects problem?](https://stackoverflow.com/questions/97197/what-is-the-n1-selects-problem) - StackOverflow 上的经典讨论。
3. [Spring Data JPA: Resolving N+1 with EntityGraphs](https://www.baeldung.com/spring-data-jpa-query-graphs) - 了解其他框架的解决方案。
