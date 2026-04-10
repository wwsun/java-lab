# 07 - 索引认知：给数据库加个“快速查阅目录”

## 核心心智映射 (Core Mental Mapping)

在处理百万级甚至更大数据量时，**索引 (Index)** 是决定系统性能的关键。如果说数据库表是图书馆里的书架，那么索引就是门口的**索引卡片目录**。

| 查询方式 | 无索引 (Full Table Scan) | 有索引 (Index Search) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **逻辑** | 从头翻到尾，直到找到目标 | 查阅目录，直接定位坐标 | 查字典 vs 翻遍整本书 |
| **复杂度** | **O(N)** | **O(log N)** | 线性增长 vs 对数增长 |
| **IO 消耗** | 极高 (由于需要读取全盘) | 极低 (只读取索引树节点) | 硬件资源的节省 |
| **执行速度** | 随数据量增大而迅速变慢 | 始终保持在毫秒级 | 系统的可扩展性 |

---

## 概念解释 (Conceptual Explanation)

### 1. 索引的本质：B+ 树 (B+ Tree)
绝大多数关系型数据库默认使用 B+ 树。
-   **为什么不是 Hash？** Hash 虽快，但不持支范围查询 (`age > 18`) 或排序。B+ 树叶子节点之间有链表，支持高效的范围扫描。

### 2. 聚簇索引 vs 非聚簇索引
-   **聚簇索引 (Clustered)**: 数据行和索引存放在一起，通常是主键。一张表只能有一个。
-   **非聚簇索引 (Secondary)**: 索引树中只存索引列和主键值。查到后需要再根据主键去聚簇索引找数据（“回表”）。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 创建索引
```sql
-- 1. 创建普通索引
CREATE INDEX idx_user_id ON tasks(user_id);

-- 2. 创建唯一索引
CREATE UNIQUE INDEX uk_username ON users(username);

-- 3. 创建复合索引 (多列组合)
CREATE INDEX idx_status_created ON tasks(status, created_at);
```

---

## 典型用法 (Typical Usage)

### ✅ 推荐加索引的场景
1.  **WHERE 子句核心字段**: 例如 `username = ?`。
2.  **JOIN 关联字段**: 外键 `user_id`。
3.  **ORDER BY / GROUP BY**: B+ 树自带排序特征，能避免文件排序 (filesort)。

### ❌ 慎加索引的场景
1.  **基数太低**: 如 `gender` (仅男/女)。索引反而会让数据库变慢。
2.  **频繁修改的字段**: 索引维护需要额外开销。
3.  **小表**: 几百行数据直接全表扫描往往更快。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **覆盖索引 (Covering Index)** 优化：
如果你查询 `SELECT id, username FROM users WHERE username = 'admin'`，而 `username` 上有索引。
由于索引树里已经存了 `username` 和 `id`（主键），数据库会直接从索引树返回结果，**无需再回表读取整行数据**。这也就是为什么我们推荐“只查询需要的列”的原因。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

慢查询分析是中高级开发者的必修课。

> **最佳实践 Prompt**:
> "我的 Spring Boot 应用在处理任务列表查询时非常慢。
> 查询 SQL 为：`SELECT * FROM tasks WHERE status = 'DONE' AND user_id = 100 ORDER BY created_at DESC;`。
> 1. 请帮我分析该 SQL 的执行计划。
> 2. 请建议应该创建一个单列索引、还是一个包含三列的『复合索引』？
> 3. 请说明什么是『最左匹配原则』，以及它如何影响这个复合索引的顺序设计。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MySQL 索引背后数据结构图解](https://blog.codinglabs.org/articles/theory-of-mysql-index.html) - 深度视觉解析。
2. [Baeldung: Guide to Database Indexing](https://www.baeldung.com/cs/database-indexing) - 实战代码指南。
3. [Use The Index, Luke](https://use-the-index-luke.com/) - 最著名的索引优化电子书。
