# 41 - Java DTO 最佳实践指南：从 Node.js 视角看数据传输

## 核心心智映射 (Core Mental Mapping)

在 Java Web 开发中，**DTO (Data Transfer Object)** 是连接 Controller 层与 Service 层的核心纽带。对于习惯了 TS 接口（Interface）的开发者来说，Java 里的 DTO 承载了更严格的**边界契约**。

| 领域 | Java 类类型 | 说明 | TS / Node.js 类比 |
| :--- | :--- | :--- | :--- |
| **API 入参** | **Request DTO** | 面向前端，包含字段校验 | Zod / Joi Schema |
| **API 出参** | **VO (View Object)** | 面向前端展示，屏蔽敏感字段 | Response Interface |
| **持久层** | **Entity (PO)** | 与数据库表结构 1:1 映射 | TypeORM / Prisma Model |
| **业务层** | **DTO** | 内部传输的核心数据包 | Business Logic Object |

---

## 概念解释 (Conceptual Explanation)

### 1. 为什么要区分 DTO 与 Entity？
**永远不要直接把 Entity 返回给前端！**
-   **安全性**: Entity 包含密码、逻辑删除位等敏感信息。
-   **鲁棒性**: 数据库表结构的微调不应直接破坏前端接口。
-   **校验**: 入参 DTO 可以携带 `@NotBlank` 等校验注解，而 Entity 往往只映射字段。

### 2. 文件夹/包结构规范
-   **`dto.request`**: 存放登录、创建用户等入参。
-   **`dto.response`**: 存放返回给前端的视图模型（VO）。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 灵活使用 Record (Java 16+)
如果你的 DTO 只是纯粹的数据容器，使用 `record` 会比 Lombok 更简洁：
```java
// 极致简洁的入参定义，自带不可变性
public record LoginRequest(
    @NotBlank String username,
    @Size(min = 6) String password
) {}
```

### 属性拷贝工具
-   **`BeanUtils.copyProperties(src, dest)`**: 浅拷贝，属性名一致时非常方便。
-   **MapStruct**: (推荐) 编译时生成转换逻辑，性能最优。

---

## 典型用法 (Typical Usage)

### 一个标准的数据流向
1.  **Controller**: 接收 `UserCreateRequest` (DTO)，执行参数校验。
2.  **Service**:
    -   将 `Request` 转换为 `User` (Entity)。
    -   调用 Mapper 保存。
    -   将结果 `User` 转换为 `UserVO` (Response) 返回。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **分层处理逻辑**:
在 `UserController` 中，我们接收的是 `dto.UserCreateRequest`。
-   它通过 `@Valid` 确保了数据合法。
-   它不包含 `id`、`createdAt` 等由系统生成的字段，体现了“按需暴露”的原则。
-   即使后端数据库将 `username` 改名为 `account_name`，我们只需修改 Entity 映射，前端看到的 DTO 依然保持稳定。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

编写 DTO 转换逻辑是典型的搬砖活。

> **最佳实践 Prompt**:
> "我有一个 `User` 实体类（包含 15 个字段）和一个 `UserResponse` DTO（只需暴露 5 个字段）。
> 1. 请帮我编写一个转换工具方法，使用 `BeanUtils.copyProperties`。
> 2. 如果我想排除 `password` 和 `internalSalt` 字段，请提供 MapStruct 的映射配置。
> 3. 请说明如何在 Java 21 中利用 Record 来定义这个 `UserResponse` 以获得最佳性能。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Spring: Entity to DTO Conversion](https://www.baeldung.com/entity-to-dto-conversion-spring) - 转换全攻略。
2. [MapStruct Official Site](https://mapstruct.org/) - Java 领域最强对象映射框架。
3. [Java 16 Records Guide](https://www.baeldung.com/java-record-keyword) - 了解不可变数据的未来。
