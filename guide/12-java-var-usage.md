# 12-Java var 使用指南：简洁与可读性的平衡

Java 10 引入了 `var`（局部变量类型推断），旨在减少 Java 传统语法的冗余感。对于 Node.js 开发者来说，它看起来像 `let/const`，但本质上 Java 依然是静态强类型语言。

## 1. 快速判定表 (Decision Matrix)

| 场景 | 推荐用法 | 原因 |
| --- | --- | --- |
| `var x = new User()` | **使用 `var`** | 类型在右侧已明确展示。 |
| `var x = 10` | **使用 `var`** | 基本类型字面量非常明确。 |
| `var x = service.get()` | **使用具体类型** | 不看 Service 源码不知道 x 是什么。 |
| `Map<K, V> x = ...` | **使用 `var`** | 泛型语法过于沉重，`var` 能极大释放视觉。 |
| 类的成员变量 | **禁止使用** | Java 不支持属性级别的类型推断。 |

## 2. 最佳实践原则

### 原则一：Reading code is more important than writing it
如果 `var` 导致你在看代码时需要频繁将鼠标悬停在变量上查看类型，那么你应该改回具体类型。

### 原则二：使用有意义的变量名
如果你决定使用 `var`，那么变量名必须更加具有描述性。
```java
// ❌ 差：不知道是什么
var data = repository.fetch();

// ✅ 好：变量名补齐了类型信息
var userList = repository.fetch();
```

## 3. Node.js 开发者视角

在 JS 中，类型是运行时的。在 Java 中，`var` 只是编译器的“语法糖”。
- **JS**: `let x = 1; x = "abc";` (OK)
- **Java**: `var x = 1; x = "abc";` (编译报错：类型已确定为 int)

## 4. 常见的 var 禁忌

1. **不要用于解耦实现**：如果你希望变量是 `List` 接口而不是具体的 `ArrayList`，不要用 `var`。
2. **不要用于复杂的流初始化**：在多行逻辑中，清晰的类型声明有助于 Debug。

---
**参考资料**：
- [OpenJDK: Local Variable Type Inference FAQ](https://openjdk.org/projects/amber/guides/lvti-faq)
- [Baeldung: Java 10 Local Variable Type-Inference](https://www.baeldung.com/java-10-local-variable-type-inference)
