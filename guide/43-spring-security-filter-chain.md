# 43 - Spring Security 过滤器链与 Node.js 概念映射

## 核心心智映射 (Core Mental Mapping)

如果你有 Express/NestJS 的开发经验，你一定熟悉“中间件 (Middleware)”。Spring Security 的核心也是一套类似的机制，叫 **Security Filter Chain**。

| 维度 | Node.js (Express / Nest) | Java (Spring Security) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **拦截层级** | 框架路由级 (Middleware) | **Servlet 级 (Filter)** | 更底层的安检关卡 |
| **认证上下文** | `req.user = payload` | **`SecurityContextHolder`** | 全局可读的身份盒子 |
| **执行顺序** | 按 `app.use` 声明顺序 | **按 Filter Order 排序** | 严谨的关卡序列 |
| **权限控制** | 自定义 Guard 或中间件 | **`@PreAuthorize` 注解** | 声明式的准入规则 |
| **认证对象** | 纯 JSON 对象 | **`Authentication` 对象** | 强类型的凭证实体 |

---

## 概念解释 (Conceptual Explanation)

### 1. 为什么要用 Filter 而非 Interceptor？
-   **Interceptor**: 属于 Spring MVC，只能拦截进入 Controller 的请求。
-   **Filter**: 属于 Servlet 规范，比 Interceptor 更早触发。这意味着在请求还没解析 URL 之前，安全框架就能把恶意请求挡在门外（如 SQL 注入、非法 Token）。

### 2. SecurityContextHolder (安全上下文)
这是 Spring Security 的灵魂。它使用 `ThreadLocal` 绑定当前线程。
-   你可以随时随地（包括 Service 层）通过 `SecurityContextHolder.getContext().getAuthentication()` 拿到当前登录人的身份，而不需要在方法参数里层层传递。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心组件
-   **`OncePerRequestFilter`**: 确保每个请求只被拦截一次的基类。
-   **`HttpSecurity`**: 用于编排过滤器链的配置类（链式调用）。
-   **`SimpleGrantedAuthority`**: 权限/角色的封装类。**注意：Spring Security 默认角色前缀是 `ROLE_`。**

---

## 典型用法 (Typical Usage)

### 1. 定义 JWT 过滤器
```java
public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        // 1. 解析 Header
        // 2. 校验 Token
        // 3. 构建 Authentication 并存入 SecurityContextHolder
        // 4. chain.doFilter(request, response) 放行
    }
}
```

### 2. 配置安全规则
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    return http
        .csrf(csrf -> csrf.disable()) // 无状态 API 禁用 CSRF
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll() // 白名单
            .anyRequest().authenticated()               // 其余必验
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **权限注解机制 (@PreAuthorize)**:
在 Controller 方法上标注 `@PreAuthorize("hasRole('ADMIN')")`。
它的底层原理是：Spring 在执行方法前，会去 `SecurityContextHolder` 里查找是否存在名为 `ROLE_ADMIN` 的权限。这种“声明式”的权限管理，让你的业务代码不再充斥着 `if-else` 的权限判断，实现了真正的关注点分离。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Spring Security 是 Java 最复杂的框架之一，配置极易出错。

> **最佳实践 Prompt**:
> "我正在为一个 Spring Boot 3 应用配置 Spring Security。
> 1. 请帮我编写一个 `SecurityConfig`，支持 JWT 无状态认证。
> 2. 请设置以下规则：`/api/public` 允许匿名访问，`/api/admin` 仅限 ADMIN 角色，其余需要登录。
> 3. 请生成如何处理『401 未登录』和『403 无权限』的自定义 `AuthenticationEntryPoint` 和 `AccessDeniedHandler`，并返回标准 JSON。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture) - 掌握过滤器链的宏观视野。
2. [Baeldung: Guide to @PreAuthorize](https://www.baeldung.com/spring-security-expressions) - 深入表达式权限控制。
3. [Understanding SecurityContextHolder](https://www.baeldung.com/spring-security-context) - 彻底理解安全上下文的存储逻辑。
