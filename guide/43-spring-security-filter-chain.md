# 43 - Spring Security 过滤器链与 Node.js 概念映射

恭喜进入 **Week 3**！在上一周，我们使用 `HandlerInterceptor` 手动实现了 JWT 认证。这在学习阶段非常管用，但在生产级 Java 项目中，安全职责通常由 **Spring Security** 过滤器链接管。

## 1. 核心概念：什么是 Filter Chain？

在 Node.js (Express/NestJS) 中，我们习惯于“中间件 (Middleware)”：
```javascript
app.use(logger);
app.use(auth);
app.use(router);
```

Spring Security 的核心也是一套类似的机制，叫 **Security Filter Chain**。它像是一道道安检关卡，所有进入 API 的 HTTP 请求都必须依次通过这些关卡。

### Node.js vs Java Spring Security 映射表

| 维度 | Node.js (Express) | Java (Spring Security) |
| :--- | :--- | :--- |
| **拦截层级** | 框架路由级 (Middleware) | Servlet 级 (Filter) |
| **执行顺序** | 按 `app.use` 顺序执行 | 按 Filter Order 执行 (通常由框架编排) |
| **认证上下文** | `req.user = payload` | `SecurityContextHolder.getContext()` |
| **权限控制** | 自定义 Guard 或中间件 | `@PreAuthorize` 注解或规则配置 |

---

## 2. 为什么要从 Interceptor 切换到 Filter？

你可能会问：“我的 `JwtInterceptor` 跑得好好的，为什么要换成 Spring Security 的 `Filter`？”

1.  **更底层的防御**：`Filter` 运行在 Servlet 层，比 Spring MVC 的 `Interceptor` 更早触发。这意味着在 Spring 还没开始解析 URL 映射之前，恶意请求就可以被拦截掉。
2.  **标准化的上下文**：Spring Security 会自动管理“当前登录人”信息。你只需要把解析出的用户信息存入 `SecurityContextHolder`，后续在 Service 层、Controller 层甚至 Audit 审计层都能直接拿到，不再需要手动在 `request.setAttribute` 里传值。
3.  **RBAC 自动化**：你可以直接通过注解（如 `@PreAuthorize("hasRole('ADMIN')")`）来声明某个接口只有管理员能进。

---

## 3. Spring Security 的“过滤器链”长什么样？

当你启动 Spring Security 后，默认会有十几个过滤器在工作。我们通常最关注以下三个动作：

1.  **提取凭证**：从 Header 读取 `Authorization: Bearer <token>`。
2.  **验证并绑定**：验证 Token。如果合法，创建一个 `Authentication` 对象并塞进“上下文盒子”里。
3.  **放行或拦截**：根据配置好的规则（哪些路径白名单，哪些路径要权限）决定请求是继续还是返回 403。

---

## 4. 动手之前的预热

在接下来的重构计划中，我们将：
1.  **定义 Filter**：创建一个继承自 `OncePerRequestFilter` 的类，它负责拦截每个请求并解析 JWT。
2.  **配置 Chain**：在 `SecurityConfig` 中告诉 Spring Security：“请在执行 UsernamePassword 验证之前，先跑一下我的 JWT 过滤器”。
3.  **开启 RBAC**：在方法上加一句代码，就能区分“普通用户”和“管理员”。

---

## 5. 重构核心：从 Interceptor 到 Filter

### 5.1 定义 JWT 认证过滤器 (Filter)
在 Node.js 中，你会写一个 `authMiddleware`。在 Spring Security 中，我们继承 `OncePerRequestFilter`：

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 提取 Token (Authorization: Bearer <token>)
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 无 Token 则放行，交给后续 Security 配置处理
            return;
        }

        // 2. 验证并解析
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtils.parseToken(token);
            String username = claims.getSubject();
            String role = (String) claims.get("role");

            // 3. 核心重构：将身份绑定到 Spring Security 上下文
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 注意：Spring Security 默认角色前缀是 ROLE_
                List<SimpleGrantedAuthority> authorities = 
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 验证失败不在此处抛异常，让上下文保持为空，后续由 SecurityConfig 统一拦截返回 403
        }
        filterChain.doFilter(request, response);
    }
}
```

### 5.2 配置过滤器链 (SecurityConfig)
你需要告诉 Spring Security 何时执行你的过滤器，并开启注解支持：

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启方法级权限控制 (@PreAuthorize)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // JWT 架构通常禁用 CSRF
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // 白名单放行
                .anyRequest().authenticated()               // 其他全部需要认证
            )
            // 核心：在标准用户名密码过滤器之前插入我们的 JWT 过滤器
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## 6. 使用：再也不用 `request.getAttribute`

重构后，你不再需要手动从 `request` 里取值，Spring Security 提供了更优雅的方式。

### 6.1 获取当前登录人
**Before (Interceptor):**
```java
String username = (String) request.getAttribute("username");
```

**After (Spring Security):**
```java
// 方式 A：通过静态上下文获取 (可以在 Service 层甚至任何地方使用)
String username = SecurityContextHolder.getContext().getAuthentication().getName();

// 方式 B：Controller 方法直接注入 Principal 对象
@GetMapping("/me")
public String getCurrentUser(Principal principal) {
    return principal.getName();
}
```

### 6.2 权限控制 (RBAC)
**Before:** 在拦截器里写复杂的 `if (role.equals("ADMIN"))` 判断，或者手动抛出异常。

**After:** 只需要在 Controller 方法上加一个注解，Spring Security 会自动校验 `SecurityContext` 里的角色。
```java
@GetMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')") // 只有角色为 ROLE_ADMIN 的用户才能进入
public List<User> getAllUsers() {
    return userService.list();
}
```

---

## 扩展阅读
1. [Spring Security Architecture (Official)](https://spring.io/guides/topicals/spring-security-architecture)
2. [Servlet Filters vs Spring Interceptors](https://www.baeldung.com/spring-mvc-handlerinterceptor-vs-filter)
3. [Understanding the SecurityContextHolder](https://www.baeldung.com/spring-security-context)
