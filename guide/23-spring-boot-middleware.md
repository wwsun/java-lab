# 23-SpringBoot 中间件深度解析：Filter vs Interceptor

本指南通过“请求日志全链路追踪”实战，带您理清 Spring Boot 中两种核心的拦截机制。

---

## 1. 核心模型对比：谁在里面，谁在外面？

在 Node.js 中，中间件（Middleware）通常是线性排列的。但在 Spring Boot 中，请求会经历一个“洋葱模型”：

| 特性 | **Filter (过滤器)** | **Interceptor (拦截器)** |
| :--- | :--- | :--- |
| **所属层级** | Servlet 容器 (如 Tomcat) | Spring MVC 框架内部 |
| **颗粒度** | 粗。只能拿到请求和响应对象 | 细。能拿到处理请求的具体的 Controller 方法 |
| **典型场景** | 全站日志、CORS、Gzip、权限初筛 | 业务权限校验、性能分析、数据预处理 |
| **Node.js 对标** | Express Middleware | NestJS Interceptor / Guard |

---

## 2. 代码实现解剖

### 2.1 Filter：系统哨兵
查看 [`RequestLogFilter.java`](../src/main/java/com/javalabs/filter/RequestLogFilter.java)。它使用了标准的 Servlet 规范，通过 `chain.doFilter` 将请求向后传递。

### 2.2 Interceptor：业务分析师
查看 [`PerformanceInterceptor.java`](../src/main/java/com/javalabs/interceptor/PerformanceInterceptor.java)。它实现了 Spring 的 `HandlerInterceptor`，可以精确获取到 `HandlerMethod`（即具体的 Controller 方法名）。

---

## 3. 运行指南：验证“套娃”顺序

### 🚀 演示步骤
1. **启动应用**：运行 `JavaLabsApplication`。
2. **发起请求**：访问 `curl http://localhost:8080/api/employees`。
3. **观察控制台日志**，您将看到完美的执行闭环：

```text
>>> [Filter 开始] HTTP GET /api/employees
    >>> [Interceptor 预处理] 执行者: EmployeeController#getAllEmployees
    <<< [Interceptor 后处理] 逻辑执行完毕
    <<< [Interceptor 完成] API 核心逻辑耗时: 5ms
<<< [Filter 结束] HTTP GET /api/employees | 耗时: 12ms
```

### 🧐 核心发现
*   **Filter 总是第一个进入，最后一个离开**。它是真正意义上的“请求守护者”。
*   **Interceptor 精确包裹了业务代码**。如果您想统计“数据库+逻辑”的纯净执行时间，拦截器是最佳选择。
*   **Interceptor 可以通过 handler 对象获取元数据**。例如，我们可以根据特定的自定义注解来决定是否放行请求。

---

## 4. 异常中断实验：Filter 崩了会怎样？

我们在实战中进行了一个“破坏性”实验：**如果在 `RequestLogFilter` 中直接抛出异常，拦截器还会执行吗？**

### 🧪 实验现象
1.  **客户端**：收到了 500 错误响应。
2.  **控制台日志**：**完全没有** `PerformanceInterceptor` 的相关日志。
3.  **结果**：请求在“大门口”（Filter）被拦截，由于异常未捕获，请求链路直接中断。

### 🧐 架构深意：防线与边界
*   **Filter 是第一道防线**：它独立于 Spring 框架之外。如果过滤器认定请求非法并抛出异常，请求将永远不会触达 Spring 的 `DispatcherServlet`，更不会触发后续的拦截器（Interceptor）。
*   **资源保护**：在 Filter 层拦截非法请求能极大地保护服务器资源，因为它避免了昂贵的 Spring Bean 初始化、参数绑定和复杂的业务逻辑操作。

---

## 5. 总结：执行顺序全景图

当一个请求进入 Spring Boot 应用时，它的预期生命周期如下：

1.  **[Filter Before]**：`RequestLogFilter.java` 开始记录耗时。
2.  **[DispatcherServlet]**：请求进入 Spring 核心枢纽。
3.  **[Interceptor preHandle]**：拦截器识别到 Controller 方法，记录开始时间。
4.  **[Controller Logic]**：执行您的业务逻辑。
5.  **[Interceptor postHandle]**：逻辑执行结束。
6.  **[Interceptor completion]**：拦截器收尾，计算业务纯净耗时。
7.  **[Filter After/Finally]**：过滤器收尾，记录全链路总耗时。

---

> [!IMPORTANT]
> **结论**：Filter 负责“进场安检”（CORS、Auth、Logging），Interceptor 负责“场内秩序”（业务校验、性能统计）。如果安检没过（Filter 报错），拦截器将永远处于“待机”状态。

**下一站建议**：完成中间件深度探索后，建议开启「数据库接入实战」，将当前的内存 Map 替换为真正的数据库存储。
