# 33-MyBatis-Plus 实战：员工系统 CRUD 测试指引

在第 2 周的学习中，我们已经将 `Employee` 系统从内存模拟（ConcurrentHashMap）迁移到了真正的数据库持久化（MyBatis-Plus + H2）。

本篇指南将带您深入剖析 [EmployeeMapperTest.java](../src/test/java/com/javalabs/mapper/EmployeeMapperTest.java) 这个核心示例文件，理解 MyBatis-Plus 的自动映射魔法。

---

## 1. 核心心智模型迁移

| 维度 | 旧版 (In-Memory) | 新版 (MyBatis-Plus) |
| :--- | :--- | :--- |
| **存储介质** | `ConcurrentHashMap<String, Employee>` | H2 内存数据库 (表名: `employees`) |
| **主键管理** | 手动生成/传入 ID | `@TableId(type = IdType.AUTO)` 数据库自增 |
| **核心逻辑** | 手写 `map.put()`, `map.get()` | 继承 `BaseMapper` 直接调用 `insert()`, `selectById()` |

---

## 2. 测试代码深度解析 (AAA 模式)

打开 [EmployeeMapperTest.java](../src/test/java/com/javalabs/mapper/EmployeeMapperTest.java)，你会看到四个经典的测试步骤：

### 2.1 新增 (Insert) & ID 回填
```java
int insertRows = employeeMapper.insert(employee);
Assertions.assertNotNull(employee.getId());
```
**Node.js 对比**：
在 Knex 或 Sequelize 中，执行 `create()` 后通常会返回新纪录。在 MyBatis-Plus 中，`insert()` 方法返回的是**受影响的行数**，而新生成的 ID 会由于 MyBatis 的主键回填机制，直接赋值给原来的 `employee` 对象。

### 2.2 读取 (Select)
```java
Employee foundEmployee = employeeMapper.selectById(id);
```
这是全自动生成的 SQL：`SELECT id, name, department, salary... FROM employees WHERE id = ?`。

### 2.3 更新 (Update)
```java
employeeMapper.updateById(foundEmployee);
```
**注意**：`updateById` 默认配置下只会更新**非空 (Not Null)** 字段。这能防止因某些字段未传而意外将数据库中的值覆盖为 NULL。

### 2.4 删除 (Delete)
```java
employeeMapper.deleteById(id);
```
物理删除。在生产环境中，我们经常会配置“逻辑删除”（即设置一个 `deleted` 标志位），MyBatis-Plus 只需一个 `@TableLogic` 注解即可实现这一平滑切换。

---

## 3. 如何运行此测试？

由于我们集成了 Maven，您可以在命令行中快速验证：

```bash
# 仅运行该测试类
mvn test -Dtest=EmployeeMapperTest
```

**运行结果观察**：
得益于 `application.yml` 中的 `mybatis-plus.configuration.log-impl` 配置，您会在控制台看到如下精美的 SQL 日志：

```sql
==>  Preparing: INSERT INTO employees ( name, department, salary ) VALUES ( ?, ?, ? )
==> Parameters: 张三(String), 研发部(String), 15000.0(Double)
<==    Updates: 1
```

---

## 4. 总结与下一步

通过这个“代码示例文件”，您已经完成了从“内存开发”到“数据库驱动”的关键一跃。
接下来的挑战是：在 [EmployeeServiceImpl.java](../src/main/java/com/javalabs/service/impl/EmployeeServiceImpl.java) 中，尝试结合 `LambdaQueryWrapper` 实现更复杂的条件查询。

---

### 📚 扩展阅读
1. [MyBatis-Plus 自动填充功能](https://baomidou.com/pages/4c6bcf/) - 了解如何自动生成 `createTime`。
2. [ActiveRecord 模式 (Optional)](https://baomidou.com/pages/49cc51/#activerecord) - 了解如何让 Entity 直接 .save()。
