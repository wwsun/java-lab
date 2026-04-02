# 08-Java 集合框架映射 (Collections Framework Mapping)

作为 Node.js 开发者，处理数据集合是你最熟悉的工作。Java 的集合框架非常庞大，但初期你只需要掌握这三个核心：**List**, **Set**, **Map**。

## 1. 核心对应关系

| JS / TS | Java 接口 | 核心实现 (Implementation) | 关键特性 |
| --- | --- | --- | --- |
| `Array` / `[]` | `List<T>` | **`ArrayList<T>`** | 有序，可重复，支持索引访问 (`get(i)`) |
| `Set` | `Set<T>` | **`HashSet<T>`** | **不可重复**，无序 (存储顺序不等于插入顺序) |
| `Map` / `{}` | `Map<K, V>` | **`HashMap<K, V>`** | 键值对映射，Key 不可重复 |

## 2. 为什么 Java 需要这么多实现？

在 JS 中，`Array` 内部是一个高度优化的动态结构。在 Java 中，为了极致性能，针对不同场景提供了细分：

### List (列表)
- **`ArrayList`** (默认首选)：基于数组实现。适合：查询多，增删（尤其在中间）相对较少。
- **`LinkedList`**：基于链表实现。适合：频繁在头部或尾部增删元素。

### Set (集合)
- **`HashSet`** (默认首选)：基于哈希表。查找极快，但不保证顺序。
- **`TreeSet`**：基于红黑树。元素会**自动排序** (按自然顺序或指定比较器)。

### Map (字典)
- **`HashMap`** (默认首选)：查找/插入平均耗时 O(1)。
- **`LinkedHashMap`**：会记住插入的顺序 (类似于 JS 的新版 `Map`)。
- **`TreeMap`**：Key 会**自动排序**。

## 3. 常见操作 API 对比

| 操作 | JavaScript | Java (List/Map) |
| --- | --- | --- |
| 添加 | `arr.push(v)` | `list.add(v)` |
| 获取 | `arr[i]` | `list.get(i)` |
| 包含 | `arr.includes(v)` | `list.contains(v)` |
| Map 设置 | `map.set(k, v)` / `obj[k] = v` | `map.put(k, v)` |
| Map 获取 | `map.get(k)` / `obj[k]` | `map.get(k)` |
| 大小 | `arr.length` / `map.size` | `list.size()` / `map.size()` |

---
**提示**：在 Java 中，集合只能存储**对象**。如果你需要存 `int`，Java 会自动将其“装箱（Boxing）”为 `Integer` 对象。
