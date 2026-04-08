# 24 - 深入浅出 Servlet：Java Web 的基石

## 核心心智映射 (Core Mental Mapping)

Servlet（Server Applet）是 Java 处理 HTTP 请求的最底层标准。理解了 Servlet，你就理解了所有 Java Web 框架（包括 Spring Boot）的灵魂。

| 领域 | Node.js (http 模块) | Java (Servlet) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **运行时** | Node.js Runtime | **Servlet 容器 (如 Tomcat)** | 承载代码的“家” |
| **核心处理** | `createServer((req, res) => ...)` | **`HttpServlet` 子类** | 定义如何响应请求 |
| **入口逻辑** | 用户手动监听端口 | **DispatcherServlet** | 自动处理分发的“上帝 Servlet” |
| **对象模型** | `IncomingMessage` / `ServerResponse` | **`HttpServletRequest` / `Response`** | 请求与响应的载体 |

---

## 概念解释 (Conceptual Explanation)

### 1. 什么是 Servlet？
简单来说，Servlet 就是一个运行在服务器端的 Java 程序，专门用来处理 HTTP 请求。它是 Java 定义的一套标准接口。

### 2. Servlet 容器 (Tomcat 是干什么的？)
Servlet 自己不能独立运行，需要 Tomcat 这种容器来管理：
-   **网络封装**: 将 Socket 原始数据解析为 `HttpServletRequest` 对象。
-   **生命周期管理**: 负责 Servlet 的创建 (init)、调用 (service) 和销毁 (destroy)。
-   **多线程模型**: 每个请求都会由容器从线程池分配一个线程来处理。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心生命周期方法
-   **`init()`**: 第一次被请求时调用。用于资源初始化（仅一次）。
-   **`service()`**: 核心逻辑入口。在 `HttpServlet` 中通过 `doGet`/`doPost` 分发逻辑。
-   **`destroy()`**: 容器关闭前调用。用于释放资源。

---

## 典型用法 (Typical Usage)

### 原始 Servlet 示例 (用于理解底层)
```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("Hello, World!");
    }
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 **Spring Boot 与 Servlet 的关系**:
虽然你在 Spring 中写的是 `@RestController`，但 Spring Boot 启动时会自动注册一个名为 **`DispatcherServlet`** 的巨型 Servlet。
-   它是所有请求的总入口开关。
-   它根据你的注解（如 `@GetMapping`），把原始请求分发给对应的 `Controller` 方法。
-   这也就是为什么在 Filter 中看到参数是 `ServletRequest` 时，我们需要强转为 `HttpServletRequest` 才能拿到 HTTP 方法。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

大多数时候你不需要直接写 Servlet，但在调试底层参数（如 Cookie、Header、Session）时，了解它是必要的。

> **最佳实践 Prompt**:
> "我正在调试一个 Spring Boot 项目，发现请求头中的某个特殊字段被丢弃了。
> 1. 请告诉我如何在 `DispatcherServlet` 之前的 Filter 层通过原始的 `HttpServletRequest` 打印所有 Header。
> 2. 请解释 Tomcat 的线程池大小（`server.tomcat.threads.max`）与 Servlet 并发处理能力的底层关系。
> 3. 请说明在 Spring MVC 中如何直接获取底层的 `HttpServletRequest` 对象而不破坏 Controller 的纯粹性。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Jakarta Servlet Specification](https://jakarta.ee/specifications/servlet/) - 行业标准的源头。
2. [Spring MVC: DispatcherServlet](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html) - 查看 Spring 如何将 Servlet 封装成工业级框架。
