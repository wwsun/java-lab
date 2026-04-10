# 15 - Java 单元测试入门：JUnit 5 + Mockito + AAA 模式

## 核心心智映射 (Core Mental Mapping)

在 Node.js 中，我们常用 `Jest` 或 `Vitest`。在 Java 生态中，**JUnit 5** (运行框架) + **Mockito** (Mock 工具) 是工业级的标准组合。

| 领域 | Node.js (Jest) | Java (JUnit 5 + Mockito) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **测试声明** | `it('should...')` | **`@Test` + `@DisplayName`** | 标注这是一个测试方法 |
| **生命周期** | `beforeEach(() => ...)` | **`@BeforeEach`** | 每个测试前的清洁工作 |
| **模拟对象** | `jest.fn()` | **`@Mock` / `mock()`** | 创建一个“虚假”的依赖寄生虫 |
| **注入模拟** | (手动注入或 Proxy) | **`@InjectMocks`** | 自动将 Mock 塞进被测对象 |
| **行为定义** | `mock.mockReturnValue()` | **`when(...).thenReturn(...)`** | 预排脚本：当 A 发生，返回 B |
| **验证调用** | `expect(fn).toHaveBeenCalled()` | **`verify(...).method(...)`** | 确认逻辑确实走到了这里 |

---

## 概念解释 (Conceptual Explanation)

### 1. 单元测试 (Unit Test) 的边界
单元测试只测试**一个类**的逻辑。如果这个类依赖了数据库或外部服务，我们需要用 Mock 屏蔽它们，确保测试结果不随网络或环境抖动。

### 2. AAA (Arrange, Act, Assert) 模式
这是编写高质量测试的黄金模板：
-   **Arrange (准备)**: 初始化对象，录制 Mock 行为。
-   **Act (执行)**: 调用目标方法，捕获返回值。
-   **Assert (断言)**: 验证结果是否符合预期。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心注解
-   **`@ExtendWith(MockitoExtension.class)`**: 激活 Mockito 能力。
-   **`@Mock`**: 声明一个模拟对象（如 Mapper）。
-   **`@InjectMocks`**: 声明被测对象（如 Service），Spring 会自动把上面的 Mock 注入进来。

### 录制行为
```java
// 录制：当调用查找 ID 为 1 的员工时，返回准备好的 Mock 数据
when(employeeMapper.selectById(1L)).thenReturn(mockEmployee);
```

---

## 典型用法 (Typical Usage)

### 1. 纯 Service 逻辑测试
不启动 Spring 容器，速度极快（毫秒级）。

### 2. Controller 层测试 (@WebMvcTest)
启动精简版 Spring 环境，只加载 Web 层。配合 **MockMvc** 模拟 HTTP 请求，无需真正启动 Tomcat 端口。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 `UserControllerTest.java`:
1.  我们使用了 `@WebMvcTest(UserController.class)`，它只启动了 Controller 相关的 Bean。
2.  通过 `mockMvc.perform(post("/api/users"))` 模拟前端提交 JSON 数据。
3.  通过 `jsonPath("$.data.username").value("javaman")` 验证返回的 JSON 结构。
这种测试方式比用 Postman 手动点点点要高效得多，且能作为项目的“安全护栏”。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

编写测试用例的边界条件（如 Null 处理、空集合等）非常适合交给 AI。

> **最佳实践 Prompt**:
> "我写好了一个 `OrderService.calculateTotal` 方法。
> 1. 请帮我使用 JUnit 5 和 Mockito 编写测试类。
> 2. 请覆盖以下场景：订单项为空、订单中有无效商品、正常计算总价。
> 3. 请严格遵循 AAA (Arrange-Act-Assert) 模式，并使用 `@DisplayName` 标注每个测试的意图。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/) - 官方指南。
2. [Mockito Documentation](https://site.mockito.org/) - 掌握各种 Stubbing 和 Verification 技巧。
3. [Baeldung: Guide to @WebMvcTest](https://www.baeldung.com/spring-boot-testing) - 深入了解切面测试。
