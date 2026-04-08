# 29 - MyBatis-Plus CRUD 实战指南

## 核心心智映射 (Core Mental Mapping)

在完成 Mapper 接口的定义后，你已经拥有了对数据库进行全量增删改查的能力。

| 操作 | Mapper 方法 | 对应 SQL 生成 | 心智映射 |
| :--- | :--- | :--- | :--- |
| **新增** | `insert(entity)` | `INSERT INTO ...` | 自动处理 null 字段，主键回填 |
| **查询** | `selectById(id)` | `SELECT ... WHERE id = ?` | 简单、直接的 ID 查询 |
| **更新** | `updateById(entity)` | `UPDATE ... SET ... WHERE id = ?` | **仅更新非空字段** (默认策略) |
| **删除** | `deleteById(id)` | `DELETE FROM ... WHERE id = ?` | 物理删除 / 逻辑删除 |

---

## 概念解释 (Conceptual Explanation)

### 1. 动态代理与 SQL 注入
MyBatis-Plus 通过**动态代理**技术，在应用启动时自动将 `BaseMapper` 中的方法映射为对应的预编译 SQL。这意味着你不需要手写任何一行 SQL，框架会根据实体类的注解自动生成。

### 2. 主键自增回填
当你调用 `insert()` 后，数据库生成的自增 ID 会被自动赋值回你的 Java 对象。
-   **优势**: 你无需进行第二次查询，就能立刻拿到新插入数据的 ID（比如用于紧接着的业务关联）。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心方法签名
-   **`int insert(T entity)`**: 返回受影响的行数。
-   **`T selectById(Serializable id)`**: 根据主键查询。
-   **`int updateById(T entity)`**: 根据 ID 修改。注意：实体类中为 null 的字段不会被更新。
-   **`int deleteById(Serializable id)`**: 根据主键删除。

---

## 典型用法 (Typical Usage)

### 1. 插入并获取 ID
```java
User user = new User();
user.setUsername("antigravity");
userMapper.insert(user);
// 此时 user.getId() 已经有值了！
System.out.println("New ID: " + user.getId());
```

### 2. 选择性更新
如果你只想修改密码，只需创建一个只有 ID 和密码的实体对象：
```java
User user = new User();
user.setId(1L);
user.setPassword("new_secret");
userMapper.updateById(user); // 只有 password 字段会被更新到 SQL 中
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **AAA (Arrange-Act-Assert) 测试模式**:
在编写 `UserMapperTest` 时，我们严格遵循：
1.  **Arrange (准备)**: 创建一个测试用户对象。
2.  **Act (执行)**: 调用 `userMapper.insert(user)`。
3.  **Assert (断言)**: 使用 `Assertions.assertNotNull(user.getId())` 验证主键是否成功回填。
这种模式能让你的测试代码逻辑及其清晰，是 Java 工业级开发的标配。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

编写 CRUD 的重复单元测试很枯燥。

> **最佳实践 Prompt**:
> "我刚刚创建了一个 `Order` 实体和对应的 `OrderMapper`。
> 1. 请帮我按照 AAA (Arrange-Act-Assert) 模式编写一个完整的 JUnit 5 测试用例。
> 2. 测试场景包括：插入一条订单并验证 ID 回填、根据 ID 局部更新订单状态、根据 ID 删除订单并验证是否已消失。
> 3. 请包含必要的 `@SpringBootTest` 注解，确保测试在真实的数据库环境（或 H2）下运行。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MyBatis-Plus 核心功能: CRUD 接口](https://baomidou.com/pages/49cc51/) - 完整 API 列表。
2. [JUnit 5 Assertions Guide](https://junit.org/junit5/docs/current/user-guide/#writing-tests-assertions) - 掌握各种断言技巧。