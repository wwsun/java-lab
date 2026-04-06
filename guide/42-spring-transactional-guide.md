# Spring Boot 中的事务管理 (@Transactional)

在 Node.js/TypeScript 生态中，你可能习惯了使用 Knex 的 `knex.transaction(async (trx) => { ... })` 或 TypeORM 的 `queryRunner` 来手动控制事务。

在 Spring Boot 中，我们通常使用 **声明式事务管理 (Declarative Transaction Management)**，即通过一个注解 `@Transactional` 就能让 Spring 框架自动帮你处理事务的开启、提交和回滚。

## 1. 核心概念：ACID

在进入 Spring 具体语法前，我们先复习一下数据库事务的核心特性（面试高频）：

- **原子性 (Atomicity)**：事务中的所有操作要么全部成功，要么全部失败回滚。类比 JS 中的 `Promise.all`（但更严格，是底层存储层面的保证）。
- **一致性 (Consistency)**：事务执行前后，数据库必须处于一致的状态。
- **隔离性 (Isolation)**：并发执行的事务之间互不干扰。
- **持久性 (Durability)**：一旦事务提交，其结果就是永久性的。

---

## 2. @Transactional 注解

在 Spring Boot 应用中，我们通常将 `@Transactional` 标注在 **Service 层** 的方法或类上。

### 2.1 基本用法

```java
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private InventoryMapper inventoryMapper;

    @Transactional
    public void createOrder(Order order) {
        // 1. 插入订单表
        orderMapper.insert(order);
        
        // 2. 扣减库存 (如果这里抛出异常，步骤1的操作也会自动回滚)
        inventoryMapper.reduceStock(order.getProductId(), order.getQuantity());
        
        // 3. 如果运行到这里没有异常，Spring 会自动提交事务
    }
}
```

### 2.2 常用参数

| 参数 | 说明 | 推荐做法 |
| :--- | :--- | :--- |
| `propagation` | **传播行为**。定义当一个事务方法调用另一个事务方法时，事务应该如何传播。 | 默认 `REQUIRED` 即可覆盖 90% 场景。 |
| `isolation` | **隔离级别**。处理并发事务时的可见性问题。 | 保持默认（通常由数据库决定，MySQL 默认为 Repeatable Read）。 |
| `rollbackFor` | **回滚策略**。指定哪些异常触发回滚。 | **注意：** Spring 默认只对 `RuntimeException` 回滚。建议显式指定：`@Transactional(rollbackFor = Exception.class)`。 |
| `readOnly` | **只读标志**。设置为 true 可优化查询性能（底层驱动优化）。 | 对于 `find/get` 等纯查询方法，建议设置 `readOnly = true`。 |

---

## 3. Node.js vs. Java 心智模型映射

| 特性 | Node.js (Knex/TypeORM) | Spring Boot (Java) |
| :--- | :--- | :--- |
| **控制方式** | 显式/编程式 (`trx.commit()`, `trx.rollback()`) | 声明式 (`@Transactional`)，框架自动处理 |
| **作用域** | 通常是通过回调函数的 `trx` 对象传递 | 通过 Spring AOP 代理拦截方法调用 |
| **回滚触发** | `try-catch` 中手动调用 `rollback()` | 方法抛出异常时自动触发 |

---

## 4. 关键坑点：为什么我的事务没生效？

这是 Java 初学者最容易遇到的"幻觉"场景。

### 4.1 自调用 (Self-invocation)
Spring 通过 **代理模式 (Proxy)** 实现事务。当你标注了 `@Transactional`，Spring 会在 Bean 初始化时生成一个代理类。

**错误示范：**
```java
@Service
public class MyService {

    public void methodA() {
        // ❌ 这里直接调用 methodB，事务将不会生效！
        // 因为这是类内部调用，没有走 Spring 的代理对象。
        methodB();
    }

    @Transactional
    public void methodB() {
        // ... 数据库操作
    }
}
```
**解决方案：** 事务方法必须由外部通过注入的 Bean 调用，或者将 `methodB` 抽离到另一个 Service。

### 4.2 访问权限
`@Transactional` 只能标注在 `public` 方法上。如果标注在 `private` 或 `protected` 方法上，Spring 会检测不到，且**不报错**，只是事务静默失效。

### 4.3 捕获了异常却没抛出
如果你在方法内部使用了 `try-catch` 并消除了异常（没有重新 throw `RuntimeException`），Spring 会认为方法执行成功，从而执行 `commit` 而不是 `rollback`。

---

## 5. 最佳实践建议

1. **粒度控制**：不要在 Controller 层加事务，应在 Service 层控制。
2. **异常声明**：始终加上 `rollbackFor = Exception.class`，确保 Checked Exception 也能触发回滚。
3. **只读优化**：对于不需要写操作的 Service 方法，使用 `@Transactional(readOnly = true)`，这在主从架构中能有效利用从库压力。
4. **长事务警告**：由于事务会占用数据库连接池，避免在 `@Transactional` 方法内执行耗时的网络请求或文件读写。

---

## 扩展阅读

1. [Spring 官方文档 - 事务管理](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
2. [Baeldung - Transactions with Spring and JPA](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring) (同样适用于 MyBatis)
3. [理解 AOP 代理的工作原理](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html)
