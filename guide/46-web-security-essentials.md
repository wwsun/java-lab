# 46 - Web 安全风险防范与 Node.js 概念映射

作为 Web 开发者，不仅要让功能“能跑”，更要让它“安全”。这一节我们聚焦于 **OWASP Top 10** 中最常见的几类漏洞，以及在 Java + Spring Boot 环境下的“拆解”方案。

---

## 1. SQL 注入 (SQLi) - 数据层防线

### 概念及 Java 实现

SQL 注入是指恶意用户通过输入片段，通过拼接绕过原本的 SQL 逻辑。

| 维度               | Node.js (mysql2/knex)                             | Java (MyBatis-Plus)                      |
| :----------------- | :------------------------------------------------ | :--------------------------------------- |
| **存在风险的写法** | `query("SELECT * FROM users WHERE id = " + id)`   | `${id}`：直接拼接字符串，极度危险。      |
| **安全的写法**     | `query("SELECT * FROM users WHERE id = ?", [id])` | `#{id}`：采用 PreparedStatement 占位符。 |

**核心结论**：MyBatis-Plus 的 `Mapper` 内置方法和 `${}` 默认都是安全的占位符模式。除非你在 XML 里手动写了 `${}` 且没有做充分校验，否则 SQLi 基本被消灭在代码编写期。

---

## 2. CSRF (跨站请求伪造) - 状态层防线

### 为什么我们禁用了 CSRF Protection？

在传统的 Session + Cookie 模式下，浏览器会自动发送 Cookie，这让 CSRF 攻击有机可乘。

**Node.js 回忆**：在 Express 中，我们常使用 `csurf` 中间件给每个表单加一个隐藏的 `_csrf` token。

**Java Spring Security**：

- **默认行为**：开启 CSRF 保护（需要前端在 Header 里带上 `X-XSRF-TOKEN`）。
- **我们的选择**：在 JWT 无状态架构下，由于我们不使用 Cookie 存储 Token（而是手动存在 LocalStorage 的 Header 中），浏览器不会自动携带认证信息。**这种“不自动携带”的特性天然免疫 CSRF**，所以我们可以放心地执行 `http.csrf(disable())`。

---

## 3. XSS (跨站脚本攻击) - 视图层防线

### 如何拦截恶意脚本？

恶意脚本被存入数据库并注入到其他用户的页面中执行。

**Node.js 工具**：你一定用过 `helmet`。
**Java Spring Security**：
`DefaultSecurityFilterChain` 已经内置了一些基础的安全响应头：

- `X-Content-Type-Options: nosniff`: 防止浏览器“猜测”内容类型。
- `X-XSS-Protection: 0`: 现代浏览器更倾向于关闭它转而使用 CSP。
- `X-Frame-Options: DENY`: 防止点击劫持（Clickjacking）。

**我们的增强计划**：在 `SecurityConfig` 中通过 `headers()` 进一步加固。

---

## 4. 限流 (Rate Limiting) - 可用性层防线

### 控制并发流量

为了防止爆破、爬虫或 Ddos，我们需要限制单个 IP 在特定时间内的请求频率。

**Node.js**：`express-rate-limit`。
**Java**：

- **初级版 (本课)**：使用自定义 `Filter` + `ConcurrentHashMap`（单机版，类似 Node.js 内存存储）。
- **进阶版**：使用 Redis + Lua 脚本（分布式环境标配）。
- **框架版**：使用 `Resilience4j-ratelimiter` 或 `Guava RateLimiter`。

---

## 扩展阅读

1. [OWASP Top 10 Summary (2025 版)](https://owasp.org/Top10/2025/0x00_2025-Introduction/)
2. [Spring Security Headers Documentation](https://docs.spring.io/spring-security/reference/servlet/exploits/headers.html)
3. [Baeldung: Guide to Spring Security X-Frame-Options](https://www.baeldung.com/spring-security-x-frame-options)
