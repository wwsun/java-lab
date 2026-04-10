# 02 - JWT 核心知识与 Node.js 概念映射

## 核心心智映射 (Core Mental Mapping)

作为 Node.js 开发者，你可能已经非常熟悉令牌认证。在 Java 领域，JWT (JSON Web Token) 的核心哲学完全一致，但工具链和类型安全性要求会有显著差异。

| 维度 | Node.js (jsonwebtoken) | Java (JJWT / java-jwt) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **认证比喻** | 登机牌 (唯一标识) | **登机牌 (唯一标识)** | 凭票准入，无状态 |
| **引入方式** | npm install jsonwebtoken | **pom.xml 添加 jjwt** | 核心三方库集成 |
| **密钥管理** | `.env` 常量 | **application.yml** | 配置外部化 |
| **传输方式** | Bearer Token / Cookie | **Authorization: Bearer** | 行业事实标准 |
| **安全模型** | 无状态 (Stateless) | **无状态 (Stateless)** | 减轻服务器内存压力 |

---

## 概念解释 (Conceptual Explanation)

### 1. JWT 的三段式结构
一个典型的 JWT 看起来像：`xxxxx.yyyyy.zzzzz`
-   **Header (头部)**: 声明类型 (JWT) 和签名算法 (如 HS256)。
-   **Payload (负载)**: 包含声明 (Claims)。**注意：它是 Base64 公开的，严禁存放敏感信息（如密码）。**
-   **Signature (签名)**: Header + Payload + 密钥计算出的哈希。用于防篡改。

### 2. 为什么选择 Bearer 而非 Cookie？
-   **防 CSRF**: 浏览器不会自动携带 Header，天然免疫伪造请求攻击。
-   **跨平台**: 小程序、App、IoT 设备处理 Header 比处理 Cookie 简单得多。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### JJWT 核心 API (Java)
```java
// 1. 生成 Token
String jwt = Jwts.builder()
    .setSubject(username)       // 标准载荷
    .claim("userId", id)        // 自定义载荷
    .setIssuedAt(new Date())    // 签发时间
    .setExpiration(expiryDate)  // 过期时间
    .signWith(key)              // 签名
    .compact();

// 2. 解析并验证
Claims claims = Jwts.parserBuilder()
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token)
    .getBody();
```

---

## 典型用法 (Typical Usage)

### 统一工具类：JwtUtils
建议封装一个 `JwtUtils` 类，包含 `createToken` 和 `parseToken` 方法。
-   **密钥长度**: 生产环境下密钥必须至少 32 位（256位），否则 `jjwt` 会抛出异常保障安全。
-   **过期时间**: 通常设置为 24 小时或配合 Refresh Token 缩短为 15 分钟。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **Token 传输规范**:
前端请求时必须携带以下 Header：
```http
Authorization: Bearer <Your_Token_String>
```
后端逻辑会通过 `authHeader.substring(7)` 剥离掉 `Bearer ` 前缀。这种做法不仅符合 OAuth2 标准，还能让你的 API 具备极佳的通用性。如果黑客修改了 Token 中间的一个字符，签名校验逻辑会立即失效并抛出 `SignatureException`，从而将非法请求挡在业务逻辑之外。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

JWT 的无状态性导致无法主动撤回令牌，这在注销场景下是个挑战。

> **最佳实践 Prompt**:
> "我正在为 Spring Boot 项目设计 JWT 认证系统。
> 1. 请帮我实现一个 `JwtUtils` 工具类，支持签发包含用户角色（Role）的 Token。
> 2. 请建议一种基于 Redis 的『无效 Token 黑名单』方案，以支持用户点击注销时使 Token 立即失效。
> 3. 请生成一个测试用例，模拟 Token 过期时如何捕获 `ExpiredJwtException` 并返回自定义的 JSON 响应。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [JWT.io Introduction](https://jwt.io/introduction/) - 在线调试与结构可视化。
2. [RFC 7519 Specification](https://datatracker.ietf.org/doc/html/rfc7519) - JWT 工业标准。
3. [Baeldung: Guide to JJWT](https://www.baeldung.com/java-jwt) - Java 领域最详尽的教程。
