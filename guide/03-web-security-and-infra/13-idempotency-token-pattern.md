---
title: "接口幂等性防重：Token 方案实战"
description: "理解接口幂等性，并利用 Redis 实现一套可复用的防重令牌机制"
---

# 13 - 接口幂等性防重：Token 方案

## 核心心智映射

在前端，我们通常通过按钮 `disabled` 或者增加加载动画来防止重复点击。但**后端防重**是最后一道防线，因为攻击者可以直接调 API 绕过前端逻辑。

在 Node.js 中，你可能会用 Redis 存一个请求 ID，设置个 10 秒过期时间，如果第二次请求带着同样的 ID 进来，直接拒绝。

在 Java 标准业务开发中，我们常用 **“先申请 Token，再携带 Token 提交”** 的模式：

| 模式 | 流程类比 | 适用场景 |
| :--- | :--- | :--- |
| **前端置灰** | 进门后把门关上 | 仅防止用户误操作 |
| **数据库唯一索引** | 凭身份证入场 | 防止重复注册、重复插入关键数据 |
| **防重 Token (Redis)** | 入场券模式 | 支付、复杂表单提交、重复下单 |

---

## Token 方案三步走

这套流程能彻底解决“同一表单快速双击”或“请求重发”导致的重复业务处理。

### 1. 获取令牌 (Get Token)
前端在打开表单页面或点击提交前，先请求后端一个接口：`/api/idempotent/token`。
后端生成一个唯一的 UUID（如 `token_12345`），存入 Redis 并设置过期时间（如 5 分钟），然后返回给前端。

### 2. 携带令牌提交 (Submit with Token)
前端在 POST 业务请求时，在 Header 中带上这个令牌：`Idempotent-Token: token_12345`。

### 3. 后端校验并删除 (Check & Delete)
后端逻辑（通常写在 Interceptor 或 AOP 中）：
1. 检查 Header 是否含有 Token。
2. 尝试从 Redis 中删除这个 Token (`redis.delete()`)。
   - **删除成功**：说明这是该令牌第一次使用，放行执行业务。
   - **删除失败**：说明 Token 不存在或已被用过，直接返回“请勿重复提交”错误。

> 💡 **核心注意：必须要保证“检查+删除”这两步是原子的！** 在 Java 中通常利用 `redisTemplate.delete(key)` 的返回值（成功为 true，失败为 false）来一次性完成，这就是原子操作。

---

## 关键语法示例：利用自定义注解 + AOP 实现

我们不希望在每个 Controller 方法里都手写防重逻辑，最优雅的方式是自定一个 `@Idempotent` 注解。

### 1. 定义注解
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /** 过期时间，默认 5 秒 */
    long expire() default 5;
}
```

### 2. 切面实现 (伪代码)
```java
@Aspect
@Component
public class IdempotentAspect {
    @Autowired
    private RedisTemplate redisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = ...;
        String token = request.getHeader("Idempotent-Token");
        
        if (StringUtils.isBlank(token)) {
            throw new BusinessException("缺少幂等 Token");
        }

        // 原子删除
        Boolean success = redisTemplate.delete(token);
        if (!success) {
            throw new BusinessException("请勿重复提交或请求已过期");
        }

        return joinPoint.proceed(); // 执行原业务方法
    }
}
```

---

## 反模式 (Anti-Patterns)

1. **先查再删 (Check-Then-Delete)**
   *错误逻辑*：`if(redis.exists(token)){ redis.delete(token); ... }`。
   *后果*：在高并发瞬间，两个请求可能同时通过 `exists` 校验，导致防重失效。**必须使用原子的 `delete` 或 Redis Lua 脚本。**
2. **业务执行失败后不归还 Token**
   *后果*：如果业务因为断网等临时故障失败了，Token 已经被删，用户必须刷新页面重新走整个流程。如果是为了提升体验，有时需要在 catch 块里视情况将 Token 重新塞回去。
3. **将 Token 作为 URL 参数传递**
   *后果*：容易在日志或浏览器历史中泄露。**推荐使用自定义 Request Header。**

---

## AI 辅助开发实战建议

当你决定在项目中落地这一套时，可以让 Agent 帮你写完整的拦截器逻辑。

**使用 Prompt 示例：**
> "我需要在 Spring Boot 项目中实现基于 Redis 的接口幂等性。请帮我写一个自定义注解 @Idempotent 和对应的 HandlerInterceptor。拦截器需要从 Header 获取 'X-Token'，并利用 StringRedisTemplate 检查并删除。如果删除成功则放行，失败则抛出自定义异常。请确保代码符合线程安全和原子性要求。"

---

## 扩展阅读
1. [Redis 深度历险：接口幂等性的艺术](https://juejin.cn/post/6844903823467479047)
2. [为什么 PUT 和 DELETE 必须是幂等的？](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent)
