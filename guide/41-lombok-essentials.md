# 44 - Lombok：Java 手动挡时代的“自动驾驶”

## 核心心智映射 (Core Mental Mapping)

如果你觉得编写 Java 的 Getter/Setter/ToString 极其琐碎，Lombok 就是你的救星。它通过注解在编译期自动为你生成这些重复代码。

| 维度 | Node.js / TS (Class) | Java (Lombok) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **样板代码** | 较少，原生支持属性 | **极大，需要手动编写** | 消灭 Boilerplate |
| **实现方式** | 成员变量直接访问 | **`@Data` 自动生成全家桶** | 简化对象定义 |
| **依赖注入** | 构造函数手动写 | **`@RequiredArgsConstructor`** | 优雅的 DI 方式 |
| **链式调用** | 对象扩展 / Fluent API | **`@Builder` 注解** | 极简的对象构建 |
| **日志声明** | `console.log` | **`@Slf4j` 自动注入 log** | 统一口径，省心记录 |

---

## 概念解释 (Conceptual Explanation)

### 1. 什么是 Lombok？
Lombok 不是一个运行时工具，而是一个 **编译期插件 (Annotation Processor)**。
当你执行 `mvn compile` 时，Lombok 会拦截并修改抽象语法树（AST），把你写的 `@Data` 替换成真正的 `getXXX`、`setXXX` 字节码。这意味着你的 `.class` 文件和手写的一模一样，但 `.java` 源码却异常整洁。

### 2. 注入神器：构造器注入
在 Spring Boot 中，我们不再推荐用 `@Autowired`。通过 `@RequiredArgsConstructor` 配合 `final` 字段，Lombok 会自动生成构造函数，实现最稳健的构造器注入。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 注解全家桶
-   **`@Data`**: 最常用，包含 Getter, Setter, ToString, EqualsAndHashCode。
-   **`@Slf4j`**: 自动生成 `private static final Logger log`，直接使用 `log.info()`。
-   **`@Builder`**: 开启 Builder 模式，支持 `Book.builder().title("X").build()`。
-   **`@NoArgsConstructor` / `@AllArgsConstructor`**: 生成无参/全参构造函数（配合框架反序列化必选）。

---

## 典型用法 (Typical Usage)

### 1. 标准实体类 (Entity / DTO)
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
}
```

### 2. 优雅的 Controller 注入
```java
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService; // 自动生成构造器注入
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **`Book.java`**:
我们只声明了 5 个字段和 3 个注解。
-   你可以直接在其他类里调用 `book.getTitle()`。
-   如果你修改了字段名，IDE 会自动重构关联的 Lombok 方法，极其安全。
-   **注意**: 即使源码里没有 `toString`，你在打印对象时也会看到漂亮的 `Book(id=1, title=...)` 输出，这就是 Lombok 的魔力。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Lombok 注解虽然强大，但滥用（如在大型继承树中）可能导致性能或逻辑问题。

> **最佳实践 Prompt**:
> "我有一个包含 30 个字段的旧 Java 类，充满了手写的 Getter/Setter。
> 1. 请帮我使用 Lombok 进行重构，保留所有功能但使代码行数减少 80%。
> 2. 请确保为该类添加 `@Builder` 以支持流式创建，并解释为什么建议同时加上 `@NoArgsConstructor`。
> 3. 请检查字段中是否有 `boolean` 类型，并告知 Lombok 对其生成的 Getter 命名规则（如 `isVip()` 而非 `getVip()`）。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Project Lombok Official Features](https://projectlombok.org/features/all) - 官方功能全集。
2. [Baeldung: Introduction to Lombok](https://www.baeldung.com/intro-to-project-lombok) - 快速入门指南。
3. [Alibaba Java Manual: POJO Naming Rules](https://github.com/alibaba/p3c) - 为什么 DTO 的 Boolean 字段在 Lombok 下需要额外注意。
