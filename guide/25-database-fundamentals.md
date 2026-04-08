# 25 - 关系型数据库基础：SQL DDL 核心规范

## 核心心智映射 (Core Mental Mapping)

在 Java 生态（特别是 Spring Boot）中，我们通常遵循“设计优先”的原则：先定义数据库 Schema (DDL)，再根据表结构编写 Entity 实体类。

| 概念 | Node.js (Object / JSON) | 关系型数据库 (SQL) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **数据容器** | Array / Object | **表 (Table)** | 数据的逻辑集合 |
| **属性定义** | Key-Value | **字段 (Column)** | 严格的类型约束 |
| **唯一标识** | `_id` (MongoDB) | **主键 (Primary Key)** | 每一行的身份证 |
| **数据关联** | 嵌套对象 / Reference | **外键 (Foreign Key)** | 物理级的强引用关系 |

---

## 概念解释 (Conceptual Explanation)

### 1. DDL (Data Definition Language)
数据定义语言。用于创建、修改和删除数据库的结构（表、索引等）。与操作数据的 DML (Insert/Update) 不同，DDL 关注的是“容器”本身。

### 2. 一对多关系 (One-to-Many)
这是最常见的业务模型。
-   **原则**: “多”的一方（子表）持有“一”的一方（父表）的主键作为外键。
-   **示例**: 一个用户 (User) 拥有多个任务 (Task)。`tasks` 表中必须有一个 `user_id` 字段指向 `users` 表。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心 DDL 语法 (MySQL 8.x)
```sql
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY, -- 主键自增
    `username` VARCHAR(50) NOT NULL UNIQUE, -- 唯一约束
    `email` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 时间戳
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 典型用法 (Typical Usage)

### 1. 约束的最佳实践
-   **NOT NULL**: 尽量所有字段都加上非空约束。
-   **DEFAULT**: 为枚举字段（如状态）设置默认值。
-   **COMMENT**: 必须为每个字段写中文注释，这对后期自动生成代码非常重要。

### 2. 类型选择
-   **主键**: 统一使用 `BIGINT`（对应 Java 的 `Long`），支持自增或分布式 ID。
-   **字符串**: 长度超过 255 建议考虑 `TEXT`，常规名称用 `VARCHAR`。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 `schema.sql`:
我们定义了 `users` 和 `tasks` 两个表。
-   `tasks` 表通过 `CONSTRAINT fk_tasks_user` 建立了物理外键。
-   `ON DELETE CASCADE`: 意味着当一个用户被删除时，他名下的所有任务也会被自动清理。
这种设计保证了数据的**完整性**，即使在直接操作数据库时也不会出现孤儿数据。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

手动写 DDL 很容易笔误，建议交给 AI。

> **最佳实践 Prompt**:
> "我需要为一个『图书管理系统』设计 DDL。
> 1. 请生成 `books` 表（书名、作者、ISBN、库存）和 `categories` 表（分类名）。
> 2. 它们之间是多对一关系（一本书属于一个分类）。
> 3. 请包含 `created_at` 和 `updated_at` 字段，并生成 MySQL 8 兼容的 DDL 脚本。
> 4. 请为所有字段添加详尽的中文 COMMENT。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MySQL 8.0: CREATE TABLE Reference](https://dev.mysql.com/doc/refman/8.0/en/create-table.html) - 官方语法手册。
2. [Database Normalization (1NF, 2NF, 3NF)](https://www.baeldung.com/cs/database-normalization) - 了解如何优雅地拆分表结构。
