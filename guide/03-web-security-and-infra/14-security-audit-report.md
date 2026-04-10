---
title: "AI 协同安全审计报告：JWT 与认证体系"
description: "深度剖析现有安全方案，识别潜在漏洞并提供工业级修复建议"
---

# 14 - AI 协同安全审计：JWT 与认证体系

## 核心心智映射

在 Node.js 中，一旦我们引入了 `jsonwebtoken` 和 `passport`，最容易犯的错误就是把 `JWT_SECRET` 直接写在代码里，或者在跨域配置里大开绿灯 `Access-Control-Allow-Origin: *`。

在 Java 的 Spring Security 体系下，由于其复杂的过滤器链（Filter Chain）机制，安全风险往往隐藏在配置细节中。

---

## 🚨 现有漏洞与安全风险审计

基于对 `com.javalabs` 包下代码的深度扫描，我识别出以下 **4 个** 核心风险点：

### 1. 密钥硬编码 (Secret Key Exposure)
- **文件**: [`JwtUtils.java`](file:///Users/weiwei/projj/github.com/wwsun/java-labs/src/main/java/com/javalabs/util/JwtUtils.java)
- **风险描述**: `SECRET_STRING` 被声明为静态常量直接写在源码中。这意味着任何拥有代码读取权限的人（或代码库泄露）都可以伪造合法的 JWT 令牌，从而冒充任意用户（包括管理员）。
- **修复方案**: 将密钥移至 `application.yml` 的环境变量，并使用 `@Value("${jwt.secret}")` 注入。

### 2. 跨域配置过松 (CORS Over-permission)
- **文件**: [`SecurityConfig.java`](file:///Users/weiwei/projj/github.com/wwsun/java-labs/src/main/java/com/javalabs/config/SecurityConfig.java)
- **风险描述**: `setAllowedOrigins(List.of("*"))` 允许来自任何域名的前端请求访问接口。在处理敏感业务（如涉及银行、隐私数据）时，这极易受到跨站攻击。
- **修复方案**: 在生产环境下，务必将 `*` 替换为具体的前端域名（如 `https://app.example.com`）。

### 3. 缺乏 Refresh Token 机制
- **文件**: [`JwtUtils.java`](file:///Users/weiwei/projj/github.com/wwsun/java-labs/src/main/java/com/javalabs/util/JwtUtils.java)
- **风险描述**: 当前 Token 过期时间固定为 24 小时。一旦过期，用户必须重新输入账号密码登录。在现代 Web 应用中，单 Token 方案（Access Token）在安全与体验之间很难平衡：过期时间短，用户体验差；过期时间长，Token 泄露风险大。
- **修复方案**: 引入 **双 Token 机制**。Access Token 设为 15 分钟，Refresh Token 设为 7 天并存储在数据库/Redis 中。

### 4. 敏感路径开发门大开 (Dev backdoor)
- **文件**: [`SecurityConfig.java`](file:///Users/weiwei/projj/github.com/wwsun/java-labs/src/main/java/com/javalabs/config/SecurityConfig.java)
- **风险描述**: `/h2-console/**` 被设置为 `permitAll()`。如果这个服务被打包部署到外网，且 H2 控制台未加密码，任何人都可以通过浏览器查看并修改你的本地数据库。
- **修复方案**: 使用 `@Profile("dev")` 隔离配置，或在生产打包逻辑中强制移除 H2 相关依赖和配置。

---

## 🛡️ 工业级加固建议

### 1. 密钥注入的最佳实践
```yaml
# application-prod.yml
jwt:
  # 通过环境变量注入，不要在 YAML 里留真实密钥
  secret: ${JWT_SECRET_KEY:default-very-long-and-secure-random-string}
  expiration: 86400000
```

### 2. JWT 载荷 (Claims) 的最小化原则
**注意**：不要在 JWT 的 Payload 里存储用户密码、身份证号或手机号。因为 JWT 只是被 Base64 编码，**并没有加密**，任何人截获 Token 后都可以直接看到里面的内容。

### 3. 拦截器顺序 (Filter Order)
确保你的 `JwtAuthenticationFilter` 放在 `UsernamePasswordAuthenticationFilter` 之前。目前的 `SecurityConfig.java` 已经正确执行了这一步（`addFilterBefore`），这是很多初学者的重灾区。

---

## AI 协同审计实战建议

当你需要在项目中引入新的安全特性（如手机验证码登录、OAuth2 社交登录）时，务必让 Agent 多做几次检查。

**建议 Prompt：**
> "我即将添加短信验证码登录接口。请帮我设计该流程的 Security 过滤器逻辑，并针对‘验证码爆破’和‘短信轰炸’这两个安全威胁，给出对应的过滤机制（如限流和验证码失效策略）的代码实现。"

---

## 总结
通过本次审计，你当前的代码已经完成了 **“功能性安全”**（能防简单的未授权访问），但尚未达到 **“生产级加固”**。在接下来的第四周综合实战中，我们将重点解决这些“毛刺”。
