# 06 - 缓存模式实践（Cache-Aside Pattern）

## 核心心智映射 (Core Mental Mapping)

在 Node.js 中，如果你使用 `ioredis` 或 `redis-om` 手动管理缓存，你的代码通常长这样：

```typescript
// Node.js (ioredis) 典型逻辑
async function getBook(id: string) {
  const cacheKey = `book:${id}`;
  const cached = await redis.get(cacheKey);
  if (cached) return JSON.parse(cached); // 命中命中！

  const book = await db.book.findUnique({ where: { id } });
  if (book) {
    await redis.set(cacheKey, JSON.stringify(book), 'EX', 1800); // 回填缓存
  }
  return book;
}
```

在 Java Spring Boot 中，手动缓存的逻辑本质上完全一致。我们使用 `RedisTemplate` 代替 `redis` 对象，并利用 Spring 的自动配置来处理连接池。

| 概念 | Node.js (ioredis) | Java (Spring Data Redis) |
| :--- | :--- | :--- |
| **操作对象** | `new Redis()` | `RedisTemplate<String, Object>` |
| **反序列化** | `JSON.parse()` | 自动完成 (基于 Jackson 配置) |
| **过期时间** | `'EX', 1800` | `Duration.ofMinutes(30)` |
| **Key 拼接** | `${prefix}:${id}` | `String.format("%s:%s", prefix, id)` |

---

## 概念解释 (Conceptual Explanation)

### 什么是 Cache-Aside Pattern？
**旁路缓存 (Cache-Aside)** 是 Web 开发中最经典的缓存策略。它的核心思想是：**应用程序负责维护缓存与数据库的一致性**。

1.  **读请求**：
    - 先看缓存，有则返回（Cache Hit）。
    - 缓存没有，看数据库（Cache Miss）。
    - 数据库有，**回填缓存**并返回。
2.  **写请求**：
    - 先更新数据库。
    - **删除缓存**（注意：是删除，不是更新。因为删除逻辑简单且能避免并发更新带来的脏数据）。

### 为什么要"删"而不是"改"缓存？
在高并发现场，如果两个线程同时更新，由于网络抖动，数据库更新顺序和缓存更新顺序可能不一致，导致缓存里存的是旧值（脏数据）。**删除**操作具有幂等性，下次读取时自然会触发回填，保证了最终一致性。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### `RedisTemplate<K, V>`
这是 Spring 操作 Redis 的核心类。

- `opsForValue()`：操作简单的 Key-Value (String 类型)。
- `opsForHash()`：操作哈希表。
- `delete(key)`：删除某个 Key。
- `expire(key, timeout, unit)`：设置过期时间。

### 核心代码片段

```java
// 写入并设置过期时间
redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES);

// 读取
Object val = redisTemplate.opsForValue().get(key);

// 删除
redisTemplate.delete(key);
```

---

## 典型用法 (Typical Usage)

标准的 `Service` 层缓存模板：

```java
public T getById(Long id) {
    String key = CACHE_PREFIX + id;
    // 1. 尝试从缓存读取
    T cached = (T) redisTemplate.opsForValue().get(key);
    if (cached != null) return cached;

    // 2. 缓存缺失，查数据库
    T dbResult = mapper.selectById(id);
    if (dbResult != null) {
        // 3. 回填缓存
        redisTemplate.opsForValue().set(key, dbResult, 30, TimeUnit.MINUTES);
    }
    return dbResult;
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

在本项目的 `BookServiceImpl.java` 中，我们实现了以下逻辑：

1.  **定义前缀**：`private static final String CACHE_BOOK_KEY = "book:cache:";` 保证 Key 的命名空间独立。
2.  **防御缓存穿透**：当数据库也查不到数据时，我们依然设置一个特殊的标志或短时间的空缓存（或者在本示例中简单返回 `Optional.empty()`），防止非法 ID 频繁透传到数据库。
3.  **双写一致性**：在 `updateBook` 和 `deleteBook` 方法中，我们坚持**先改数据库，后删缓存**。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

在让 Agent 帮你写缓存代码时，可以使用以下 Prompt 提高成功率：

> "请帮我在 `XxxService` 的 `getById` 方法中加入 Cache-Aside 逻辑。
> 1. 使用 `RedisTemplate<String, Object>`。
> 2. 缓存 Key 格式为 `prefix:id`。
> 3. 缓存过期时间设置为 30 分钟。
> 4. 确保在 `update` 和 `delete` 方法中正确清理缓存。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Redis 官网：Caching with Redis](https://redis.io/docs/manual/client-side-caching/) - 官方关于缓存最佳实践的说明。
2. [Baeldung: Guide to Redis with Spring Boot](https://www.baeldung.com/spring-data-redis-tutorial) - 深入了解 `RedisTemplate` 的高级用法。
3. [小林 coding：缓存一致性](https://xiaolincoding.com/database/redis/cache_consistency.html) - 非常透彻地解释了为什么先更新数据库再删除缓存是较优解。
