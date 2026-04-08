# 41 - MySQL vs PostgreSQL：如何选择适合你的生产级数据库？

## 核心心智映射 (Core Mental Mapping)

如果你曾使用过 Node.js 的 `mysql2` 或 `pg` 库，你会发现 Java 下两者的配置链路高度相似，但在方言处理和性能特性上各有千秋。

| 维度 | MySQL 8.x | PostgreSQL 16.x | 心智映射 |
| :--- | :--- | :--- | :--- |
| **定位** | 追求极速读写，国内互联网大厂首选 | **最先进的对象关系型数据库** | 简单快速 vs 严谨大而全 |
| **Node.js 背景** | 类似于很多老牌 CMS、Discuz 的默认选择 | **Supabase、Hasura 等现代平台的首选** | 经典 vs 前卫 |
| **JSON 支持** | 基础支持，基本够用 | **JSONB 性能极佳** | 结构化 vs 半结构化平衡 |
| **SQL 标准** | 支持度一般，方言较多 | **极度接近标准 SQL** | 行业方言 vs 国际通用语 |
| **Java 适配** | 一流公民，文档极多 | **完美支持，复杂查询首选** | 通用 vs 高级 |

---

## 概念解释 (Conceptual Explanation)

### 1. 为什么选 PostgreSQL？
-   **复杂查询**: 如果你的业务涉及大量的窗口函数、CTE（公共表表达式），PG 的执行计划优化更稳健。
-   **JSONB**: PG 的 JSONB 支持索引，是很多放弃 MongoDB 的项目的理想替代品。
-   **GIS**: 如果应用涉及地理位置计算，PostGIS 是行业唯一的“真理”。

### 2. 本项目建议
如果你是初学者，**推荐首选 MySQL**。因为它在 H2 内存数据库中的兼容性最成熟，能极大降低学习门槛。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### PostgreSQL 驱动配置 (pom.xml)
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### JDBC 连接字符串
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/java_labs
    driver-class-name: org.postgresql.Driver
```

---

## 典型用法 (Typical Usage)

### MyBatis-Plus 方言适配
由于分页逻辑在不同数据库间存在差异（MySQL 用 `LIMIT`, Oracle 用 `ROWNUM`），切换到 PG 后需在配置类中指定：
```java
// 在 MybatisPlusInterceptor 配置中
interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **解耦设计**:
得益于 Spring Data 的抽象，从 MySQL 切换到 PostgreSQL 你只需要：
1.  更改 `pom.xml` 中的驱动包。
2.  修改 `application.yml` 中的 URL 和驱动类名。
3.  重启应用。
业务逻辑层（Service/Mapper）的代码**完全不需要修改**。这种“换根不换叶”的能力是大型 Java 应用保持长生命周期的关键。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

两者的 DDL 语法存在细微差异（如自增主键）。

> **最佳实践 Prompt**:
> "我需要将现有的 MySQL DDL 迁移到 PostgreSQL。
> 1. 请帮我将 `BIGINT AUTO_INCREMENT` 改为 PG 兼容的 `SERIAL` 或 `GENERATED ALWAYS AS IDENTITY`。
> 2. 请解释 PG 中 `TEXT` 类型与 MySQL `LONGTEXT` 的性能差异。
> 3. 请生成一份 Docker Compose 配置，包含初始化脚本挂载，以便一键拉起 PG 环境。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [PostgreSQL: The World's Most Advanced Open Source Relational Database](https://www.postgresql.org/) - 官方网站。
2. [Baeldung: Hibernate and PostgreSQL JSONB](https://www.baeldung.com/hibernate-postgresql-jsonb) - Java 与 PG 的高级联动。
3. [Dzone: Why PostgreSQL is a Better Alternative to MySQL](https://dzone.com/articles/why-postgresql-is-a-better-alternative-to-mysql) - 选型辩论赛。
