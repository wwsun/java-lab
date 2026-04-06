# 45 - Spring Security CORS 跨域配置与 Node.js 概念映射

在前后端分离（Decoupled）架构中，跨域（CORS）几乎是每个开发者都会遇到的第一道坎。在 Node.js 中，由于中间件模型非常简单，我们习惯于一行 `app.use(cors())` 解决所有问题，但在 Java Spring Security 中，由于存在多层过滤器链，配置需要更加精细。

## 1. 核心概念：为什么需要 CORS？

**浏览器同源策略 (Same-Origin Policy)** 限制了一个域的脚本（如 `localhost:3000` 里的 Vue/React）访问另一个域的资源（如 `localhost:8080` 里的 Java 服务）。

CORS (Cross-Origin Resource Sharing) 是一套允许服务器通过 **HTTP Header** 告诉浏览器：“这个域的请求是合法的，请放行”。

---

## 2. Node.js vs Java Spring Security 概念映射

| 维度 | Node.js (Express `cors`) | Java (Spring Security CORS) |
| :--- | :--- | :--- |
| **拦截层级** | 业务中间件 (Router 中) | 过滤器链的最顶层 (Security Filter) |
| **配置对象** | `cors({ origin: '*' })` | `CorsConfiguration` / `UrlBasedCorsConfigurationSource` |
| **预检请求 (OPTIONS)** | 通常由中间件自动拦截并返回 | Spring Security 必须显式开启 `.cors()` 才能正确自动处理 OPTIONS |

---

## 3. 预检请求 (Pre-flight OPTIONS) 的陷阱

这是 Java 开发者最常踩的坑：
1. 浏览器在发送实际请求（如 `POST` 或带有自定义 Header 的 `GET`）之前，会先发一个 `OPTIONS` 请求。
2. 如果 Spring Security 没有正确放行 `OPTIONS`，浏览器就会认为服务器不支持跨域，直接报错，压根不会去发真正的 `POST` 请求。

**结论**：在 Spring Security 架构下，CORS 配置必须在 **安全拦截层前** 生效。

---

## 4. 如何在 Spring Security 中配置 CORS？

我们需要分两步走：

### 第一步：定义配置源 (CorsConfigurationSource)
我们需要创建一个 Bean，规定允许哪些源、方法、Header。

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*")); // 允许所有源（开发环境）
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 应用到所有路径
    return source;
}
```

### 第二步：应用到过滤器链 (HttpSecurity)
在 `securityFilterChain` 中通过一行代码激活它。

```java
http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

---

## 5. 常见问题：为什么 `WebMvcConfigurer.addCorsMappings` 不起作用？

这也是一个经典疑惑。很多 Java 教程教你使用 `addCorsMappings`，但在使用了 Spring Security 的项目中，**Security 过滤器链的优先级高于 Spring MVC**。

请求还没到达 MVC 之前，就被 Spring Security 的 Filter 拦截了。如果 Filter 层没有 CORS 配置，请求就死在半路上了。所以：**带安全框架的项目，请务必在 Security 层配置 CORS**。

---

## 扩展阅读
1. [MDN: Cross-Origin Resource Sharing (CORS)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
2. [Spring Security Reference - CORS Support](https://docs.spring.io/spring-security/reference/servlet/exploits/cors.html)
3. [Baeldung: CORS with Spring Security](https://www.baeldung.com/spring-security-cors-preflight)
