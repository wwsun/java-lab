# 37 - JWT 核心知识与 Node.js 概念映射

作为 Node.js 开发者，你可能已经非常熟悉令牌认证。在 Java 领域，JWT (JSON Web Token) 的核心哲学完全一致，但工具链和类型安全性要求会有显著差异。

## 1. 核心心智模型：JWT 到底是什么？

### 生活类比：登机牌
想象你去机场值机：
1. **认证 (Authentication)**：你出示护照（用户名/密码），柜台确认是你。
2. **签发 Token**：柜台给你一张**登机牌** (JWT)。
3. **授权 (Authorization)**：安检和登机口**只看登机牌**（不看护照）。登机牌上写着你的座位、航班号。只要登机牌上的“航空公司盖章”（签名）是真的，他们就信任你。

### Node.js vs Java 类比表

| 维度 | Node.js (jsonwebtoken) | Java (JJWT / java-jwt) |
| :--- | :--- | :--- |
| **引入方式** | `npm install jsonwebtoken` | `pom.xml` 添加依赖 (如 `jjwt-api`) |
| **密钥管理** | 常存放在 `.env` | 常存放在 `application.yml` |
| **对象模型** | Plain Object `{ id: 1 }` | 强类型 Claims 对象或 Map |
| **异步处理** | 回调或 Promise | 通常是同步阻塞（除非使用 WebFlux） |

---

## 2. JWT 的三段式结构（必须烂熟于心）

一个典型的 JWT 看起来像：`xxxxx.yyyyy.zzzzz`

1. **Header (头部)**：声明类型 (JWT) 和算法 (如 HS256)。
2. **Payload (负载)**：包含声明 (Claims)。比如 `sub` (主题), `iat` (签发时间), 自定义 `userId`。
3. **Signature (签名)**：用 Header + Payload + Secret Key 算出来的哈希值。

> [!CAUTION]
> **Payload 是公开的！** 它只是 Base64Url 编码。任何人拿到 Token 都能看到 Payload 内容。
> **绝不要在 Payload 中存储密码、手机号等私密数据。**

---

## 2.5 如何在 HTTP 请求中传输 JWT？

在前后端分离的架构中，JWT 通常不通过 Cookie 发送，而是放在 HTTP 请求的 **Header** 中。这是行业的事实标准：

### 标准格式
```http
Authorization: Bearer <Your_JWT_Token>
```

### 核心要点：
1. **Key**：`Authorization` (首字母大写)。
2. **Prefix**：`Bearer` (后面跟一个空格)。这个前缀源自 OAuth 规范，意为“持有者”，表示谁持有这个令牌谁就有权限。
3. **Value**：完整的三段式 JWT 字符串。

### Node.js 发送与校验类比：
* **前端 (Axios)**：
  ```javascript
  axios.get('/api/profile', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  ```
* **后端抽取出 Token**：
  ```javascript
  const authHeader = req.headers.authorization;
  if (authHeader && authHeader.startsWith('Bearer ')) {
    const token = authHeader.substring(7); // 截取掉 "Bearer " (7个字符)
  }
  ```

---

## 2.6 深度对比：为什么选择 Bearer 而非 Cookie？

这是面试高频题，也是架构设计的核心选择。

| 维度 | Cookie (传统) | Bearer Token (推荐) |
| :--- | :--- | :--- |
| **CSRF 攻击** | **风险大**。浏览器会自动携带 Cookie，请求会被伪造。 | **天然免疫**。Header 不会被浏览器自动携带，必须由代码注入。 |
| **客户端支持** | 仅浏览器支持良好。 | 浏览器、原生 App、小程序、IoT 设备完美支持。 |
| **跨域 (CORS)** | 限制多，配置复杂。 | 非常自由，是前后端分离/微服务架构的首选。 |
| **状态管理** | 倾向于配合 Session 使用（有状态）。 | 配合 JWT 使用，实现彻底的**无状态 (Stateless)**。 |

**核心结论：**
使用 `Authorization: Bearer` 是为了**安全性**（防 CSRF）和**灵活性**（跨平台）。在 Java 领域，由于 Spring Security 默认开启 CSRF 保护，如果你选择 Cookie，配置会变得非常繁琐；而选择 Bearer 则能顺应现代 REST API 的潮流。

---

## 3. Java 实战：使用 JJWT 库

### 第一步：添加依赖 (Maven)

在 `pom.xml` 中引入目前 Java 界最流行的 `jjwt` 库（Spring Boot 3.x 推荐）：

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- 或 jjwt-gson -->
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 第二步：编写 JwtUtils 工具类

```java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    // 生产环境中应从配置文件读取，且长度至少 32 位
    private static final String SECRET_STRING = "your-super-secret-key-that-must-be-very-long";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    /**
     * 生成 Token
     */
    public static String createToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username) // 标准载荷
                .claim("userId", userId) // 自定义载荷
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析并验证 Token
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

## 4. 九成开发者都会纠结的三个问题

### Q1：既然 Payload 能被别人看到，那安全性在哪？
安全性在于**防篡改**。如果黑客修改了 Payload 里的身份，因为他没有你的 `SECRET_KEY`，他就无法重新计算出正确的 `Signature`。服务端在 `parseToken` 时会抛出异常。

### Q2：JWT 既然是无状态的，用户点击“注销”怎么办？
这是 JWT 的痛点。
*   **方案 A (推荐)**：缩短过期时间（如 15 分钟），并配合 Refresh Token。
*   **方案 B (混合式)**：在 Redis 中维护一个“黑名单”，注销时将 Token 放入并设定过期时间。

### Q3：JWT 应该存放在 Cookie 还是 LocalStorage？
*   **LocalStorage**：最简单，Node.js 社区常用。但容易被 XSS 脚本窃取。
*   **HttpOnly Cookie**：更安全（脚本读不到），但要注意 CSRF 防护。
*   **结论**：在 Java 现代架构中，前后端分离项目**强制推荐**使用 `Authorization: Bearer <token>` 放在 Header 中。这种方式天然免疫 CSRF 攻击，且更符合 RESTful API 规范。

---

## 5. 开发实践：验证你的理解

1.  **运行指引**：
    *   在我们的项目中创建一个 `test` 目录（如果还没有）。
    *   编写一个 `JwtTest.java`。
    *   运行 `testCreateAndParse` 方法，观察 `Claims` 的输出。
2.  **设计思路**：
    *   尝试把生成的 Token 复制到 [jwt.io](https://jwt.io)，看看是否能直接看到你的 `userId`。
    *   修改 Token 中的一个字母，再尝试解析，观察抛出的异常类型。

## 扩展阅读
1. [RFC 7519 - JSON Web Token (JWT) Specification](https://datatracker.ietf.org/doc/html/rfc7519)
2. [Introduction to JSON Web Tokens (jwt.io)](https://jwt.io/introduction/)
3. [Spring Security 与 JWT 整合最佳实践](https://www.baeldung.com/spring-security-oauth2-jwt-prospects)
