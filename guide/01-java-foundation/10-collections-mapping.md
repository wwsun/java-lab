# 10 - Java 集合框架映射与快速选型指南

## 核心心智映射 (Core Mental Mapping)

在 Node.js 中，你可能习惯了用一个 `Array` 搞定所有列表操作，用一个字面量 `{}` 或 `Map` 搞定所有键值对。Java 的集合框架更为庞大，但初期你只需掌握“三驾马车”：

| JS / TS | Java 接口 | 核心实现 (Implementation) | 心智映射 |
| :--- | :--- | :--- | :--- |
| `Array` / `[]` | `List<T>` | **`ArrayList<T>`** | 有序，可重复，支持索引访问 |
| `Set` | `Set<T>` | **`HashSet<T>`** | **不可重复**，数学意义上的集合 |
| `Map` / `{}` | `Map<K, V>` | **`HashMap<K, V>`** | 键值对字典 |

---

## 核心选型矩阵 (Quick Selection Matrix)

在 Java 开发中，选择“接口”之后，更重要的是选择合适的“实现类”。以下是开发中最常见的选型逻辑：

### 1. List (列表) - 对应 JS Array
| 实现类 | 内部结构 | 优点 | 缺点 | 适用场景 |
| :--- | :--- | :--- | :--- | :--- |
| **ArrayList** | 动态数组 | 查询快 (O(1)) | 中间增删涉及元素移动，较慢 | **默认首选**。绝大多数业务场景。 |
| **LinkedList** | 双向链表 | 头部/尾部增删极快 | 随机访问慢 (O(n)) | 用作队列 (Queue) 或栈 (Stack) 时。 |

### 2. Set (去重集合) - 对应 JS Set
| 实现类 | 排序性 | 性能 | 适用场景 |
| :--- | :--- | :--- | :--- |
| **HashSet** | 无序 | 最高 | **默认首选**。仅需去重，不关心顺序。 |
| **LinkedHashSet** | **保持插入顺序** | 高 | 既要去重，又要按添加顺序进行展示。 |
| **TreeSet** | **自动排序** | 中 | 需要集合内部元素始终处于有序状态。 |

### 3. Map (字典) - 对应 JS Object/Map
| 实现类 | 排序性 | 性能 | 适用场景 |
| :--- | :--- | :--- | :--- |
| **HashMap** | 无序 | 最高 | **默认首选**。普通的键值对存储。 |
| **LinkedHashMap** | **保持插入顺序** | 高 | 需要按添加（Put）顺序读取数据时。 |
| **TreeMap** | **按 Key 排序** | 中 | 需要 Key 自动排序（如：按时间戳排序统计）。 |

---

## 概念解释 (Conceptual Explanation)

### 1. 接口与实现的分离 (Coding to Interface)
在 Java 中，推荐“声明接口，实例化类”：
```java
List<String> list = new ArrayList<>(); // 以后换 LinkedList 只需改这一处
```

### 2. 装箱与拆箱 (Boxing/Unboxing)
Java 集合只能存对象。如果你存 `int`，它会自动转为 `Integer` 对象。这会带来微小的性能开销，在大数据量处理时需留意。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 常用操作对照表
| 操作 | TypeScript | Java |
| :--- | :--- | :--- |
| **添加元素** | `arr.push(v)` | `list.add(v)` |
| **获取元素** | `arr[i]` | `list.get(i)` |
| **Map 设置** | `map.set(k, v)` | `map.put(k, v)` |
| **判断包含** | `arr.includes(v)` | `list.contains(v)` |
| **集合大小** | `arr.length` | `list.size()` |

---

## 典型用法 (Typical Usage)

### 1. 初始化（Java 9+ 风格）
```java
List<String> tags = List.of("Java", "Spring", "Redis"); // 注意：此方法创建的是不可变集合
Map<String, String> config = Map.of("env", "prod", "version", "1.0");
```

### 2. 传统可变集合初始化
```java
List<String> mutables = new ArrayList<>(List.of("A", "B"));
mutables.add("C"); // 允许执行
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `LinkedHashMap` 的保持顺序特性：
```java
Map<String, String> map = new LinkedHashMap<>();
map.put("Z", "Last");
map.put("A", "First");
// 打印结果始终是 Z, A。如果是 HashMap，顺序则不确定。
```
这在构建某些需要**顺序敏感**的配置项（如：拦截器链、权限规则列表）时非常关键。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

> **最佳实践 Prompt**:
> "我需要一个能按 Key 自动排序的字典结构，Key 是字符串类型的日期，Value 是金额。
> 1. 请推荐 Java 中最合适的实现类（如 TreeMap）。
> 2. 请展示如何使用 Stream API 对其 Value 进行求和。
> 3. 请说明该实现类在处理 10 万条数据时的内存表现。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Oracle: Collections Framework](https://docs.oracle.com/javase/8/docs/technotes/guides/collections/overview.html)
2. [Baeldung: Guide to Java HashMap](https://www.baeldung.com/java-hashmap)
