---
title: "Redis 缓存大考：穿透、击穿与雪崩"
description: "理解和应对大型服务端系统最常见的三种缓存失效场景"
---

# 08 - 缓存问题与解决方案：穿透、击穿与雪崩

## 核心心智映射

在 Node.js 中，为了提升性能，我们经常会在 Express/Koa 路由里加一层 Redis 缓存：先 `redis.get(key)`，如果没有，再去 `db.collection.findOne()`，然后回写 Redis。

但是在高并发场景下，这种朴素的写法会遇到三个经典问题（Java 和 Node.js 都会遇到，因为这是架构层面的问题）：

| 缓存问题 | Node.js 场景脑补 | 核心特征 |
| :--- | :--- | :--- |
| **缓存穿透** (Penetration) | 黑客狂刷 `GET /api/users/-1`，Redis 查不到，Mongo 也查不到，请求全砸在 Mongo 上。 | **数据根本不存在**。缓存和 DB 都没命中。 |
| **缓存击穿** (Breakdown) | 微博热搜数据过期的一瞬间，10万个并发请求同时涌入，Redis 没数据，全部去查 DB。 | **热点数据过期**。单一热 Key 失效引发的并发查询。 |
| **缓存雪崩** (Avalanche) | 晚上12点，大量昨天定时设置的缓存同时过期，或者 Redis 宕机，DB 被海量请求打挂。 | **大面积 Key 集中失效** 或 缓存集群宕机。 |

---

## 概念解释与解决方案

## 1. 缓存穿透 (Cache Penetration)

现象：恶意请求或者代码 Bug 导致大量请求查询一个**一定不存在**的数据。因为 DB 里也没这个数据，所以永远不会被写进缓存。结果就是每一次请求都要去 DB 走一圈，缓存形同虚设。

**规避思路：**
- **缓存空值 (Cache Null / Empty Object)**：如果 DB 查不到，也把这个 Key 写进 Redis（比如存一条特殊字符串 `"EMPTY"`），并设置一个较短的过期时间（TTL）。
- **布隆过滤器 (Bloom Filter)**：在请求到达 Redis 之前，先经过布隆过滤器。如果布隆过滤器说不存，那肯定不存在，直接拒绝请求。（注：布隆过滤器有极小的误判率，说存在的不一定真的存在）。

## 2. 缓存击穿 (Cache Breakdown)

现象：一个极其热门的 Key（比如秒杀商品、热搜新闻），在不停地抗击着巨大的并发量。当这个 Key 突然过期（TTL 到期）的这一个极短的瞬间，持续的大并发请求由于在缓存中拿不到数据，瞬间全部穿透到 DB 去查询数据并试图重建缓存，导致 DB 压力骤增甚至宕机。

**规避思路：**
- **互斥锁 (Mutex Lock)**：在缓存未能命中的时候，不是所有线程都去查 DB。而是必须要先获取一个锁（比如 Redisson 分布式锁，或者单机下的 `synchronized` 代码块）。拿到锁的那个线程去查 DB 写缓存，其他没拿到锁的线程就睡眠重试。
- **逻辑过期**：热点 Key 甚至可以不设置 TTL。我们在 Value 里面塞一个字段叫 `expireAt`，这叫物理不过期、逻辑过期。每次拿数据出来对比一下这时间，如果过期了，当前线程返回旧数据，然后**开一个异步线程**去 DB 捞新数据更新缓存。

> 💡 **Node.js 对比**：在 Node.js 中，由于是单线程事件循环，我们有时会用一个全局 Map 把当前正在 Fetch DB 的 Promise 存起来以应对并发击穿，后续请求碰到了如果发现已经在 Fetch，就拿同一个 Promise 来 `await`。而在 Java 中，因为真正的多并发线程，我们需要显式的**锁机制**。

## 3. 缓存雪崩 (Cache Avalanche)

现象：如果在某个时间段，缓存**集中过期失效**，或者 **Redis 节点直接宕机**。那么一大波原本应该由 Redis 挡住的流量，全部打到了数据库上。

**规避思路：**
- **TTL 随机抖动 (Random Jitter)**：给不同的 Key 设置过期时间时，加上一个随机的偏移量（例如 `ttl + Math.random() * 60` 秒），让它们不要在同一秒扎堆死亡。
- **高可用与限流**：Redis 部署集群（Sentinel / Cluster）防宕机；系统层面加一层 Sentinel/Resilience4j 做限流降级，保底。

---

## 关键语法与最佳实践

在 Spring Boot 中，框架已经帮我们把很多解决思路封装到了开箱即用的注解里。

如果你使用 `@Cacheable`（这也是我们上一节刚学过的），它实际上默认支持了防御缓存穿透和缓存击穿的某种层度的特性！

### 配置演示：防御穿透与击穿

**防止击穿：`sync = true`**
```java
// 使用 sync = true。
// 它表示当多个线程同时试图加载这同一个 Key 时，底层会加本地锁。
// 只有一个线程能去执行真正的 getBookById(id)（即查库），其他线程等待。
// 这极大降低了单个热 Key 瞬间击穿 DB 的概率。
@Cacheable(value = "books", key = "#id", sync = true)
public Book getBookById(Long id) {
    // 模拟耗时查库操作
    return bookMapper.selectById(id);
}
```

**防止穿透：`cache-null-values: true`**
Spring Boot 默认在 Redis Cache 配置中是开启缓存空值的，即允许方法返回 null 时，将 null 作为值缓存（通常在 Redis 中会由序列化器转为类似于一份特殊的空字节），如果想要显式开启或确认，在 `application.yml` 里：
```yaml
spring:
  cache:
    redis:
      cache-null-values: true  # 允许缓存空值，防御缓存穿透
```

---

## 反模式 (Anti-Patterns)

1. **所有的缓存设置一模一样的 TTL**
   *后果*：典型的雪崩前兆。如果 10万条商品信息在一批次导入中被设置了同样的一小时过期，一小时后 DB 直接崩溃。
2. **在缓存未命中时肆意开异步线程去重建**
   *后果*：Java 的多线程创建和销毁也是有成本的（除非用了虚拟线程），如果在高并发下疯狂 new Thread 去查库，不仅把数据库打爆，应用 JVM 自身的内存也会迅速 OOM。一定要用线程池或者干脆排队加锁。
3. **遇到查不到数据的 id 直接 throw new Exception() 且不处理**
   *后果*：导致方法的执行被中断，Spring 的注解可能不会将 null 存进缓存去防御下一次相同的恶意查询（穿透）。

---

## AI 辅助开发实战建议

当你在实际开发复杂的高可用接口时，千万不要自己手写所有的 Redisson 分布式锁来防御击穿，容易漏写 `try-finally` 造成死锁。可以利用 AI 帮你生成样板：

**使用 Prompt 示例：**
> "我需要在 Spring Boot 项目中实现一个缓存查询方法，为了彻底防止缓存查询击穿（因为应用是分布式的，sync=true可能只防单机），请帮我使用 Redisson 的 RLock 写一个标准的高并发查询模板代码，要包含正确的 double-check-locking (双重检查加锁) 和 try-finally 释放锁的机制。"

---

## 扩展阅读

1. [Redisson 分布式锁官方文档](https://github.com/redisson/redisson/wiki/8.-distributed-locks-and-synchronizers) (极其重要，面试考点也是工程利器)
2. 布隆过滤器(Bloom Filter)的数学原理与 Guava/Redisson 中的应用
