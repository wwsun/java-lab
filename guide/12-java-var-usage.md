# 12 - Java var 使用指南：简洁与可读性的平衡

## 核心心智映射 (Core Mental Mapping)

Java 10 引入了 `var`（局部变量类型推断），旨在减少 Java 传统语法的冗余感。对于 Node.js 开发者来说，它看起来非常像 `let/const`。

| 概念 | JavaScript / TypeScript | Java (var) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **声明** | `let x = 1` | `var x = 1` | 允许编译器推断类型 |
| **类型性质** | **动态类型** (运行期可变) | **静态类型** (编译期确定) | Java 还是那个 Java，只是简写了 |
| **作用域约束** | 块级作用域 | **仅局部变量** | 只能在方法内部使用 |
| **可读性哲学** | 默认首选 | **权衡使用** | Java 强调“代码首先是给人读的” |

---

## 概念解释 (Conceptual Explanation)

### 1. 局部变量类型推断 (LVTI)
`var` 不是关键字，而是一个“保留类型名”。它告诉编译器：“请根据右侧的初始化语句，帮我推断出这个变量的具体类型”。

### 2. 静态性保持
这与 JS 有本质区别：
```java
var x = 10;     // 编译器确定 x 是 int
// x = "hello"; // 编译报错！类型已锁定，无法修改
```

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 使用限制
-   **必须初始化**: `var a;` 是非法的，编译器无法推断。
-   **不支持 Null 赋值**: `var a = null;` 是非法的，因为 null 不具备具体的类型信息。
-   **单行声明**: 不支持 `var a = 1, b = 2;`。
-   **禁止成员变量**: 不能用于类的字段（属性），只能用于方法内部。

---

## 典型用法 (Typical Usage)

### 1. 消除冗长的泛型
这是 `var` 最显神威的地方：
```java
// 传统写法
Map<String, List<UserResponseDTO>> map = new HashMap<>();

// 使用 var
var map = new HashMap<String, List<UserResponseDTO>>();
```

### 2. 循环遍历
```java
for (var user : userList) {
    System.out.println(user.getName());
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 **菱形运算符** 陷阱：
```java
var list = new ArrayList<>(); // 编译器推断为 ArrayList<Object>
```
如果你希望它是 `ArrayList<String>`，你必须按照以下两种方式之一写：
1. `var list = new ArrayList<String>();` (明确右侧)
2. `List<String> list = new ArrayList<>();` (明确左侧，不使用 var)

**为什么？**
因为当左右两侧都使用推断（`var` 和 `<>`）时，编译器为了安全起见会退化到最通用的 `Object`。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

过度使用 `var` 会降低代码可读性。

> **最佳实践 Prompt**:
> "我正在重构一段逻辑非常复杂的 Java 代码，里包含了多层嵌套的 Map 和复杂的 Stream 链。
> 1. 请帮我判断哪些局部变量适合转换为 `var` 以减少视觉噪音。
> 2. 请指出哪些变量必须保留显式类型，因为它们的变量名（如 `result`, `data`）不足以描述其数据结构及其含义。
> 3. 请生成一份重构建议，平衡代码的简洁性与可维护性。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [OpenJDK: LVTI Style Guide](https://openjdk.org/projects/amber/guides/lvti-style-guide) - 官方对 var 使用时机的深度建议。
2. [Baeldung: Java 10 Local Variable Type-Inference](https://www.baeldung.com/java-10-local-variable-type-inference) - 详细的用法与局限性分析。
