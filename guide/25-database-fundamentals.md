# 25-关系型数据库基础：SQL DDL 核心规范

在 Java 生态（特别是 Spring Boot）中，我们通常遵循“设计优先”的原则：先定义数据库 Schema (DDL)，再根据表结构编写 Entity 实体类。理解 DDL 是构建高质量 Java Web 应用的基石。

---

## 1. 核心概念映射

| 概念 | 关系型数据库 (SQL) | 说明 |
| :--- | :--- | :--- |
| **表 (Table)** | `CREATE TABLE` | 数据的逻辑容器 |
| **字段 (Field)** | `COLUMN` | 数据的属性及其数据类型 |
| **主键 (PK)** | `PRIMARY KEY` | 唯一标识一行（通常使用自增 ID 或 UUID） |
| **外键 (FK)** | `FOREIGN KEY` | 建立两表之间的关联（实现数据引用） |

---

## 2. 一对多关系设计：用户与任务

在我们的实战场景中，**一个用户 (`User`) 可以拥有多个任务 (`Task`)**。

### 2.1 心智模型
-   **父表 (Parent Table)**: `users` (被引用的地方)
-   **子表 (Child Table)**: `tasks` (持有外键的一方)

> [!important] 设计规范
> 在关系型数据库中，“多” 的一方（子表）负责持有外键指向 “一” 的一方（父表）。

---

## 3. 实操：编写 DDL 脚本

以下是符合 MySQL 8+ 或 H2 兼容的 DDL 语法。我们将使用 `BIGINT` 自增 ID 和现代的时间字段。

### 3.1 创建用户表 (`users`)

```sql
-- 仅供理解设计方案，后续 Spring Boot 整合时将由框架加载
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '哈希后的密码',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基本信息表';
```

### 3.2 创建任务表 (`tasks`)

```sql
CREATE TABLE `tasks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    `user_id` BIGINT NOT NULL COMMENT '所属用户 ID (外键)',
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务描述',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态: PENDING, DOING, DONE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 建立物理外键约束
    CONSTRAINT `fk_tasks_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务列表';
```

---

## 4. DDL 与 Java 开发的关系

在 Java 的 **MyBatis-Plus** 体系中：
1.  **物理表** 是整个系统的持久化基石。
2.  **Entity (实体类)** 是 Java 层面对该表的映射，通常通过 Lombok 注解简化开发。
3.  **Mapper** 负责执行 SQL，MyBatis-Plus 通过分析映射关系为您提供基础 CRUD 能力。

理解了 DDL 的详细定义，您就能更好地指揮 AI Agent 为您生成对应的 Service 层骨架和业务代码。

---

## 📚 扩展阅读
1. [MySQL 8.0 官方文档: CREATE TABLE](https://dev.mysql.com/doc/refman/8.0/en/create-table.html)
2. [数据库范式 (Normalization) 浅析](https://www.baeldung.com/cs/database-normalization) - 了解 1NF, 2NF, 3NF 基本原则。

---

### [运行指南]
您可以将上述 SQL 暂存，在后续 **MyBatis-Plus** 整合实验中，我们将：
1.  在 `src/main/resources` 下创建 `schema.sql`。
2.  配置 Spring Boot 自动初始化。
3.  使用 Lombok 生成对应的 `User` 和 `Task` 实体类。
