# 04 - Spring Boot 中间件深度解析：Filter vs Interceptor

## 核心心智映射 (Core Mental Mapping)

在 Node.js (Express) 中，中间件（Middleware）通常是线性的。但在 Spring Boot 中，请求会经历一个“洋葱模型”。你需要明确“谁在外面守大门，谁在里面维持秩序”。

| 特性 | Filter (过滤器) | Interceptor (拦截器) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **层级** | Servlet 容器级 (如 Tomcat) | Spring MVC 框架级 | 大门口 vs 走廊里 |
| **颗粒度** | 粗。只能拿到请求/响应流 | 细。能拿到对应的 Controller 方法 | 拿快递 vs 进屋查户口 |
| **Node.js 对标** | Express Middleware | NestJS Interceptor / Guard | 不同框架层的抽象 |
| **典型场景** | Gzip、全站日志、CORS | 业务鉴权、审计日志、性能统计 | 全局 vs 业务 |

---

## 概念解释 (Conceptual Explanation)

### 1. 执行顺序：洋葱模型
当一个请求进来：
1.  **Filter (Entrance)**: 最先触发。
2.  **DispatcherServlet**: 进入 Spring 的核心处理器。
3.  **Interceptor (preHandle)**: 在执行业务代码前触发。
4.  **Controller**: 你的业务逻辑。
5.  **Interceptor (postHandle)**: 业务逻辑结束后触发。
6.  **Filter (Exit)**: 最后离开。

### 2. 核心区别
Filter 是外部 Servlet 规范定义的，它甚至不知道 Spring 的存在。而 Interceptor 是 Spring 定义的，它可以直接访问 Spring 容器中的任何 Bean 和方法元数据。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 实现一个 Filter
```java
@Component
public class MyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        // 请求进入逻辑
        chain.doFilter(req, res); // 必须调用，否则请求就此中断
        // 请求离开逻辑
    }
}
```

### 实现一个 Interceptor
```java
@Component
public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 返回 true 放行，返回 false 拦截
        return true; 
    }
}
```
> **注意**: 拦截器还需要在 `WebMvcConfigurer` 中显式注册。

---

## 典型用法 (Typical Usage)

### 系统守卫 (Filter)
最适合处理那些与业务逻辑完全解耦的操作：
-   **Logging**: 记录每个请求的耗时。
-   **Security**: 提取 Header 中的 Token（如 SecurityContextFilter）。
-   **CORS**: 统一处理跨域请求。

### 业务管理 (Interceptor)
最适合处理与 Controller 紧密相关的逻辑：
-   **Permission**: 检查当前用户是否有权访问该 Controller 方法。
-   **Params**: 预处理或格式化特殊的请求参数。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察以下执行日志顺序：
```text
>>> [Filter 开始] HTTP GET /api/employees
    >>> [Interceptor 预处理] 执行者: EmployeeController#getAllEmployees
    <<< [Interceptor 后处理] 逻辑执行完毕
<<< [Filter 结束] HTTP GET /api/employees | 耗时: 12ms
```
你会发现，`Interceptor` 被完美包裹在 `Filter` 内部。如果在 Filter 中抛出了异常（如权限没过），请求将永远不会触达 `Interceptor`。这种分层防御机制极大地保护了服务器资源。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Java 的中间件配置相对繁琐。

> **最佳实践 Prompt**:
> "我需要为现有的 Spring Boot 项目增加一个『限流』中间件。
> 1. 请帮我分析：应该选择在 Filter 层还是 Interceptor 层实现？
> 2. 请给出实现代码，并说明如何在 `WebMvcConfigurer` 中将其注册到特定的 `/api/**` 路径下，同时排除 `/api/public/**`。
> 3. 请说明如何在拦截器中通过 AOP 获取当前执行的 Controller 方法上的自定义注解。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Spring Docs: Handler Interceptors](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/interceptors.html) - 官方对拦截器的配置建议。
2. [Baeldung: Filter vs Interceptor](https://www.baeldung.com/spring-mvc-handlerinterceptor-vs-filter) - 经典的对比文章。
3. [Spring Security: Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html) - 了解 Spring Security 是如何利用 Filter 链构建防御体系的。
