# 45 - Spring Security CORS 跨域配置与 Node.js 概念映射

## 核心心智映射 (Core Mental Mapping)

跨域 (CORS) 是前后端分离架构中的第一道坎。在 Node.js 中，一行 `app.use(cors())` 往往能解决，但在 Java Spring Security 中，由于存在多层拦截器和过滤器，配置需要更加精准。

| 维度 | Node.js (Express `cors`) | Java (Spring Security CORS) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **拦截层级** | 业务中间件 (Router 中) | **过滤器链顶层 (Security Filter)** | 最外层的关卡 |
| **预检请求** | 中间件自动处理 OPTIONS | **必须显式开启 `.cors()`** | 自动应答 OPTIONS |
| **配置粒度** | 通常全局配置 | **UrlBasedCorsConfigurationSource** | 精细化的路径控制 |
| **失效陷阱** | 较少，除非放在路由后 | **MVC 级配置会被 Security 拦截** | 优先级决定胜负 |

---

## 概念解释 (Conceptual Explanation)

### 1. 为什么会有 CORS 限制？
浏览器出于安全考虑，限制了 `localhost:3000` (前端) 访问 `localhost:8080` (后端)。CORS 就是后端给浏览器的一个“通行证”，通过 Header 告知浏览器哪些源是可信的。

### 2. 预检请求 (Pre-flight OPTIONS)
浏览器在发 `POST` 或自定义 Header 请求前，会先发一个 `OPTIONS`。如果你的安全框架（Spring Security）没有正确放行这个 `OPTIONS` 请求，浏览器会认为跨域失败，连真正的请求都不会发出。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 核心配置类
-   **`CorsConfiguration`**: 定义允许的 Origin、Method、Header。
-   **`UrlBasedCorsConfigurationSource`**: 将配置关联到具体的 URL 路径（如 `/**`）。
-   **`http.cors(...)`**: 在 `SecurityFilterChain` 中激活 CORS 过滤器。

---

## 典型用法 (Typical Usage)

### 1. 定义 CORS 配置源
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000")); // 前端地址
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

### 2. 应用到 SecurityFilterChain
```java
http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **CORS 生效顺序**:
很多新手会问：“为什么我在 `WebMvcConfigurer` 里配置了跨域还是报 CORS 错？”
-   **根本原因**: Spring Security 过滤器的优先级**高于** Spring MVC。
-   当一个请求进来，它还没到 MVC 映射层，就被 Security 过滤器链拦住了。如果 Security 层没有配置 CORS，预检请求 `OPTIONS` 就会被当做非法请求拦截，直接返回 403。
-   因此，在带有 Spring Security 的项目中，**必须在 Security 层级配置跨域**，确保“通行证”在第一关就发出去。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

跨域报错（No 'Access-Control-Allow-Origin' header...）是前端联调最头疼的问题。

> **最佳实践 Prompt**:
> "我正在调试我的 Spring Boot 3 + Vue 3 项目，遇到了跨域报错。
> 1. 请帮我根据 Spring Security 3.2 语法编写一个支持带凭证（Allow Credentials）的 `CorsConfigurationSource`。
> 2. 请解释为什么在配置中使用了 `AllowedOrigins("*")` 时不能同时设置 `AllowCredentials(true)`。
> 3. 我需要暴露出自定义的响应头 `X-Total-Count` 供前端读取，请生成相应的 `setExposedHeaders` 配置代码。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [MDN: Cross-Origin Resource Sharing (CORS)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) - 最权威的标准化解释。
2. [Spring Security Docs: CORS Support](https://docs.spring.io/spring-security/reference/servlet/exploits/cors.html) - 官方配置指南。
3. [Baeldung: CORS with Spring Security](https://www.baeldung.com/spring-security-cors-preflight) - 实战代码模板。
