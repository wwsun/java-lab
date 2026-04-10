# 07 - Spring Cache 与 Redis 实战指南

## 核心心智映射

在 Node.js/TypeScript 生态中，缓存通常是手动管理的（如使用 `ioredis` 手写 `get/set` 逻辑，或使用 `memoizee` 等库）。而在 Java Spring 生态中，我们倾向于使用 **AOP（面向切面编程）** 提供的 **声明式缓存 (Declarative Caching)**。

| 概念         | Node.js (典型做法)               | Java Spring Cache                |
| :----------- | :------------------------------- | :------------------------------- |
| **位置**     | 业务逻辑代码中嵌入 `redis.get()` | 仅在方法上添加 `@Cacheable` 注解 |
| **缓存清理** | 手动执行 `redis.del(key)`        | 使用 `@CacheEvict` 注解          |
| **实现原理** | 显式编写逻辑                     | 代理模式 (Proxy) 自动处理        |
| **解耦度**   | 低（业务代码耦合了缓存逻辑）     | 高（业务代码只关注数据获取）     |

## 概念解释

Spring Cache 是一个抽象层，它并不直接提供缓存存储，而是通过 `CacheManager` 接口对接不同的存储介质（如 Redis, Caffeine, Ehcache）。

### 核心注解

1.  **`@Cacheable`**：在方法执行前先查缓存。命中则返回；未命中则执行方法，并将结果存入缓存。
2.  **`@CacheEvict`**：清理缓存。通常用于 `update` 或 `delete` 方法。
3.  **`@CachePut`**：保证方法被调用，并将结果更新到缓存（常用于更新操作）。
4.  **`@EnableCaching`**：配置类头部的开关，开启注解驱动的缓存。

## 关键语法与 API

### 配置项 (RedisCacheConfiguration)

在 `RedisConfig` 中配置，关键点在于**序列化方式**。默认的 JDK 序列化会导致 Redis 中的数据变成不可读的乱码，建议统一使用 JSON 序列化。

```java
@Bean
public CacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // 设置全局过期时间
            .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    return RedisCacheManager.builder(factory).cacheDefaults(config).build();
}
```

### 注解参数详解

- **`value/cacheNames`**：缓存组件的名字（类似于 Redis Key 的前缀）。
- **`key`**：缓存的具体 Key，支持 SpEL 表达式（如 `#id` 表示取参数 id）。
- **`unless`**：条件排除。`#result == null` 表示如果结果为空则不缓存。避免 缓存穿透。

## 典型用法

### 查询缓存

```java
@Cacheable(value = "book", key = "#id", unless = "#result == null")
public Optional<Book> getBookById(Long id) {
    return Optional.ofNullable(bookMapper.selectById(id));
}
```

#### 代码执行流程解析

当你调用 `getBookById(1L)` 时，Spring 会通过 **AOP 代理机制** 拦截该请求，具体流程如下：

1.  **拦截并生成 Key**：Spring 拦截到请求，根据 `value` ("book") 和 `key` ("#id") 生成最终的缓存 Key（默认格式为 `book::1`）。
2.  **查询缓存 (Pre-check)**：在 Redis 中查找该 Key。
3.  **命中处理 (Cache Hit)**：如果 Redis 中存在该数据，Spring 会将其直接反序列化为 `Optional<Book>` 对象并返回。**此时方法体内的逻辑（如 DB 查询）将被完全跳过**。
4.  **失效处理 (Cache Miss)**：如果 Redis 中不存在该数据：
    -   **执行方法体**：调用实际的业务逻辑（查询数据库）。
    -   **条件检查 (unless)**：获取方法返回值，检查 `unless = "#result == null"` 条件是否成立。
    -   **回填缓存**：如果返回值不为 null（且满足其他条件），Spring 将该结果以 JSON 格式存入 Redis。
5.  **返回结果**：将数据返回给调用方。

### 更新时清理缓存 (Cache Aside 模式)

```java
@CacheEvict(value = "book", key = "#id")
public Book updateBook(Long id, Book book) {
    bookMapper.updateById(book);
    return book;
}
```

#### 代码执行流程解析

当你调用 `updateBook(1L, book)` 时，Spring 的执行逻辑如下：

1.  **执行业务逻辑**：首先执行方法体内的代码，即调用数据库更新操作。
2.  **成功校验**：如果方法执行过程中抛出异常，**默认情况下缓存不会被清理**（除非配置了 `beforeInvocation = true`）。
3.  **生成 Key**：方法成功执行后，Spring 根据 `@CacheEvict` 中的 `value` 和 `key` 生成需要清理的 Key（如 `book::1`）。
4.  **执行清理 (Evict)**：向 Redis 发送 `DEL` 命令，删除对应的 Key。
5.  **完成**：此后，当下一次调用查询接口 `getBookById(1L)` 时，由于缓存已失效，系统会自动触发数据库查询并重新回填缓存，从而保证了**数据的一致性**。

## 最佳实践与反模式

### ✅ 最佳实践

- **统一序列化**：务必使用 JSON 序列化，方便运维排查和跨系统共享。
- **设置过期时间**：避免缓存无限堆积导致内存溢出。
- **防止缓存击穿**：对于热点数据，考虑使用不同的过期策略。

### ❌ 反模式

- **内部方法调用**：Spring Cache 基于 AOP 代理。若类 A 内部 `methodA` 调用 `methodB`（带缓存注解），缓存将**不会生效**。
- **缓存大数据对象**：尽量只缓存必要的 DTO，避免 Redis 网络带宽开销过大。

## AI 辅助开发实战建议

当你需要为复杂的业务逻辑添加缓存时，可以这样问 AI Agent：

> "请为这个服务类的查询方法添加 Spring Cache。要求：Key 包含用户 ID 前缀，结果为空时不缓存，且在所有涉及数据变更的方法上自动清理相关的缓存 Key。"

## 扩展阅读

1.  **缓存穿透、击穿、雪崩**：这是面试高频考点，请务必理解其含义及 Spring Cache 的应对方案。
2.  **Caffeine 缓存**：如果对性能有极致要求且数据量不大，可以使用本地缓存 Caffeine。
