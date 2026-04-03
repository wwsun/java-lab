# 16-线程安全集合：ConcurrentHashMap 实战指南

作为 Node.js 开发者，您可能习惯于直接在 `Map` 中存储缓存。但在 Java 的多核多线程环境下，普通的 `HashMap` 如果被多个线程同时操作，会发生非常隐蔽的错误。

## 1. 三种容器的演进史

| 容器名 | 安全性 | 性能 | 机制 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| **`HashMap`** | ❌ 不安全 | 🚀 极快 | 无任何同步机制 | 现代开发的首选（单线程下） |
| **`Hashtable`** | ✅ 安全 | 🐢 极慢 | 在每个方法上加 `synchronized` 大锁 | **已废弃**，请勿使用 |
| **`ConcurrentHashMap`** | ✅ 安全 | ⚡️ 很快 | 分段锁 (JDK 7) / CAS + 节点锁 (JDK 8) | **现代并发开发的正道** |

## 2. 为什么不用 HashMap？ (复现脏写)

在多线程环境下，`HashMap.put()` 涉及到对链表或红黑树的操作。如果两个线程同时扩容或在同一个桶 (Bucket) 插入数据，会导致：
- **数据丢失**：一个线程的写入覆盖了另一个。
- **Size 不准**：计数器非原子累加。
- **甚至死机**：在旧版 JDK 中，并发扩容甚至可能导致 CPU 100% (死循环)。

## 3. ConcurrentHashMap 的核心绝招

### A. 锁粒度极细
它不再锁住整个 Map，而是只锁定当前正在操作的那个 **桶 (Node)**。这就像一个大型停车场，只有当你把车停入某一个具体车位时，该车位才会被占用，而其他车位可以并行停车。

### B. 常用的原子操作方法 (必会)

如果你先 `get` 判断是否存在，再 `put` 写入，这在并发下依然是不安全的。你应该使用 `ConcurrentHashMap` 提供的原子方法：

1.  **`putIfAbsent(key, value)`**：如果不存在则放入。
2.  **`computeIfAbsent(key, mappingFunction)`**：如果不存在，则运行函数计算值并放入。
    - **类比**：非常适用于实现 **Cache Aside** 模式（即：读缓存，未命中则查库并更新缓存）。

## 4. 给 Node.js 开发者的建议

### ⚠️ 不要在 Loop 中改 Map

即使是 `ConcurrentHashMap`，在遍历时修改虽然不会抛出 `ConcurrentModificationException`（`HashMap` 会抛），但**逻辑可能不符合预期**，产生难以排查的 Bug。

#### 问题场景：遍历时删除过期缓存

```java
// ❌ 危险写法：遍历时直接修改（逻辑可能不完整/不一致）
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("a", 1);
map.put("b", 2);
map.put("c", 3);

for (String key : map.keySet()) {
    if (map.get(key) < 2) {
        map.remove(key); // 虽然不会报错，但在并发下可能漏删或逻辑混乱
    }
}
```

> **类比 Node.js**：这就像在 `for...of` 遍历数组时，同时调用 `array.splice()` 删除元素——你可能会跳过某些元素，或者处理到已经被删除的位置。

---

#### ✅ 最佳实践 1：使用 `entrySet().removeIf()` —— 原子批量删除

```java
// ✅ 推荐：removeIf 内部使用迭代器安全删除
map.entrySet().removeIf(entry -> entry.getValue() < 2);
```

- `removeIf` 底层使用迭代器的 `remove()`，是专门为遍历删除设计的安全方式。
- 逻辑简洁，类比 JS 的 `filter`（但这里是原地修改）。

---

#### ✅ 最佳实践 2：先收集、再操作 —— 两阶段处理

```java
// ✅ 推荐：先收集需要删除的 key，再统一删除
List<String> keysToRemove = map.entrySet().stream()
    .filter(entry -> entry.getValue() < 2)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());

keysToRemove.forEach(map::remove);
```

- **读写分离**：遍历阶段只读，修改阶段只写，逻辑清晰，不会自我干扰。
- 适合逻辑复杂、需要跨多个条件判断的场景。

---

#### ✅ 最佳实践 3：使用 `replaceAll()` 批量更新值

```java
// ❌ 危险写法：遍历时修改 value
for (String key : map.keySet()) {
    map.put(key, map.get(key) * 2); // 并发下可能覆盖其他线程的写入
}

// ✅ 推荐：replaceAll 是原子性地对每个 entry 执行函数
map.replaceAll((key, value) -> value * 2);
```

- `replaceAll` 内部对每个节点加锁，保证每次更新的原子性。

---

#### ✅ 最佳实践 4：使用 `forEach()` 只读遍历，禁止在其中写入

```java
// ✅ 遍历只用于读取/统计，不在内部修改 map
map.forEach((key, value) -> {
    System.out.println(key + " -> " + value);
    // ⚠️ 不要在这里调用 map.put() / map.remove()
});
```

---

#### 总结对比表

| 场景 | ❌ 危险写法 | ✅ 推荐写法 |
| :--- | :--- | :--- |
| 遍历时删除元素 | `for + map.remove()` | `removeIf()` 或两阶段删除 |
| 遍历时更新 value | `for + map.put()` | `replaceAll()` 或 `compute()` |
| 遍历时只读统计 | 直接 `for` | `forEach()` 只读 |
| 条件写入（不存在才插入）| `get` + `put` | `putIfAbsent()` / `computeIfAbsent()` |

---

- **优先用 `java.util.concurrent` 包**：除了 Map，还有 `CopyOnWriteArrayList` 等专门为并发设计的集合。

---
> [!TIP]
> **本周核心：** 接下来我们将通过代码演示 50 个线程同时向 `HashMap` 写入数据时发生的崩溃现场。

**参考资料**：
- [Internal Working of ConcurrentHashMap](https://www.baeldung.com/java-concurrent-hashmap)
- [How JDK 8 ConcurrentHashMap differs from its predecessors](http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/share/classes/java/util/concurrent/ConcurrentHashMap.java)
