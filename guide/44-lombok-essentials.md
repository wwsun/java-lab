# 20-Lombok：Java 手动挡时代的"自动驾驶"

作为从 Node.js/TypeScript 转型而来的开发者，你可能会对 Java 的繁琐（Boilerplate Code）感到不适。Lombok 就是为了解决这个问题而诞生的神器。

美式发音：[ˈlɑːmbɑːk]（近似音：朗-博克）

## 1. 什么是 Lombok？

**Lombok** 是一个 Java 库，它通过**注解（Annotations）**在编译期自动为你生成代码。

### 核心类比

- **在 TypeScript 中**：如果你想为一个类添加 Getter/Setter，你可能需要手写，或者依赖 IDE 插件。如果你用过 `class-transformer` 或某些 Decorators，Lombok 的感觉非常相似。
- **本质**：它是一个 **编译期插件**。它不是在运行时通过反射去操作，而是在你按下 `mvn compile` 或 IDE 编译的一瞬间，把源码里的 `@Data` 替换成真正的 `getXXX`、`setXXX`、`toString` 等方法的二进制代码。

---

## 2. 你一定要知道的"全家桶"注解

以下是 Lombok 中最高频、最实用的注解，掌握这几个就足够应付 90% 的场景。

### 1. `@Data` (最常用)

它是真正的“全家桶”，相当于同时加了：

- `@Getter` / `@Setter`：生成所有字段的访问方法。
- `@ToString`：生成易读的 `toString()`。
- `@EqualsAndHashCode`：生成正确的对比方法。
- `@RequiredArgsConstructor`：为 `final` 字段生成构造函数（这就是为什么 Spring 注入推荐用它）。

```java
@Data
public class User {
    private Long id;
    private String username;
}
// 编译后，你可以直接使用 user.getUsername() 和 user.setUsername()
```

### 2. `@RequiredArgsConstructor` (依赖注入神器)

在 Spring Boot 中，我们不再推荐用 `@Autowired`。最稳健的方案是 **构造器注入**。手动写构造函数很烦，于是：

```java
@RestController
@RequiredArgsConstructor // 自动生成包含 bookService 的构造函数
public class UserController {
    // 标记为 final，Lombok 就会把它放进构造函数里
    private final BookService bookService;
}
```

### 3. `@Slf4j` (日志必备)

不用再手写 `private static final Logger log = ...` 了。

```java
@Slf4j
@Service
public class UserService {
    public void doSomething() {
        log.info("执行了某些操作"); // 自动拥有 log 对象
    }
}
```

### 4. `@Builder` (链式调用)

类似于 TS 中的对象扩展或流式 API，让对象创建变得极其优雅。

```java
@Builder
@Data
public class Book {
    private String title;
    private String author;
}

// 使用时：
Book book = Book.builder()
    .title("Java 实战")
    .author("某作者")
    .build();
```

### 5. `@NoArgsConstructor` & `@AllArgsConstructor`

生成无参和全参构造函数。在与 MyBatis-Plus、Jackson（JSON 序列化）配合时，**无参构造函数通常是必须的**。

---

## 3. 为什么你必须知道它？

1.  **代码极简**：一个原本 100 行的实体类，用了 Lombok 只要 10 行。
2.  **避免 Bug**：如果你手动修改了字段名，却忘了改对应的 Setter，会导致很多隐蔽的运行时错误。Lombok 在编译期就帮你搞定了。
3.  **行业标准**：无论是在阿里、腾讯还是国际大厂，Lombok 几乎是 Java Web 开发的默认标配。

---

## 4. 特别避坑指南 (很重要)

1.  **IDE 插件**：在本地开发时，IntelliJ IDEA 必须安装 **Lombok 插件**（现代版本内部已集成），否则 IDE 会报错说找不到 `getXXX` 方法。
2.  **DTO 必须加 `@NoArgsConstructor`**：如果你定义了一个 `@Data` 类作为接口入参，尽量同时也加上 `@NoArgsConstructor` 和 `@AllArgsConstructor`，确保 JSON 反序列化不会出问题。
3.  **调试**：由于代码是生成的，你无法在 Getter 方法里打断点。如果你需要复杂的 Getter 逻辑，请手动手写该方法，Lombok 会检测到并跳过生成这个特定方法。

---

## 5. 运行指引

你可以去 `src/main/java/com/javalabs/entity/` 下找任何一个实体类（比如 `Book.java`），尝试删掉 `@Data` 观察 IDE 的报错，再加回来。

**扩展阅读**：

1. [Lombok 官方文档 - Features](https://projectlombok.org/features/all)
2. [Baeldung: Introduction to Project Lombok](https://www.baeldung.com/intro-to-project-lombok)
