# 20 - Spring Boot 核心：现代 Java 后端的工业级范式

## 核心心智映射 (Core Mental Mapping)

欢迎来到 Java 生态的基石 —— **Spring Boot 3.x**。如果你有 NestJS 或全栈开发经验，理解 Spring Boot 的核心其实非常直观。

| 场景 | Node.js (NestJS / Express) | Java (Spring Boot) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **路由层** | `@Controller()` | **`@RestController`** | 业务的“门卫” |
| **业务逻辑** | `@Injectable() Service` | **`@Service`** | 领域逻辑处理器 |
| **依赖注入** | `constructor(srv: Srv)` | **`@RequiredArgsConstructor`** | “大管家”分配资源 |
| **配置文件** | `.env` / `config.json` | **`application.yml`** | 类型安全的核心配置 |
| **启动逻辑** | `app.listen()` | **`SpringApplication.run()`** | 一键点火 |

---

## 概念解释 (Conceptual Explanation)

### 1. 约定优于配置 (CoC - Convention Over Configuration)
这是 Spring Boot 成功的核心原因：只要你遵循约定，框架就会自动帮你完成工作。
-   **包扫描**: 只要你的类在主启动类（`@SpringBootApplication`）所在包或其子包下，它们就会被自动发现并交给 Spring 管理。
-   **零配置启动**: 默认集成了 Tomcat、JSON 序列化、日志系统，你只需关注业务代码。

### 2. 控制反转 (IoC - Inversion of Control)
原本由你手动 `new` 对象，现在交给 Spring 这个“大管家”。
-   **好处**: 解耦。你只需声明“我需要一个支付服务”，Spring 就会在启动时帮你把对应的实例注入进来。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心注解
-   **`@SpringBootApplication`**: 复合注解，标志着这是一个 Spring Boot 项目的入口。
-   **`@RestController`**: 组合了 `@Controller` 和 `@ResponseBody`，返回值会自动序列化为 JSON。
-   **`@Service` / `@Component`**: 将类标识为 Spring 的组件（Bean），交给管家管理。

---

## 典型用法 (Typical Usage)

### 1. 声明与注入
```java
@Service // 1. 声明它是管家管理的资源
public class UserService { ... }

@RestController
@RequiredArgsConstructor // 2. 自动生成构造函数进行注入
public class UserController {
    private final UserService userService; // 3. 声明我需要这个资源
}
```

### 2. 配置注入
通过 `@Value` 或 `@ConfigurationProperties` 轻松获取 `yml` 中的配置。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 `JavaLabsApplication.java`:
```java
@SpringBootApplication
public class JavaLabsApplication {
    public static void main(String[] args) {
        // 这一行执行后：
        // 1. 启动内嵌的 Tomcat 服务器
        // 2. 扫描所有 Bean 并完成依赖注入 (Dependency Injection)
        // 3. 建立 HTTP 路由映射图
        SpringApplication.run(JavaLabsApplication.class, args);
    }
}
```
它就像是一个预定义的骨架，免去了以往繁杂的 XML 配置，真正实现了 Web 应用的“开箱即用”。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Spring Boot 的分层结构非常固定，非常适合 AI 生成。

> **最佳实践 Prompt**:
> "我需要为一个新的『图书评论』模块生成基础骨架。
> 1. 请生成对应的 `BookComment` 实体类（Lombok 风格）。
> 2. 请生成 `BookCommentService` 并在其中注入 `BookCommentMapper`。
> 3. 请生成 `BookCommentController`，提供 GET/POST 接口，并确保包名符合 `com.javalabs` 的约定扫描路径。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Spring Boot: Build System](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems) - 官方对构建系统的权威解读。
2. [Baeldung: Intro to Spring Boot](https://www.baeldung.com/spring-boot) - 极佳的实战起步教程。
