# 09 - 泛型与类型擦除

## 核心心智映射 (Core Mental Mapping)

如果你在 TypeScript 中频繁使用泛型（Generics），你会发现 Java 的泛型在语法上高度相似，但在底层实现上有一个巨大的“坑”：**类型擦除**。

| 概念 | TypeScript | Java | 核心差异 |
| :--- | :--- | :--- | :--- |
| **声明** | `Array<T>` / `Box<T>` | `List<T>` / `Box<T>` | Java 泛型只适用于对象，不适用于 `int/double` |
| **约束** | `T extends string` | `T extends String` | 语法几乎完全一致 |
| **运行时** | 类型信息消失 (转为 JS) | **类型被擦除为 Object** | Java 运行时也拿不到 `T` 的具体类型 |
| **通配符** | (通常用联合类型 `A | B`) | `? extends T` / `? super T` | Java 独有的 PECS 一致性设计 |

---

## 概念解释 (Conceptual Explanation)

### 1. 什么是类型擦除？
在 Java 中，泛型只在**编译期**进行类型检查。一旦编译成字节码，所有的泛型信息都会被“擦除”为其上界（通常是 `Object`）。
*   **后果**: 在运行期，`List<String>` 和 `List<Integer>` 的二进制格式是完全一样的。

### 2. 擦除带来的禁区
由于运行期不知道 `T` 是什么，你**不能**做以下操作：
-   `new T()`: 无法直接实例化。
-   `instanceof T`: 无法判断。
-   `new T[10]`: 无法创建泛型数组。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 泛型类声明
```java
public class Result<T> {
    private T data;
    // ... getter/setter
}
```

### PECS 准则 (这是 Java 泛型的灵魂)
-   **P**roducer **E**xtends: 如果你需要从集合里**读**数据，用 `? extends T`。
-   **C**onsumer **S**uper: 如果你需要往集合里**写**数据，用 `? super T`。

---

## 典型用法 (Typical Usage)

### 使用 `Class<T>` 绕过类型擦除
如果你在运行期真的需要知道 `T` 是什么，通常的做法是显式传递一个 `Class` 对象。

```java
public class GenericScanner<T> {
    private final Class<T> type;

    public GenericScanner(Class<T> type) {
        this.type = type;
    }

    public void check(Object obj) {
        if (type.isInstance(obj)) {
            System.out.println("It is a " + type.getSimpleName());
        }
    }
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察以下 `List<? extends Number>` 的特性：
```java
List<? extends Number> list = new ArrayList<Integer>();
Number num = list.get(0); // 编译通过：因为不管存的是什么，它一定是 Number 或其子类
// list.add(10);          // 编译失败！：虽然 10 是 Integer，但 list 可能指向 List<Double>
```
这是 Java 强类型的安全性保障。它告诉编译器：这个列表是一个“只读的数字列表”，只能取出（Produce），不能存入（Consume），除非存入的是 `null`。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Java 泛型报错通常晦涩难懂（尤其是通配符冲突）。

> **最佳实践 Prompt**:
> "我正在编写一个 Java 泛型方法，但在处理 `List<? extends T>` 时遇到了无法 add 元素的问题。
> 1. 请解释这是否违反了 PECS 准则？
> 2. 请帮我根据我的业务场景（我需要同时读写该列表），建议应该使用具体的 `<T>` 还是 `<? super T>`。
> 3. 请说明如何在不牺牲类型安全的前提下重构该方法。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Oracle: Generics Lesson](https://docs.oracle.com/javase/tutorial/java/generics/) - 官方最系统的讲解。
2. [Baeldung: The PECS Principle](https://www.baeldung.com/java-generics-pecs) - 专门讲解通配符用法的文章，非常清晰。
