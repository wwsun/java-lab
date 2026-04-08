# 21 - 实战验证：IoC 与 DI 的“安全性”深度实验

## 核心心智映射 (Core Mental Mapping)

如果你把 Spring 容器想象成一个“自动化工厂”，依赖注入 (DI) 就是在这个工厂流水线上组装零件的过程。

| 注入方式 | 表现形式 | 心智映射 | 安全性 |
| :--- | :--- | :--- | :--- |
| **字段注入** | `@Autowired` 加在属性上 | 类似在 JS 对象上打补丁 | ❌ 低（隐藏依赖，不易测试） |
| **构造器注入** | **构造函数传参** | 类似 Node.js 显式传参 | ✅ 高（编译期拦截，强制依赖） |
| **Setter 注入** | `setService(...)` | 后期配置 | ⚠️ 中（可选依赖） |

---

## 概念解释 (Conceptual Explanation)

### 1. 为什么“构造器注入”是现代标准？
字段注入虽然代码量少，但它有巨大的缺陷：它允许你在**不完整**的状态下创建对象。
而构造器注入（配合 `final` 关键字）保证了：**任何一个 Bean 在被 Spring 创建出来的一瞬间，它的所有依赖都必须已经到位**。

### 2. 生命周期钩子 (@PostConstruct)
在 Java 中，构造函数执行时，依赖可能还没注入完成。如果你需要在对象创建后立即执行一些逻辑，应该使用 `@PostConstruct`。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 黄金方案：Lombok + Final
这是目前工业界最推荐的写法：
```java
@Service
@RequiredArgsConstructor // 自动生成包含所有 final 字段的构造函数
public class OrderService {
    private final PaymentService paymentService; // 必须注入，不可修改
}
```

---

## 典型用法 (Typical Usage)

### 1. 安全验证实验
我们可以模拟脱离 Spring 环境手动 `new` 对象：
-   **字段注入**: `new MyService()` 编译通过，但调用时会报 `NullPointerException`。
-   **构造器注入**: `new MyService()` 编译直接报错，强制你传入依赖。

### 2. 初始化逻辑
```java
@PostConstruct
public void init() {
    // 此时依赖已注入，可以在这里加载缓存或预热数据
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `DISafetyTest.java` 中的对比：
属性注入类似于在 Node.js 中写了一个 class，但其内部的 `service` 变量始终是 `undefined`，直到你在运行时手动给它赋值。这不仅违反了封装性，也让单元测试变得极其复杂。
而构造器注入利用了 Java 的**强语言约束**，实现了“编译即文档”：只要能通过编译，对象就是完整的。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

很多旧项目还在使用 `@Autowired` 字段注入，这被视为反模式。

> **最佳实践 Prompt**:
> "请帮我 Review 这段遗留的 Java 代码：`[贴入代码]`。
> 1. 请指出其中的字段注入（Field Injection）风险。
> 2. 请利用 Lombok 的 `@RequiredArgsConstructor` 和 `final` 关键字，将其重构成安全的构造器注入。
> 3. 请说明这种重构如何让单元测试（Mock）变得更容易。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Spring Blog: Why field injection is evil](https://blog.marcosbarbero.com/field-injection-is-evil/) - 必读的行业共识。
2. [Baeldung: Constructor Injection in Spring](https://www.baeldung.com/constructor-injection-in-spring) - 详细对比各种注入方式。
