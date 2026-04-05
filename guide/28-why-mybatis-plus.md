# 31-为什么选择 MyBatis-Plus？：Java 持久层的王者之选

对于从 Node.js 环境转型的开发者来说，MyBatis-Plus (简称 MP) 是目前 Java 生态中平衡“开发效率”和“SQL 控制力”的最佳方案。

---

## 1. 核心定位：只做增强，不做改变

MyBatis-Plus 是在 [MyBatis](https://mybatis.org/mybatis-3/) 基础上的增强工具。如果说 MyBatis 是 **Knex.js**，那么 MyBatis-Plus 就是内置了 **Prisma** 体验的增强版。

### 核心心智图
`JDBC (裸连)` -> `MyBatis (半自动，SQL 映射)` -> `MyBatis-Plus (全自动 CRUD + 动态查询)`

---

## 2. 五大杀手锏

### 2.1 零配置的通用 CRUD
只要你的 Mapper 接口继承了 `BaseMapper<T>`，你就拥有了：
- `insert()`, `deleteById()`, `updateById()`, `selectList()` 等 10+ 个内置方法。
- **Node.js 对比**：类似于 Sequelize 的 `Model.create()`，无需在 XML 中编写对应的 SQL 标签。

### 2.2 类型安全的 Lambda 条件构造器
MP 提供了 `LambdaQueryWrapper`，通过方法引用避免了字符串硬编码。
```java
// Java (MP Lambda)
userMapper.selectList(new LambdaQueryWrapper<User>()
    .eq(User::getStatus, "ACTIVE")
    .between(User::getAge, 20, 30));

// Node.js (Prisma 类比)
// prisma.user.findMany({ where: { status: 'ACTIVE', age: { gte: 20, lte: 30 } } })
```

### 2.3 自动分页 (Pagination)
通过配置 `PaginationInnerInterceptor` 插件，可以实现自动物理分页（支持 MySQL, Oracle, H2 等多种方言），你只需在 Service 层传入一个 `Page` 对象。

### 2.4 主轴功能：自动填充 (Auto Fill)
通过配置，可以在插入或更新时自动为 `createTime`, `updateTime` 赋值。
- **Node.js 对比**：类似于 Mongoose 的 `timestamps: true`。

### 2.5 活跃的国产开源生态
MyBatis-Plus 由国内团队维护，文档极其详尽（全中文），且针对国内常见的业务场景（如分库分表、逻辑删除）有极佳的原生支持。

---

## 3. MyBatis-Plus 标准开发流

对于 Node.js 开发者来说，MP 的开发流程可以类比为：**定义 Schema -> 创建 Repository -> 导出 Service**。以下是标准四步走：

### 第一步：引入依赖 (pom.xml)
类比 `npm install`。建议使用 `mybatis-plus-spring-boot3-starter`。

```xml
<dependency>
    <groupId>baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.5</version>
</dependency>
```

### 第二步：定义实体类 (Entity)
使用 Lombok 消除 Boilerplate，使用 MP 注解映射数据库表。

```java
@Data
@TableName("ts_user") // 类比 @Entity(name = "ts_user")
public class User {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    @TableField("user_name")
    private String name;

    private Integer age; // 字段名与数据库列名一致时可省略注解

    @TableField(fill = FieldFill.INSERT) // 自动填充创建时间
    private LocalDateTime createTime;
}
```

### 第三步：定义 Mapper 接口
核心动作：继承 `BaseMapper<T>`。无需实现类，MP 会自动生成 SQL。

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 基础 CRUD 已内置：insert, delete, update, selectById...
    // 也可以在这里定义复杂的 XML 映射 SQL
}
```

### 第四步：定义 Service 层 (可选但推荐)
MP 提供了一套标准的 `IService` 接口和 `ServiceImpl` 实现类，能极大地简化批量操作和复杂查询。

```java
// 1. 定义接口
public interface UserService extends IService<User> {}

// 2. 编写实现类 (注入 Mapper)
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    // 此时你已经拥有了强大的 list(), saveBatch(), getOne() 等方法
}
```

### 业务调用示例 (Controller)
```java
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // 感悟：一行代码完成查询，无需手写任何 SQL
        return userService.getById(id);
    }
}
```

---

## 4. 开发规范建议

1. **Entity (实体类)**：使用 Lombok 注解减少冗余，配合 MP 的 `@TableId`、`@TableField`。
2. **Mapper 接口**：直接继承 `BaseMapper<Entity>`。
3. **Service 层**：继承 `IService<Entity>` 并实现 `ServiceImpl<Mapper, Entity>`。这是 MP 的一套标准脚手架代码，能让你获得极强的 Service 层扩展能力。

---

## 5. 后续实战预告

在下一章中，我们将：
1. 本地引入 **H2 内存数据库**（无需安装 MySQL，即开即用）。
2. 让 `Employee` 系统真正的持久化。
3. 演示如何用三行代码实现分页查询。

---

### 📚 扩展阅读
1. [MyBatis-Plus 官方文档](https://baomidou.com/) - 建议重点看“入门”和“条件构造器”章节。
2. [MyBatis 动态 SQL 原理](https://mybatis.org/mybatis-3/zh/dynamic-sql.html) - 了解 MP 底层的运行基石。
