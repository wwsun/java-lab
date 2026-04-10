---
title: "API 文档集成：SpringDoc 与 Knife4j"
description: "告别手写文档，像 NestJS 一样用注解自动生成高颜值在线联调接口文档"
---

# 09 - API 文档集成：SpringDoc 与 Knife4j

## 核心心智映射

在 Node.js 中，如果我们用纯 Express，很可能要手写 Markdown 甚至在 YApi 等平台手动维护接口文档。如果我们用了 NestJS，往往会引入 `@nestjs/swagger` 插件：

1. 在 `main.ts` 中写一段 `SwaggerModule.setup()`。
2. 在 Controller 上打 `@ApiTags('用户模块')`。
3. 在 DTO 的字段上打 `@ApiProperty({ description: '用户名' })`。

在 Java 生态里，这一切几乎是**一模一样**的，概念完全对标：

| Node.js (NestJS + Swagger) | Java (Spring Boot + SpringDoc + Knife4j) | 作用 |
| :--- | :--- | :--- |
| `SwaggerModule.setup(...)` | 引入依赖，写一份 `@Configuration` 即可 | 初始化与注册中间件 |
| `@ApiTags('xxx')` | `@Tag(name = "xxx")` | 给整个 Controller / 接口分组命名 |
| `@ApiOperation({ summary: 'xxx' })` | `@Operation(summary = "xxx")` | 给单个接口命名并说明作用 |
| `@ApiProperty({ description: 'xxx' })` | `@Schema(description = "xxx")` | 给实体类（DTO）的字段增加文档说明 |

> 💡 **什么是 Knife4j？**
> `SpringDoc` 负责底层的规范解析（OpenAPI 3 规范）和 JSON 数据生成，它自带的 Swagger UI 有点简陋。而在国内企业开发中，几乎家家都用 **Knife4j**。它本质上就是一个增强版的 UI 皮肤，颜值更高，支持接口直接在线调试发送 Token 甚至导出离线文档。

---

## 关键依赖与配置

在 Spring Boot 3（基于 Jakarta EE）中，我们需要引入专门匹配 OpenAPI 3 规范的依赖：

**1. 引入 Maven 依赖 (`pom.xml`)**

```xml
<!-- 引入 Knife4j，内部自动包含了 springdoc-openapi-starter-webmvc-ui -->
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

**2. 核心配置 (`application.yml`)**

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html # 原生 UI 路径
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs
knife4j:
  enable: true                      # 开启 Knife4j 增强功能
  setting:
    language: zh_cn                 # 设置为中文
    enable-swagger-models: true     # 显示 Schema 数据模型
```

---

## 核心接口与注解套用

这是一份带上了完整 API 文档注解的标准化 CRUD 代码示例。一旦项目启动，访问 `http://localhost:8080/doc.html` 就能看到漂亮的文档。

### 1. DTO 实体文档 (`@Schema`)

```java
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "用户登录请求参数")
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户登录账号", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
```

### 2. Controller 文档 (`@Tag` & `@Operation`)

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理接口", description = "包含用户的注册、登录、信息查询等接口")
public class UserController {

    @Operation(summary = "用户登录", description = "使用账号密码进行登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginDTO loginDTO) {
        // ... 登录逻辑
        return Result.success("mock-jwt-token");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserInfoDTO> getCurrentUser() {
        // ... 取信息逻辑
        return Result.success(new UserInfoDTO());
    }
}
```

---

## 最佳实践

1. **环境隔离保护**：文档千万不要暴露到生产环境（会被黑客当成菜谱）。在应用中可以使用 Spring Profile 工具，如：在 `@Configuration` 配置类上加 `@Profile({"dev", "test"})`，或者在 prod 环境的 yml 中设置 `knife4j.enable: false`。
2. **全局 Token 注入**：由于我们的接口很多都被 Spring Security 保护，每次在这个页面上调试如果不带 Token 会报 401。Knife4j 非常贴心，可以在后台配置一个名为 `Authorization` 的全局请求头（`Global Parameters`），把你在登陆接口拿到的 JWT Token 输进去，它会在后续所有调试接口里自动帮你带上这个 Header。
3. **配合 JSR-303 使用**：也就是结合我们上个阶段学的 validation 注解，通常只在入参字段上说明类型。因为你的 `@NotNull` / `@Size` 这些注解本身就会被提取识别为 OpenAPI 结构的一部分，自动体现到文档里。

---

## 扩展阅读

1. [Knife4j 官方文档 (Spring Boot 3 版本)](https://doc.xiaominfo.com/docs/quick-start) - 国内开发者一定要收藏的文档网站，很多精细的 UI 配置都在里面。
2. [OpenAPI 3 规范入门](https://swagger.io/specification/) - 了解底层规范，这可以帮助你开发自己公司内部的代码生成器或者 API 网关层工具。
