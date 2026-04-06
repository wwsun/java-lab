# 41-MySQL vs PostgreSQL：如何选择适合你的生产级数据库？

在 Node.js 生态中，你可能已经用过 `mysql2` 或 `pg` 驱动。在 Java 生态（尤其是 Spring Boot + MyBatis-Plus）中，两者都是一流公民。

---

## 一、 技术对比：该选哪一个？

| 维度 | MySQL 8.x | PostgreSQL 16.x |
| :--- | :--- | :--- |
| **定位** | “世界上最流行的开源数据库”，追求简单、极速渲染。 | “世界上最先进的开源对象关系数据库”，追求功能严谨、大而全。 |
| **Node.js 开发者关联** | 类似于很多 CMS、Discuz 等老牌项目的默认选择。 | 类似于 **Supabase**、**Hasura** 等现代后端平台的首选方案。 |
| **JSON 支持** | 支持 JSON 类型，基本够用。 | **JSONB 性能极佳**，是很多放弃 MongoDB 的项目的“真香”替代品。 |
| **SQL 标准建议** | 对 SQL 标准支持度一般。 | 非常接近标准 SQL，对复杂查询（CTE, Window Functions）支持更好。 |
| **本项目建议** | **推荐初学者首选 MySQL**。文档多、云服务（RDS）普遍，且 H2 的 MySQL 兼容模式做得最成熟。 | 如果你有**复杂数据结构**或**GIS 地理信息**需求，建议选择 PG。 |

---

## 二、 配置 PostgreSQL (pom.xml)

如果你决定使用 PostgreSQL，需要先引入驱动：

```xml
<!-- PostgreSQL 驱动 -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 三、 配置 application.yml

PostgreSQL 的连接 URL 格式与 MySQL 略有不同：

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    # 注意：PG 默认使用 5432 端口
    url: jdbc:postgresql://localhost:5432/java_labs
    username: postgres
    password: your_password
```

---

## 四、 MyBatis-Plus 的差异适配

在 `MybatisPlusConfig.java` 中（如果开启了分页），需要将 `DbType` 设置为 `POSTGRE_SQL`。

```java
// 在分页插件配置中
interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
```

---

## 五、 Docker 一键部署

在 `docker-compose.yml` 中添加（或替换）配置：

```yaml
  postgres:
    image: postgres:16
    container_name: java-labs-pg
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
      POSTGRES_DB: java_labs
    volumes:
      - ./pg_data:/var/lib/postgresql/data
```

---

**总结**：
- 如果你追求 **生态通用性** 和 **简单上路**，选 **MySQL**。
- 如果你的应用涉及 **深度地理分析** 或 **极其复杂的数据报表**，选 **PostgreSQL**。

在学习阶段，我建议您先在应用中跑通一种（如 MySQL），掌握了数据库连接池（HikariCP）和 ORM 映射原理后，底层的切换成本其实非常低。
