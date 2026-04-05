# 32-MyBatis-Plus CRUD 实战指南

在完成 Mapper 接口的定义后，您已经拥有了对数据库进行全量增删改查的能力。本章将详细讲解 `BaseMapper` 提供的四个核心方法。

---

## 1. 核心 CRUD 方法一览

| 操作     | Mapper 方法                   | 对应 SQL 生成                     | 说明                                   |
| :------- | :---------------------------- | :-------------------------------- | :------------------------------------- |
| **新增** | `userMapper.insert(user)`     | `INSERT INTO ...`                 | 自动处理 null 字段，支持主键自增回填。 |
| **查询** | `userMapper.selectById(id)`   | `SELECT ... WHERE id = ?`         | 简单、直接的 ID 查询。                 |
| **更新** | `userMapper.updateById(user)` | `UPDATE ... SET ... WHERE id = ?` | 仅更新非空字段（默认配置下）。         |
| **删除** | `userMapper.deleteById(id)`   | `DELETE FROM ... WHERE id = ?`    | 物理删除（也可配置逻辑删除）。         |

---

## 2. 深入理解 BaseMapper

MyBatis-Plus 通过**动态代理**技术，在应用启动时自动将 `BaseMapper` 中的方法映射为对应的 SQL。

### 2.1 主键策略

在 `User` 实体类中，我们使用了 `@TableId(type = IdType.AUTO)`。这意味着：

- 执行 `insert()` 后，MyBatis-Plus 会自动将数据库生成的 ID 赋值回 `User` 对象的 `id` 属性。
- 您无需进行第二次查询即可获得新插入行的 ID。

---

## 3. SQL 日志审计

在开发阶段，我们在 `application.yml` 中配置了：

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

**为什么这很重要？**
虽然我们不用写 SQL，但查看生成的 SQL 是确保业务逻辑正确、排查 N+1 查询等性能问题的唯一途径。每当我们执行一次测试，控制台都会打印出完整的预编译 SQL 和参数。

---

## 4. 动手实操提示：AAA 测试模式

在编写单元测试（如 `UserMapperTest` 或 `EmployeeMapperTest`）时，我们强烈建议遵循 **AAA (Arrange-Act-Assert)** 模式。这是一种公认的测试结构，能让测试逻辑极其清晰：

### 4.1 AAA 模式分解

1.  **Arrange (准备)**:
    - **职责**: 设置测试所需的先决条件。
    - **操作**: 初始化实体对象、填充测试数据、配置模拟（Mock）对象。
    - **示例**: `User user = new User("Alice", 25);`

2.  **Act (执行)**:
    - **职责**: 调用实际要测试的方法（即核心测试逻辑）。
    - **操作**: 触发 Mapper 方法，捕获返回值或受影响行数。
    - **示例**: `int rows = userMapper.insert(user);`

3.  **Assert (断言)**:
    - **职责**: 验证结果是否符合预期。
    - **操作**: 检查数据库返回的行数、对象状态变化（如 ID 是否回填）、或者是否抛出了预期的异常。
    - **示例**: `Assertions.assertNotNull(user.getId());`

### 4.2 为什么在 MyBatis-Plus 实战中很重要？

在数据库测试中，我们经常容易把“准备数据”和“验证逻辑”混在一起。使用 AAA 模式可以强制你思考：
- **副作用验证**: 比如 `insert` 之后对象 ID 的变化，这属于 `Assert` 阶段的检查点。
- **配置一致性**: 比如 `updateById` 是否只更新了非空字段，你可以清晰地在 `Arrange` 阶段设置部分字段，并在 `Assert` 阶段验证结果是否符合预期。

---

## 📚 扩展阅读

1. [MyBatis-Plus 核心功能: CRUD 接口](https://baomidou.com/pages/49cc51/)
2. [JUnit 5 断言文档](https://junit.org/junit5/docs/current/user-guide/#writing-tests-assertions)