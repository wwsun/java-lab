# 46 - Web 安全风险防范与 Node.js 概念映射

## 核心心智映射 (Core Mental Mapping)

作为 Web 开发者，不仅要让功能“能跑”，更要让它“安全”。这一节我们聚焦于 **OWASP Top 10** 中最常见的几类漏洞，并对比 Node.js 与 Java 的不同防御手段。

| 风险类型 | Node.js (Helmet / mysql2) | Java (Spring Security / MyBatis) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **SQL 注入** | `db.query('... ?', [id])` | **MyBatis `#{id}` 占位符** | 预编译，拒绝对话 |
| **CSRF 攻击** | `csurf` 中间件 | **JWT 模式下天然免疫** | 不发 Cookie 即不中招 |
| **XSS 攻击** | `helmet` 设置响应头 | **`http.headers()` 链式配置** | 强化瀏覽器防卫 |
| **点击劫持** | `X-Frame-Options` | **`frameOptions().deny()`** | 禁止被嵌在 iframe 中 |
| **限流防刷** | `express-rate-limit` | **Guava RateLimiter / Redis** | 流量闸门控制 |

---

## 概念解释 (Conceptual Explanation)

### 1. SQL 注入 (SQLi)
-   **风险写法**: MyBatis 中的 `${id}`。它是简单的字符串拼接，黑客可以传入 `1 OR 1=1` 绕过校验。
-   **安全写法**: MyBatis 中的 `#{id}`。它使用 `PreparedStatement`，将输入视为纯参数而非 SQL 命令的一部分。

### 2. 为什么我们可以放心地 `disable().csrf()`？
在 JWT + LocalStorage 架构下，前端通过 `Authorization: Bearer` 手动发送令牌。由于不使用 Cookie，浏览器在跨站请求时不会自动发送身份凭证。没有了“自动携带”这个前提，CSRF 攻击就失去了土壤。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 安全响应头配置
在 `SecurityConfig` 中，Spring Security 默认开启了一系列防护：
-   **`frameOptions().deny()`**: 防止点击劫持。
-   **`httpStrictTransportSecurity()`**: 强制 HSTS (HTTPS)。
-   **`xssProtection()`**: 激活浏览器的 XSS 过滤器。

---

## 典型用法 (Typical Usage)

### 1. MyBatis 层的终极守卫
永远优先使用 `#{}`。只有在需要动态传递表名或排序字段（如 `ORDER BY ${column}`）时才使用 `${}`，且必须在 Java 层对 `${}` 的内容做严格的白名单校验。

### 2. 简易限流实现
在 Spring Boot 中，你可以利用 Google 的 Guava 库快速实现：
```java
private RateLimiter limiter = RateLimiter.create(10.0); // 每秒准入 10 个请求
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **安全响应头**:
当你使用 `curl -I` 查看 API 返回时，你会看到 `X-Content-Type-Options: nosniff`。
-   这是 Spring Security 默认注入的。
-   它告诉浏览器：“不要猜测我的文件类型，我说是 JSON 就是 JSON”。这防止了黑客上传伪装成图片的 HTML 脚本导致的 XSS 攻击。
这些“静默”的防御措施构成了 Java 应用厚实的安全垫。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

安全审计是 AI 的拿手好戏。

> **最佳实践 Prompt**:
> "请对我这段 `OrderMapper.xml` 中的 SQL 语句进行安全审计。
> 1. 请检查是否存在可能导致 SQL 注入的 `${}` 占位符。
> 2. 请针对我的 `LoginController` 生成一个基于 IP 的简单限流 Filter 逻辑片段。
> 3. 请检查我的 `SecurityConfig` 是否遗漏了必要的安全响应头（如 CSP 策略）。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [OWASP Top 10 Guide](https://owasp.org/www-project-top-ten/) - 全球 Web 安全风向标。
2. [Spring Security: Default Security Headers](https://docs.spring.io/spring-security/reference/servlet/exploits/headers.html) - 了解每一行 Header 的深意。
3. [Baeldung: Guide to Rate Limiting in Spring](https://www.baeldung.com/spring-rate-limiting) - 从单机到分布式的限流演进。
