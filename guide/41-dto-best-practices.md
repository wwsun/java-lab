# Java DTO 最佳实践指南：从 Node.js 视角看数据传输

在 Java Web 开发中，**DTO (Data Transfer Object)** 是连接 Controller 层与 Service 层的核心纽带。对于习惯了 TS 接口（Interface）的开发者来说，Java 里的 DTO 显得略显沉重，但它承载了系统的**边界契约**（Contract）。

## 1. DTO vs Entity 的心智映射

| 层级 | Java 类类型 | 说明 | TS/Node 类比 |
| :--- | :--- | :--- | :--- |
| **API 层** | `DTO` (Request/Response) | 面向前端，包含校验逻辑，屏蔽敏感字段 | `Zod Schema` / `Interface` |
| **业务层** | `Domain Model` (可选) | 处理核心业务逻辑 | `Business Entity` |
| **持久层** | `Entity` (PO) | 与数据库表结构 1:1 映射 | `TypeORM Entity` / `Prisma Model` |

> [!IMPORTANT]
> **永远不要直接把 Entity 返回给前端。** 就像你不会在 Node.js 中直接把整个 MongoDB/PostgreSQL 的原始对象通过 `res.send()` 发出去一样。

## 2. 文件夹/包结构规范

建议在项目中采用以下目录结构：

```text
com.javalabs
├── dto                  # DTO 总包
│   ├── request          # 入参：LoginRequest, UserCreateDTO
│   └── response         # 出参：UserVO, UserBaseInfoResponse
├── entity               # 数据库实体类 (PO)
├── service              # 在此层进行 Entity <-> DTO 的转换
└── controller           # 只接受 DTO，返回 Result<DTO>
```

## 3. 命名与声明技巧

### 灵活使用 Record (Java 16+)
如果你的 DTO 只是纯粹的只读数据块，使用 Java 的 `record` 关键字，它比 Class 配合 Lombok 更简洁，且自带不可变性。

```java
// 极致简洁的入参定义
public record UserLoginRequest(
    @NotBlank(message = "用户名不能为空") 
    String username,
    
    @Size(min = 6, message = "密码至少6位") 
    String password
) {}
```

### 规范命名后缀
*   **Request**: `UserCreateRequest` (明确这是前端发来的)
*   **Response / VO**: `UserResponse` 或 `UserVO` (明确这是发给前端的)

## 4. 如何高效转换？

在 Service 层，你经常需要将 `Entity` 转为 `DTO`。

**方法 A：原生构造器（最快，最直观）**
```java
return new UserVO(user.getId(), user.getUsername());
```

**方法 B：BeanUtils（最省事，但反射有轻微损耗）**
```java
UserResponse response = new UserResponse();
BeanUtils.copyProperties(userEntity, response); // 属性名一致时自动拷贝
```

**方法 C：MapStruct（工程级推荐）**
通过编译时生成代码实现高性能映射，是目前主流大厂的首选。

---
**扩展阅读：**
1. [Baeldung: Entity to DTO Conversion](https://www.baeldung.com/entity-to-dto-conversion-spring)
2. [MapStruct Official Site](https://mapstruct.org/)
