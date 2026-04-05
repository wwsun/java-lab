# 32-MyBatis XML Mapper 深度解析：掌控 SQL 的终极力量

在 MyBatis-Plus 中，虽然 90% 的简单增删改查可以通过 `BaseMapper` 自动完成，但剩下的 10% 复杂场景（如复杂的联表、动态 SQL、报表查询）则必须依靠 **XML Mapper**。

本文将以 [UserMapper.xml](../src/main/resources/mapper/UserMapper.xml) 为例，拆解其背后的逻辑。

---

## 1. 结构概览：谁是接口？谁是实现？

在 Java 届，`Mapper` 采用了类似 **Controller/Service 接口与实现分离** 的思想：

-   **Java 接口 (`UserMapper.java`)**: 仅仅是方法的声明。
-   **XML 文件 (`UserMapper.xml`)**: 接口的具体实现。

两者通过 `namespace` 属性实现“灵魂绑定”：
```xml
<mapper namespace="com.javalabs.mapper.UserMapper">
```

---

## 2. ResultMap：对象映射的“图纸”

这是 XML 文件中最核心的部分。它解决了 **SQL 表结构** 与 **Java 类结构** 不一致的问题。

### 为什么需要它？
-   **字段别名**：数据库是 `user_name`，Java 是 `username`。
-   **类型转换**：数据库是 `INT`，Java 是自定义 `Enum`。
-   **对象嵌套**：数据库是两条平铺的 Join 结果，Java 需要组装成 `User` 嵌套 `List<Task>`。

### 标签拆解：
-   `<id>`: 映射 **主键**。MyBatis 会用主键来判断这两条数据是不是属于同一个对象。
-   `<result>`: 映射普通字段。
-   `<collection>`: 处理 **一对多**。
    -   `property`: Java 实体类里的字段名。
    -   `ofType`: 集合里装的是什么类。

---

## 3. SQL 语句：精细化控制

```xml
<select id="selectUserWithTasks" resultMap="UserWithTasksMap">
    SELECT ... FROM users u LEFT JOIN tasks t ...
</select>
```

-   **id**: 必须对应接口中的方法名。
-   **resultMap**: 如果返回的是复杂对象（有嵌套），必须指向上面定义的 `<resultMap>` 的 `id`。
-   **resultType**: 如果返回的是基础类型（如 `String`）或简单对象，可以直接写类名。

---

## 4. Node.js 开发者视角的“映射”

如果你习惯了 **Prisma** 或 **TypeORM**，可以这样理解：

| 维度 | Prisma | MyBatis XML |
| :--- | :--- | :--- |
| **定义关系** | `schema.prisma` 中的 `Relation` | XML 中的 `<collection>` / `<association>` |
| **查询关联** | `prisma.user.findMany({ include: ... })` | 调用 XML 里的 `LEFT JOIN` SQL |
| **字段映射** | `@map("column_name")` | XML 中的 `<result column="..." property="..." />` |

---

## 5. 什么时候该用 XML？

作为资深开发者，请遵循以下分界线：

1.  **用 BaseMapper (MyBatis-Plus)**: 简单的单表 CRUD、简单的分页。
2.  **用注解 (`@Select`)**: 简单的两表关联，SQL 不超过 5 行。
3.  **用 XML**: 涉及 3 张表以上的 JOIN、复杂的动态判断 (如 `if/where/foreach` 标签)、或者需要极限优化 SQL 性能时。

---

### 📚 练习建议
您可以尝试在 `UserMapper.xml` 中增加一个 `<delete>` 标签，实现根据用户名模糊删除任务的功能，并思考这与在 Service 层循环删除有何性能差异。
