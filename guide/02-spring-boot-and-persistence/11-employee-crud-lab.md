# 11 - MyBatis-Plus 实战：员工系统 CRUD 测试指引

## 核心心智映射 (Core Mental Mapping)

在本项目的演进中，我们已经将 `Employee` 系统从“内存模拟”迁移到了真正的“数据库持久化”。

| 维度 | 旧版 (In-Memory) | 新版 (MyBatis-Plus) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **存储介质** | `ConcurrentHashMap` | **H2 / MySQL 数据库** | 从不稳定到持久化 |
| **主键管理** | 手动生成 UUID 字符串 | **数据库自增 ID** | 移交主键控制权 |
| **查找逻辑** | `map.get(id)` | **`selectById(id)`** | 利用索引查询 |
| **核心优势** | 逻辑简单，重启失效 | **支持复杂查询、永久保存** | 具备工业级生产力 |

---

## 概念解释 (Conceptual Explanation)

### 1. 从内存到持久化的跨越
在 Node.js 中，我们常用简单的 JS 对象做 Mock。迁往 Java 后，我们需要用实体类 (Entity) 映射数据库表。MyBatis-Plus 扮演了“搬运工”的角色，负责在 Java 对象和数据库行之间自动转换数据。

### 2. ID 回填的直观感受
在遗留系统中，你可能需要 `insert` 后再 `select` 才能拿到新 ID。在 MP 中，`insert` 动作完成后，你的 Java 对象里原本为 null 的 id 字段会瞬间被填上对应的值。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 测试核心代码片断 (EmployeeMapperTest.java)
```java
// 1. 新增
employeeMapper.insert(employee);
// 2. 查询
Employee found = employeeMapper.selectById(employee.getId());
// 3. 修改
found.setName("李四");
employeeMapper.updateById(found);
// 4. 删除
employeeMapper.deleteById(found.getId());
```

---

## 典型用法 (Typical Usage)

### 观察 SQL 日志审计
我们在 `application.yml` 中配置了控制台打印 SQL。运行测试时，请密切注意：
-   **Preparing**: 看到 MP 生成的预编译 SQL。
-   **Parameters**: 看到 Java 对象是如何被拆解并填入 SQL 参数位的。
-   **Updates**: 看到数据库返回的受影响行数。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `EmployeeMapperTest.java` 演示的 **AAA (Arrange-Act-Assert)** 流程：
1.  **Arrange**: 构造一个“研发部”的张三对象。
2.  **Act**: 调用 `insertRows = employeeMapper.insert(employee)`。
3.  **Assert**: 断言 `insertRows == 1` 且 `employee.getId() != null`。
这个简单的测试验证了整个持久化链路的连通性，包括：数据库驱动配置、表结构映射以及 MyBatis-Plus 的自动映射。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当你的 Mapper 方法报错时（通常是字段名对不上）：

> **最佳实践 Prompt**:
> "我的 `EmployeeMapper.insert` 方法抛出了 `BadSqlGrammarException`，报错信息是：`Unknown column 'department_name' in 'field list'`。
> 1. 请帮我检查 `Employee` 实体类的字段定义与 `schema.sql` 中的列名是否匹配。
> 2. 请说明如何使用 `@TableField` 注解来修复这种『属性名与列名不一致』的问题。
> 3. 请生成修复代码，确保 MP 能正确解析映射关系。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MyBatis-Plus 自动填充功能](https://baomidou.com/pages/4c6bcf/) - 了解如何让 `createTime` 自动生成。
2. [ActiveRecord 模式简介](https://baomidou.com/pages/49cc51/#activerecord) - 让实体类具备 `.insert()` 能力的高级技巧。
