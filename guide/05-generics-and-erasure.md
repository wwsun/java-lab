# 05-泛型与类型擦除 (Generics and Type Erasure)

在 TypeScript 中，泛型（Generics）是你处理多种类型数据的核心工具。Java 的泛型在语法上与 TS 非常相似，但在底层实现上有一个巨大的坑：**类型擦除 (Type Erasure)**。这是每个从 TS 转型 Java 的开发者必须掌握的心智模型。

## 1. 核心心智映射

| TypeScript | Java | 关键差异 |
| --- | --- | --- |
| `Array<T>` / `T[]` | `List<T>` | Java 的泛型只适用于对象，不适用于基本类型（如 `int`）。 |
| `interface Box<T> { ... }` | `public class Box<T> { ... }` | Java 中泛型信息在运行期是缺失的。 |
| `T extends string` | `T extends String` | Java 中使用 `extends` 指定上界，用法一致。 |

## 2. 什么是类型擦除？

**TypeScript**：泛型只在**编译期**存在。编译成 JS 后，所有的类型信息都消失了。
**Java**：同样，泛型也只在**编译期**进行类型检查。在**运行期**，所有的泛型信息都会被“擦除”为它的第一个上界（通常是 `Object`）。

### 为什么这很重要？

在 TS 中，如果你写：
```typescript
function isString<T>(item: T): boolean {
  // TS 允许你做这种逻辑判定（虽然通常配合 type guard）
  return typeof item === 'string';
}
```

在 Java 中，你**不能**这样做：
```java
public <T> void check(T item) {
    // 错误！在运行期，T 只是 Object，Java 不知道 T 到底是什么
    if (item instanceof T) { ... } 
}
```

### 类型擦除带来的限制：
1. **无法直接实例化**：不能 `new T()`。
2. **无法使用 `instanceof T`**：因为运行期没有 `T` 这个类型。
3. **无法创建泛型数组**：不能 `new T[10]`。

## 3. Java 泛型的高级用法：通配符 (Wildcards)

这是 Java 泛型中最让 Node.js 开发者头疼的地方：`?`。

- `List<? extends Number>`：**生产者 (Producer) 上限**。表示列表里存的是 Number 或其子类。你可以从中**取**出 Number，但不允许**存入**任何东西（除了 null），因为编译器不知道具体是哪种数字。
- `List<? super Integer>`：**消费者 (Consumer) 下限**。表示列表里可以接收 Integer 或其父类。你可以往里**存** Integer。

**口诀：PECS (Producer Extends, Consumer Super)**
- 如果你需要读数据，用 `extends`。
- 如果你需要写数据，用 `super`。

## 4. 实战建议

作为指挥 AI 的开发者，你只需要记住：
1. **尽量让 AI 使用具体的类型**，避免裸露的 `List`（Raw Types）。
2. **遇到类型转换报错时**，问 AI：“这段泛型逻辑是否违反了类型擦除规则？请用 `Class<T>` 传参的方式重构。”

---
**扩展阅读**：
- [Oracle 官方泛型教程](https://docs.oracle.com/javase/tutorial/java/generics/index.html)
- [Baeldung: Java Generics Guide](https://www.baeldung.com/java-generics)
