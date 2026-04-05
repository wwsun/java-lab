# 26-索引认知：给数据库加个“快速查阅目录”

在百万级甚至更大数据量的 Java Web 应用中，**索引 (Index)** 是区分“初级”与“中高级”开发者的分水岭。如果说数据库表是图书馆里的书架，那么索引就是图书馆门口的**索引卡片目录**。

---

## 1. 索引的本质：B+ 树心智模型

绝大多数关系型数据库（MySQL InnoDB, H2, PostgreSQL）默认使用的索引结构是 **B+ 树 (B+ Tree)**。

### 1.1 类比心智图
-   **无索引 (Full Table Scan)**: 从头翻到尾，直到找到目标（O(N) 复杂度）。就像在没有目录的几十万页文档里找一个人名。
-   **有索引 (B+ Tree)**: 每一层都是一个分叉。比如在 A-M 的目录下，找 J，就直接看左边，再进一步细分。平衡树的查询复杂度是 **O(log N)**。

> [!tip] 为什么要用 B+ 树而不是 Hash？
> Hash 索引虽然在等值查询 (`id = 1`) 极快，但它**不支持范围查询** (`age > 18`)。B+ 树通过叶子节点的链表，能高效地处理排序和范围。

---

## 2. 哪些字段应该加索引？(The Do's)

作为开发者，当您设计一个新模块（如项目管理里的 `Projects` 和 `Tasks`）时，以下字段是索引的常客：

1.  **主键 (PK)**: 默认自带聚簇索引。
2.  **外键 (FK)**: 几乎所有的高频联查 (JOIN) 都会用到。比如 `tasks` 表的 `user_id`。
3.  **高频查询过滤字段**: 经常出现在 `WHERE` 子句中的字段。比如 `username`, `email`。
4.  **排序 (ORDER BY) / 分组 (GROUP BY)**: B+ 树本身是有序的，这能极大地加速排序过程。
5.  **唯一性字段**: 比如 `id_card`, `phone_number`。数据库通过索引来确保唯一约束。

---

## 3. 哪些字段不应该加索引？(The Don'ts)

> [!caution] 索引不是越多越好
> 每一个索引都会占用额外的磁盘空间，并且每当你 `INSERT/UPDATE/DELETE` 数据时，数据库都需要**同步维护**多棵 B+ 树。这意味着：**过多的索引会降低写性能**。

1.  **数据量小的表**: 几百行数据全表扫描甚至比读索引卡片还快。
2.  **低基数 (Low Cardinality) 字段**: 字段取值非常有限（如 `gender` (仅 2-3 种)、`is_deleted` (仅 0, 1)）。在这种字段上加索引，数据库优化器通常会选择无视它。
3.  **经常更新的字段**: 索引维护成本过高。
4.  **超大字段**: 比如 `TEXT`, `BLOB` 类型。索引过大会导致磁盘 I/O 剧增，若非要索引，通常使用“前缀索引”。

---

## 4. SQL 中的索引操作

在标准 SQL DDL 中，您可以在创建表时直接定义索引，也可以在表创建后添加。

```sql
-- 在创建表时添加索引
CREATE TABLE `tasks` (
    `id` BIGINT PRIMARY KEY,
    `status` VARCHAR(20),
    `created_at` TIMESTAMP,
    -- 创建名为 idx_status 的普通索引
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB;

-- 或者在表创建后，通过 ALTER 或 CREATE INDEX 添加
CREATE INDEX `idx_status_created` ON `tasks` (`status`, `created_at`);
```

---

## 5. 覆盖索引 (Covering Index) —— 高级进阶

如果您查询的字段已经包含在了索引树中（比如只查询 ID 和 Username，而 Username 有索引），那么数据库就不需要再去回到原始行读磁盘（“回表”），直接从索引结构里取数据并返回。这叫**覆盖索引**，是查询优化的核心武器。

---

## 📚 扩展阅读
1. [MySQL 索引背后的数据结构算法：B-Tree 与 B+Tree](https://blog.codinglabs.org/articles/theory-of-mysql-index.html) - 经典文章。
2. [Baeldung: Guide to Database Indexing](https://www.baeldung.com/cs/database-indexing)

---

### [实操建议]
请 Review 您的 `tasks` 表设计。在上一阶段，我们已经为 `user_id` 添加了外键约束，而主流数据库会自动为外键创建索引。

思考题：如果您需要根据 `status` 和 `created_at` 逆序获取一个用户的任务列表，什么样的组合索引（Composite Index）最合适？
