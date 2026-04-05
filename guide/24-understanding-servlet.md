# 24-深入浅出 Servlet：Java Web 的基石

Servlet（Server Applet）是 Java 控制 Web 请求的最底层标准。理解了 Servlet，你就理解了所有 Java Web 框架（包括 Spring Boot）的灵魂。

---

## 1. 什么是 Servlet？

简单来说，Servlet 就是一个**运行在服务器端的 Java 程序**，专门用来处理 HTTP 请求并生成响应。

Servlet（Server Applet）是 Java 定义的一套处理 HTTP 请求的标准接口。在 Java 中：所有的 Web 框架（Spring MVC, Struts, JSF）底层都是基于 HttpServlet 实现的。

### 核心心智模型

在 Node.js 中，你可能会写：

```javascript
http
  .createServer((req, res) => {
    if (req.url === '/hello') {
      res.end('Hello World');
    }
  })
  .listen(8080);
```

而在 Java 中，我们不直接操作 Socket，而是定义一个 **Servlet 类**：

```java
@WebServlet("/hello") // 路由映射
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.getWriter().write("Hello World"); // 响应输出
    }
}
```

---

## 2. Servlet 容器（Container）：Tomcat 是干什么的？

Servlet 自己是不能运行的，它需要一个“家”，这就是 **Servlet 容器**（如 Tomcat）。

容器的作用类似于 Node.js 的运行时 + `http.createServer` 的封装：

1. **生命周期管理**：负责创建 Servlet 实例、初始化、调用方法、最后销毁。
2. **多线程处理**：每进入一个请求，容器就会从线程池里取出一个线程来执行 Servlet（这也就是为什么我们第一周要学并发）。
3. **网络封装**：把复杂的 HTTP 报文解析成 `HttpServletRequest` 对象供你使用。

---

## 3. Servlet 的生命周期：三个关键时刻

Servlet 的生命周期由容器（Tomcat）全权管理。你可以把它类比为 Node.js 长期运行进程中的单例对象：

| 生命周期阶段 | 对应方法 | 发生时机 | Node.js 类比 |
| :--- | :--- | :--- | :--- |
| **初始化 (Init)** | `init()` | Servlet 第一次被请求或服务器启动时调用（**仅一次**）。用于加载资源、建立连接。 | 类的 `constructor` 或 `once('ready')` |
| **就绪/执行 (Service)** | `service()` | 每次收到请求时调用（**多次**）。在 `HttpServlet` 中会分发给 `doGet`/`doPost`。 | 核心 `requestHandler` 回调 |
| **销毁 (Destroy)** | `destroy()` | Web 应用停止或 Servlet 被卸载时调用（**仅一次**）。用于清理内存、关闭连接。 | `process.on('exit')` 钩子 |

> [!TIP]
> **深度思考：单例与并发**  
> 默认情况下，Servlet 是**单例 (Singleton)** 的。这意味着在高并发下，会有多个线程同时执行同一个 Servlet 实例的 `service()` 方法。  
> **这就是为什么你在 Servlet（或 Spring Controller）中绝对不能使用非线程安全的成员变量！** 这和你编写无状态的 Node.js 处理器逻辑异曲同工。

---

## 4. Spring Boot 与 Servlet 的关系

你可能会问：“既然我已经用了 Spring Boot 的 `@RestController`，为什么还要了解 Servlet？”

**因为 Spring MVC 的核心就是一个超级 Servlet！**

在 Spring 架构中：

1. **DispatcherServlet**：Spring Boot 启动后，会自动向容器注册一个名为 `DispatcherServlet` 的类。
2. **总入口**：所有的请求（无论是 `/api/employees` 还是 `/api/greeting`）都会先到达这个总 Servlet。
3. **分发器**：它根据你的 `@RequestMapping` 注解，把请求分发（Dispatch）给具体的 Controller 方法。

---

## 5. 开发实践：通过原始 API 感受底层的力量

我们在 `RequestLogFilter` 中看到的 `doFilter` 参数是 `ServletRequest`。
由于 Servlet 规范不只支持 HTTP（虽然现在基本只用 HTTP），所以我们需要手动强转：

```java
// 代码片段来自 RequestLogFilter.java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    // 1. 强制转型：告诉编译器我们要作为 HTTP 协议处理
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    // 2. 只有转型后，才能调用 .getMethod(), .getRequestURI() 等 HTTP 特有方法
    String method = httpRequest.getMethod();
}
```

---

---

## 6. 核心知识大盘点：转型 Java 必须跨越的坎

作为资深 Node.js 开发者，请务必在脑海中刻下这三点差异：

### 6.1 线程模型的降维打击 🌪️
*   **Node.js 指南**：放心写全局变量。只要不操作数据库/文件，一切都是同步且线程安全的。
*   **Servlet 真相**：**一请求一线程**。
    *   Tomcat 线程池（默认 200 个线程）会并发调用你的 Servlet。
    *   **禁忌**：绝对不要在 Servlet/Filter/Controller 中定义“有状态”的成员变量。
    *   *错误示例*：`private int count = 0;` 在 `doGet` 里 `count++`。这在并发下会丢数据。

### 6.2 存储阶梯：数据放哪里？ 📂
*   **Request 域**：相当于 Express 的 `req.myData`。仅在本次请求有效。
*   **Session 域**：由容器自动关联 Cookie。类似于 Express-session，但在 Java 中它是通过 `req.getSession()` 这种原生 API 获取的。
*   **Context 域 (ServletContext)**：全应用唯一的全局对象。类似于 Node.js 的 `process.env`（但可读写）或全局单例。

### 6.3 异步 Servlet 的演进 ⚡
*   早期的 Servlet 是阻塞的（读取大文件会卡死线程）。
*   **Servlet 3.0+** 引入了 `request.startAsync()`。
*   **心智关联**：这让 Java 拥有了类似于 Node.js 非阻塞处理的能力，Spring MVC 的 `DeferredResult` 和 WebFlux 响应式框架都是基于这种底层能力进化的。

---

### 📚 扩展阅读

1. [MDN: HTTP 概述](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Overview) - 夯实协议基础。
2. [Jakarta Servlet 官方规范简述](https://jakarta.ee/specifications/servlet/) - 了解行业标准。
3. [Spring MVC 官方文档 - DispatcherServlet](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html) - 查看 Spring 是如何封装 Servlet 的。
