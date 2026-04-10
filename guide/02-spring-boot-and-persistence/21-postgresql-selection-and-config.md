# 21 - MySQL vs PostgreSQL：当前项目为什么默认选 MySQL

## 核心心智映射

对 Node.js 开发者来说，这个选择题很像：

- 你做一个标准后台管理系统时，会优先用 MySQL
- 如果业务大量依赖 JSONB、全文检索、复杂分析查询，才会认真考虑 PostgreSQL

Java 里也是一样，只是数据库切换通常还会牵动 JDBC 驱动、方言、分页插件和初始化脚本。

| 维度 | MySQL 8.4 | PostgreSQL 16 | 怎么理解 |
| --- | --- | --- | --- |
| 学习门槛 | 更低 | 略高 | 先跑通业务，再追求高级能力 |
| 生态兼容 | 国内教程和案例更多 | 也很成熟 | MySQL 更适合当前学习路径 |
| JSON 能力 | 够用 | 更强，尤其 JSONB | PG 更适合复杂半结构化数据 |
| SQL 标准支持 | 较强，但方言更多 | 更接近标准 SQL | PG 在高级 SQL 上更舒服 |
| 本项目适配 | 默认推荐 | 可选进阶路线 | 当前主线先走 MySQL |

## 为什么当前项目默认选 MySQL

这是出于“学习路径”和“工程阻力”两方面考虑。

### 1. 与当前脚手架和教程更一致

当前学习路线里的：

- MyBatis-Plus CRUD
- HikariCP 配置
- Docker Compose 中间件环境
- 会议室预约系统练习

都默认围绕 MySQL 展开，切入成本最低。

### 2. 后端管理系统场景下足够通用

用户、角色、菜单、订单、预约、库存，这类典型业务用 MySQL 完全够用。

### 3. 便于你先把重点放在 Java 工程化能力上

第 2 周和第 3 周真正要学的是：

- Spring Boot 分层
- MyBatis-Plus
- DTO / Entity 边界
- 事务
- 安全、缓存、部署

不是数据库高阶特性的 PK。

## PostgreSQL 更适合什么场景

如果你后面遇到以下需求，PostgreSQL 会更有吸引力：

- 大量使用 JSONB 做复杂查询
- 频繁使用窗口函数、CTE
- 需要地理位置计算，想接入 PostGIS
- 需要更强的 SQL 标准能力

所以它不是“不好”，而是“当前主线不优先”。

## 切换到 PostgreSQL 需要改什么

### 1. 驱动依赖

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. 数据源配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/java_labs
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: postgres
```

### 3. MyBatis-Plus 方言

如果用了分页插件，需要显式指定：

```java
interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
```

### 4. DDL 语法

最常见的差异包括：

- `AUTO_INCREMENT` 需要改写
- `datetime` / `timestamp` 习惯不同
- JSON 字段建议改为 `jsonb`
- 部分索引语法和函数写法不同

## 一个简单判断原则

如果你现在的问题是：

- “接口先跑起来”
- “先把业务表建出来”
- “先学会 MyBatis-Plus 和事务”

那就选 MySQL。

如果你现在的问题已经变成：

- “我要做复杂搜索和分析”
- “我想充分利用 JSONB”
- “我愿意为数据库能力承担更高迁移成本”

再认真评估 PostgreSQL。

## Node.js / TypeScript 迁移视角

你可以这样映射：

- MySQL 很像大多数传统业务系统的默认数据库
- PostgreSQL 很像 Supabase / Hasura 背后的那套“更现代、更高级”的数据能力

区别不在“能不能用”，而在“当前阶段优先学哪个更划算”。

## AI 辅助开发实战建议

> 请基于当前 MySQL 项目，帮我评估是否值得迁移到 PostgreSQL：
> 1. 先列出当前业务是否真的需要 JSONB、窗口函数、GIS；
> 2. 列出驱动、配置、DDL、分页插件、初始化脚本的改动点；
> 3. 给出“继续用 MySQL”与“迁移到 PostgreSQL”的成本对比。

## 扩展阅读

1. [PostgreSQL Official Site](https://www.postgresql.org/)
2. [Baeldung: PostgreSQL JSONB](https://www.baeldung.com/spring-boot-jpa-storing-postgresql-jsonb)
3. [MyBatis-Plus Pagination Plugin](https://baomidou.com/)
