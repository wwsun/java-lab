# 32 - MyBatis XML Mapper 深度解析：掌控 SQL 的终极力量

## 核心心智映射 (Core Mental Mapping)

虽然 MyBatis-Plus 自动处理了 90% 的 CRUD，但剩下的 10% 复杂场景（如深度联表、动态 SQL、高性能报表）必须依靠 **XML Mapper**。

| 领域 | Java 接口 (Mapper.java) | XML 实现 (Mapper.xml) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **定位** | 业务的“契约” | SQL 的“实验室” | 接口与实现的解耦 |
| **映射** | 方法名声明 | 通过 `id` 绑定实现 | 灵魂绑定的过程 |
| **映射规则** | (默认推断) | **ResultMap (精密蓝图)** | 描述数据如何拼装成对象 |
| **控制力** | 较低 (自动化) | **极高 (像素级 SQL 控制)** | 性能优化的最后领地 |

---

## 概念解释 (Conceptual Explanation)

### 1. 灵魂绑定：NameSpace
在 Java 中，`UserMapper` 接口只有方法头没有方法体。XML 通过 `namespace="com.javalabs.mapper.UserMapper"` 告诉框架：我就是这个接口的业务实现。

### 2. ResultMap：对象组装蓝图
这是 MyBatis 最强大的地方。它解决了表结构与 Java 对象结构不一致的问题：
-   **字段转换**: 数据库 `user_name` 映射到 Java `username`。
-   **对象嵌套**: 将平铺的 JOIN 结果集“折叠”成嵌套的 Java 对象树（如 User 包含 List<Task>）。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心标签拆解
-   **`<mapper namespace="...">`**: 声明绑定的接口全路径。
-   **`<resultMap>`**: 定义复杂的映射规则。
    -   **`<id>`**: 标记主键（必填，用于对象去重）。
    -   **`<result>`**: 映射普通列。
    -   **`<collection>`**: 处理“一对多”关联。
    -   **`<association>`**: 处理“一对一”关联。

---

## 典型用法 (Typical Usage)

### 定义一个一对多 ResultMap
```xml
<resultMap id="UserWithTasksMap" type="User">
    <id property="id" column="id"/>
    <result property="username" column="user_name"/>
    <!-- 处理多个任务 -->
    <collection property="tasks" ofType="Task">
        <id property="id" column="task_id"/>
        <result property="title" column="task_title"/>
    </collection>
</resultMap>
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 `UserMapper.xml`:
当调用 `selectUserWithTasks` 方法时：
1.  MyBatis 执行一条 `LEFT JOIN` 语句。
2.  由于结果集是“笛卡尔积”形式的平铺行，MyBatis 会根据 `id` 标签判断哪些行属于同一个 User。
3.  它会自动创建 User 实例，并将多行记录中的 Task 数据塞进该 User 的 `tasks` 列表里。
这种“自动折叠”能力让开发者无需手动通过 `for` 循环去合并数据。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

手写 XML 极其容易出现命名空间或标签错误。

> **最佳实践 Prompt**:
> "我需要为一个『财务系统』编写一个复杂的报表查询：统计每个部门下所有员工在过去 3 个月的平均奖金。
> 1. 请帮我设计涉及 `departments` 和 `bonus_records` 的 `LEFT JOIN` SQL。
> 2. 请生成对应的 MyBatis XML 片段，包含一个 `ResultMap` 处理部门与奖金记录的一对多映射。
> 3. 请确保使用 `<where>` 和 `<if>` 标签来支持可选的时间范围过滤。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MyBatis XML Mapper 官方文档](https://mybatis.org/mybatis-3/zh/sqlmap-xml.html) - 标签属性的最全解释。
2. [Effective MyBatis: ResultMap 性能陷阱](https://www.baeldung.com/mybatis-resultmap) - 避免常见的映射错误。
3. [Prisma Relationship Mapping Guide](https://www.prisma.io/docs/concepts/components/prisma-schema/relations) - 对标 Node.js 开发者的关系映射心智。
