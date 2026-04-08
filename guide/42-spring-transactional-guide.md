# 42 - Spring Boot 中的事务管理 (@Transactional)

## 核心心智映射 (Core Mental Mapping)

在 Node.js 中，你可能习惯了显式地控制事务（如 `trx.commit()`）。在 Spring Boot 中，我们使用**声明式事务**，让框架通过注解自动处理。

| 领域 | Node.js (Knex / TypeORM) | Java (Spring Boot) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **控制方式** | 编程式：手动开启、提交、回滚 | **声明式：通过 @Transactional 注解** | 自动化管理 |
| **回滚触发** | `try-catch` 中手动调用 `rollback` | **抛出运行时异常时自动回滚** | 异常即指令 |
| **传播行为** | 较难处理嵌套事务 | **Propagation 参数 (默认 REQUIRED)** | 灵活的层级控制 |
| **底层原理** | 传递事务对象 (trx) | **Spring AOP (代理模式)** | 动态织入拦截逻辑 |

---

## 概念解释 (Conceptual Explanation)

### 1. 声明式事务的本质
当你给方法加上 `@Transactional` 时，Spring 会在运行时为你的 Service 创建一个“代理对象”。当别人调用这个方法时，其实是先经过了代理对象，它负责打开数据库连接、关闭自动提交，并在方法结束后根据是否有异常来决定 `commit` 还是 `rollback`。

### 2. ACID 特性
-   **原子性 (Atomicity)**: 所有操作要么全成，要么全败。
-   **一致性 (Consistency)**: 事务执行前后，数据必须处于一致状态。
-   **隔离性 (Isolation)**: 并发事务互不干扰。
-   **持久性 (Durability)**: 一旦提交，结果永久保存。

---

## 什么时候使用？(When to Use?)

并不是所有数据库操作都需要开启事务。过度使用会降低并发性能，而不使用则会导致数据不一致。

### 1. 必须使用的场景 (Critical Scenarios)
-   **多表关联更新**：典型的“转账”场景，A 表扣钱，B 表加钱。必须保证要么全成功，要么全失败。
-   **业务状态流转**：例如“订单支付成功”后，需要：1. 更新订单状态；2. 减少商品库存；3. 增加用户积分。这三步逻辑上是一个不可分割的整体。
-   **级联删除/更新**：删除一个主记录时，需要同步手动清理多个关联子表的数据。

### 2. 建议使用的场景 (Recommended Scenarios)
-   **同一方法内的多次更新**：即便操作的是同一张表，只要涉及多条 SQL 写入，都应开启事务。
-   **读一致性要求极高的查询**：设置 `@Transactional(readOnly = true)`。例如报表生成，需要确保查询过程中数据不被其他事务修改，保证结果的“快照一致性”。

### 3. 禁止使用的场景 (Anti-Patterns) —— 重点避坑
-   **包含长耗时外部 API 调用**：
    -   ❌ **错误写法**: `开启事务 -> 更新 DB -> 调用外部邮件接口 (耗时 2s) -> 提交事务`。
    -   🚨 **风险**: 数据库连接会卡在调用接口的 2 秒内不释放。并发高时，连接池会瞬间耗尽，导致系统崩溃。
    -   ✅ **正确写法**: 先提交事务释放 DB 连接，再进行异步 API 调用；或者改为“本地消息表”模式。
-   **纯内存计算逻辑**：不涉及数据库操作的代码，开启事务只会白白浪费 CPU 和 AOP 性能。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心参数
-   **`rollbackFor = Exception.class`**: **(必填推荐)** 确保所有异常（包括受检异常）都能触发回滚。
-   **`readOnly = true`**: 对于查询方法，设置为只读可以优化数据库驱动性能。
-   **`propagation`**: 定义事务如何流流向。`REQUIRED` 表示如果当前没有事务就新建一个，如果有就加入。

---

## 典型用法 (Typical Usage)

### 正确的 Service 示例
```java
@Service
public class OrderService {
    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(Order order) {
        orderMapper.insert(order);       // 1. 扣钱
        stockMapper.reduce(order.id);   // 2. 减库存
        // 若此处抛出异常，1 和 2 都会自动撤销
    }
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 **“事务失效”的三大坑**：
1.  **自调用 (Self-invocation)**: 类内部 A 方法调用 B 方法，即使 B 有注解也不会生效，因为没走代理。
2.  **非 public 方法**: 注解标注在 `private` 上无效。
3.  **异常被吞**: 如果你在方法里 `try-catch` 了异常却没有重新抛出 `RuntimeException`，Spring 会以为你处理好了，从而照常提交。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

事务问题往往是“静默失败”，非常危险。

> **最佳实践 Prompt**:
> "我的一段 Spring Boot 代码涉及了跨两个数据库表的更新，但我发现即使第二步报错了，第一步的数据依然存进去了。
> 1. 请帮我检查我的 `@Transactional` 注解是否标注在了正确的方法和可见性上。
> 2. 请检查是否存在『自调用』或『异常捕获后未抛出』的情况。
> 3. 请生成一套使用 `rollbackFor` 的标准代码，并说明如何利用单元测试来断言事务的回滚行为。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Spring Docs: Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html) - 官方权威指南。
2. [Baeldung: Transactions with Spring and JPA](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring) - 实战配置技巧。
3. [Understanding AOP Proxying](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html) - 理解事务失效的底层逻辑。
