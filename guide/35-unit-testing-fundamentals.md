# Java 单元测试入门：JUnit 5 + Mockito + AAA 模式

在 Node.js 中，我们常用 `Jest` 或 `Vitest` 进行单元测试。Java 的标准组合是 **JUnit 5** (测试框架) + **Mockito** (Mock 工具)。

## 1. 核心概念映射：从 Node.js 到 Java

| 概念 | Node.js (Jest) | Java (JUnit 5 + Mockito) |
| :--- | :--- | :--- |
| **定义测试** | `it('should ...')` 或 `test('should ...')` | `@Test` + `@DisplayName("测试说明")` |
| **准备工作** | `beforeEach(() => ...)` | `@BeforeEach` |
| **创建 Mock** | `jest.fn()` 或 `jest.mock()` | `@MockBean` 或 `mock(ClassName.class)` |
| **Mock 行为定义** | `mockFn.mockReturnValue(val)` | `when(mockObj.method()).thenReturn(val)` |
| **断言方式** | `expect(a).toBe(b)` | `assertEquals(expected, actual)` 或 `Assertions.assertThat` |

## 2. 现代单元测试的最佳实践：AAA 模式

无论语言如何，高质量的测试通常遵循 **AAA (Arrange, Act, Assert)** 结构：

1.  **Arrange (准备)**：设置 Mock 的行为、构造输入参数、初始化被测对象。
2.  **Act (执行)**：调用被测方法，并捕获结果（或异常）。
3.  **Assert (断言)**：验证结果是否符合预期，或验证 Mock 对象是否被调用。

### 2.1 代码示例：
查看 `src/test/java/com/javalabs/controller/UserControllerTest.java`：

```java
@Test
@DisplayName("测试 POST /api/users - 合法数据应成功")
void shouldCreateUser() {
    // 1. Arrange (准备)
    User validUser = new User(null, "javaman", "java@test.com", "password123", ...);
    User savedUser = new User(1L, "javaman", "java@test.com", "password123", ...);
    // 当调用 userService.createUser 时，返回 savedUser
    when(userService.createUser(any(User.class))).thenReturn(savedUser);

    // 2. Act (执行)
    // 这里使用了 MockMvc 来模拟 HTTP 请求，并链式地进行了断言
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validUser)))
            
    // 3. Assert (断言)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.username").value("javaman"));
}
```

## 3. 手把手运行指引：如何在本项目中运行测试

### 3.1 运行所有测试
在命令行运行：
```bash
mvn test
```

### 3.2 运行特定测试类
```bash
mvn test -Dtest=UserControllerTest
```

### 3.3 运行特定测试方法
```bash
mvn test -Dtest=UserControllerTest#shouldCreateUser
```

## 4. 关键注解解释
- **`@WebMvcTest`**：只启动 Web 层的 Spring 环境（即 Controller），不启动数据库和 Service。这是提高单元测试速度的关键。
- **`@MockBean`**：自动创建一个 Mock 对象并注入到 Spring 上下文中，替代真实的 Service 实例。

---
**扩展阅读：**
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://site.mockito.org/)
- [Baeldung: Guide to JUnit 5](https://www.baeldung.com/junit-5)
