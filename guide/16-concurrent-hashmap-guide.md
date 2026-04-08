# 16 - 线程安全集合：ConcurrentHashMap 实战指南

## 核心心智映射 (Core Mental Mapping)

如果你习惯了 Node.js 的单线程环境，处理 `Map` 就像喝水一样自然。但在 Java 的多核并发环境下，普通的 `HashMap` 如果被多个线程同时操作，会发生极其隐蔽且致命的错误。

| 特性 | HashMap | Hashtable | ConcurrentHashMap | 心智映射 |
| :--- | :--- | :--- | :--- | :--- |
| **线程安全** | ❌ 不安全 | ✅ 安全 | ✅ 安全 | 现代并发开发的正道 |
| **锁粒度** | 无锁 | **全表大锁** | **分段锁/节点锁** | 只锁住正在操作的那个“坑位” |
| **执行效率** | 🚀 极快 | 🐢 极慢 | ⚡️ 很快 | 高并发下的吞吐量之王 |
| **状态** | 开发首选 (单线程) | **已废弃** | **并发场景必备** | 优先选择现代化的同步方案 |

---

## 概念解释 (Conceptual Explanation)

### 1. 为什么 HashMap 在并发下会“翻车”？
-   **数据丢失**: 线程 A 和 B 同时在同一个位置插入，一个会覆盖另一个。
-   **Size 不准**: 计数器不是原子的。
-   **死循环**: 在旧版 JDK 中，并发扩容可能导致链表形成环。

### 2. 什么是原子复合操作？
在并发环境下，“先判断再执行”是万恶之源。
-   **❌ 方案**: `if (!map.containsKey(key)) { map.put(key, val); }` —— 在 if 执行完的瞬间，别的线程可能已经存进去了。
-   **✅ 方案**: `map.putIfAbsent(key, val)` —— 这个操作在 JVM 底层是原子的，要么成功，要么失败，没有中间态。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心原子方法
-   **`putIfAbsent(K, V)`**: 只有 key 不存在时才放入。
-   **`computeIfAbsent(K, Function)`**: **(最推荐)**。如果不存在，则运行函数计算值并放入。常用于初始化缓存列表。
-   **`remove(K, V)`**: 只有当 key 对应的值确实是 V 时才删除。
-   **`replace(K, oldV, newV)`**: 只有当值是 oldV 时才替换为 newV（CAS 思想）。

---

## 典型用法 (Typical Usage)

### 场景：实现一个简单的本地缓存
```java
public User getUser(String id) {
    // 自动实现：查缓存 -> 缺失则查库 -> 存回缓存 -> 返回
    return cache.computeIfAbsent(id, key -> db.findUserById(key));
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `ConcurrentHashMap` 的“细粒度锁”：
它不再锁住整个 Map，而是只锁定当前正在操作的那个 **桶 (Node)**。这就像一个大型停车场，只有当你把车停入某一个具体车位时，该车位才会被占用，而其他车位可以并行停车。这种设计允许成百上千个线程同时并发读写，而性能几乎不受损。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

手动处理并发计数或缓存很容易写出 Race Condition。

> **最佳实践 Prompt**:
> "我需要统计 100 个线程同时处理任务时，每个关键词出现的频率。
> 1. 请帮我使用 `ConcurrentHashMap` 配合 `compute` 或 `merge` 方法实现原子的计数累加。
> 2. 请指出为什么传统的 `get + put` 写法在这段程序里会导致计数丢失。
> 3. 请说明如何在遍历该 Map 时安全地删除频率低于 5 的项。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Internal Working of ConcurrentHashMap](https://www.baeldung.com/java-concurrent-hashmap) - 深入底层数据结构。
2. [Java Concurrency in Practice - Chapter 5](https://jcip.net/) - 详细讲解了并发容器的设计初衷。
