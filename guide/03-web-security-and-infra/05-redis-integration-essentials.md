# 05 - Redis 集成核心指南：从 Node.js 到 Java

作为资深的 Node.js 开发者，你可能已经习惯了 `ioredis` 的链式调用和 `JSON.stringify`。在 Spring Boot 中，Redis 的集成方式更加契合其"模板模式 (Template Pattern)"和"声明式编程 (Declarative Programming)"。

## 0. Redis 核心数据结构与场景

在动手写 Java 代码前，我们先对齐一下 Redis 的底层武器库。

### 5 大基本数据结构

| 数据结构 | Node.js 对标 | 核心指令 | 典型场景简述 |
| :--- | :--- | :--- | :--- |
| **String** | `string / number` | `SET`, `GET`, `INCR` | 缓存 JSON、分布式锁、计数器。 |
| **Hash** | `Object / Map` | `HSET`, `HGET` | 存储对象、配置项。 |
| **List** | `Array` | `LPUSH`, `RPOP` | 简单队列、最新动态列表。 |
| **Set** | `Set` | `SADD`, `SISMEMBER` | 去重、交集（共同好友）。 |
| **ZSet** | `N/A` | `ZADD`, `ZRANGE` | 排行榜、延迟队列。 |

### 深度解析：什么时候该用什么？

#### 1. String (字符串) —— 万金油
*   **缓存 POJO (JSON)**：最常见的用法。将 Java 对象序列化为 JSON 存入，设置 TTL。
    *   *Node.js 对标*：`await redis.set(key, JSON.stringify(obj))`
*   **分布式锁**：利用 `SET key value NX PX 30000` 实现互斥。
    *   *Java 实践*：使用 `StringRedisTemplate.opsForValue().setIfAbsent()`。
*   **原子计数器**：秒杀场景下的库存扣减、接口防刷限流。

#### 2. Hash (哈希) —— 对象的容器
*   **存储复杂对象**：如果你只想更新对象的某个字段（如更新用户的 `lastLoginTime`），用 Hash 比读出整个 JSON 再写回更高效。
*   **购物车/配置中心**：`key` 是用户 ID，`field` 是商品 ID，`value` 是数量。

#### 3. List (列表) —— 异步的桥梁
*   **轻量级消息队列**：通过 `LPUSH` 和 `BRPOP` 实现简单的生产者-消费者模型。
*   **时间线/最新列表**：存储最近的 100 条操作日志或评论。

#### 4. Set (集合) —— 关系的大师
*   **点赞/签到**：去重且快速检查某个 ID 是否在集合中 (`SISMEMBER`)。
*   **社交计算**：利用 `SINTER` (交集) 计算共同好友，`SDIFF` (差集) 寻找可能认识的人。

#### 5. ZSet (有序集合) —— 排名的利器
*   **动态排行榜**：自动根据 `score` (分数) 排序。适合游戏积分榜、热搜榜。
*   **延迟任务队列**：将 `score` 设为执行的时间戳，定时轮询超过当前时间的任务。

### 典型应用模式 (Patterns)
... (保持原有内容)

## 1. 核心映射 (The Mapping)

### 从 `ioredis` 到 `RedisTemplate`

在 Node.js 中，你通常直接操作客户端对象：

```javascript
// Node.js (ioredis)
const redis = new Redis();
await redis.set('user:1', JSON.stringify({ name: 'wwsun' }), 'EX', 3600);
```

在 Java 中，我们将这个操作解耦为 **Template** 和 **Operations**：

```java
// Java (Spring Data Redis)
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// opsForValue() 对齐的是 Redis 的 String 类型操作 (SET/GET)
redisTemplate.opsForValue().set("user:1", userObject, Duration.ofHours(1));
```

| Redis 类型 | Java Operations 对象 | 常用方法 |
| :--- | :--- | :--- |
| String | `opsForValue()` | `set`, `get`, `increment` |
| Hash | `opsForHash()` | `put`, `get`, `entries` |
| List | `opsForList()` | `leftPush`, `rightPop` |
| Set | `opsForSet()` | `add`, `members`, `intersect` |
| ZSet | `opsForZSet()` | `add`, `rangeByScore` |

### 序列化：为什么会有乱码？

Node.js 开发者最常遇到的坑是：在 Java 里存了一个 `key: "foo"`，结果在 `redis-cli` 里看到的是 `\xac\xed\x00\x05t\x00\x03foo`。

这是因为 Spring 默认使用 **JdkSerializationRedisSerializer**，它会将对象序列化为二进制。
**解决方案**：我们在配置类中显式指定 **StringRedisSerializer** (用于 Key) 和 **Jackson2JsonRedisSerializer** (用于 Value)。

| 序列化器 | 描述 | Node.js 对等物 |
| :--- | :--- | :--- |
| `StringRedisSerializer` | 原始字符串存储 | `Buffer.from(str)` |
| `Jackson2JsonRedisSerializer` | JSON 字符串存储 | `JSON.stringify(obj)` |

## 2. 工具类的选择

Spring 提供了两个开箱即用的 Bean：

1.  **StringRedisTemplate**：专门用于处理 `String` 类型的 Key-Value。它的序列化器已经全部预设为 String。
    *   *场景*：验证码存储、简单的 Flag 标记、分布式锁。
2.  **RedisTemplate<K, V>**：通用的模板库。我们需要手动配置它，以便支持复杂的 POJO。
    *   *场景*：缓存用户信息、存储任务对象等。

## 3. 声明式缓存：@Cacheable

这类似于在 TypeScript 中使用高阶函数或装饰器来包裹逻辑。

```java
@Cacheable(value = "users", key = "#id")
public User getUserById(Long id) {
    // 如果缓存命中，则直接返回；否则执行数据库查询并自动回填 Redis
    return userMapper.selectById(id);
}
```

## 4. 开始集成

我们将执行以下步骤：
1.  **添加依赖**：`spring-boot-starter-data-redis`。
2.  **配置环境**：`docker-compose` 启动 Redis。
3.  **编写配置类**：解决序列化问题。

---
**📚 准备好了吗？** 请确认实施计划，我们将立即开始。
