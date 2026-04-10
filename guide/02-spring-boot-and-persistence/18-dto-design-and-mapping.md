# 18 - DTO 设计与对象映射：别把 Entity 直接暴露给前端

## 核心心智映射

DTO 的本质是“边界契约”。

如果用 Node.js / TypeScript 类比：

- Request DTO 很像带校验规则的 Zod / Joi schema
- Response VO 很像前后端约定好的返回类型
- Entity 则更像 Prisma / TypeORM 的数据库模型

三者职责不同，最好不要混用。

| 类型 | 典型职责 | Node.js / TypeScript 类比 | 是否应直接暴露给前端 |
| --- | --- | --- | --- |
| Request DTO | 接收入参并做校验 | Zod / Joi 输入模型 | 可以 |
| Response VO | 面向前端输出 | API Response Type | 可以 |
| Entity / PO | 数据库映射 | Prisma / TypeORM Model | 不建议 |
| Service DTO | 服务层内部传递 | 业务上下文对象 | 视边界而定 |

## 为什么不能直接返回 Entity

这是 Java Web 初学者最容易踩的坑之一。

### 1. 安全问题

Entity 里常见这些字段：

- `password`
- `deleted`
- `version`
- `createdBy`
- `updatedBy`

它们往往不该返回给前端。

### 2. 接口稳定性问题

数据库字段改名是持久层内部演进，但 API 契约应该尽量稳定。

如果前端直接依赖 Entity，一次表结构微调就可能把接口一起打爆。

### 3. 校验职责不清

Request DTO 可以带：

- `@NotBlank`
- `@Email`
- `@Size`

而 Entity 更偏向数据库映射，不应该塞满“接口输入规则”。

## 推荐的包结构

一个清晰的分层方式如下：

```text
dto/
  request/
  response/
entity/
mapper/
service/
controller/
```

如果项目规模再大一点，也可以按业务模块拆：

```text
user/
  controller/
  dto/
  entity/
  mapper/
  service/
```

## 常见 DTO 类型

### 1. Request DTO

```java
public record UserCreateRequest(
    @NotBlank String username,
    @NotBlank String password,
    @Email String email
) {
}
```

适合接收前端请求，重点是“字段约束”。

### 2. Response VO

```java
@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
}
```

适合返回给前端，重点是“按需暴露”。

### 3. Service 层传输对象

当一个流程跨多个服务时，可以单独定义服务层 DTO，避免 Controller DTO 和 Entity 互相污染。

## 一个标准的数据流

推荐的数据流是：

1. Controller 接收 `UserCreateRequest`
2. `@Valid` 完成参数校验
3. Service 把 Request DTO 转成 Entity
4. Mapper 持久化 Entity
5. Service 再把 Entity 转成 `UserResponse`
6. Controller 返回 `Result<UserResponse>`

这条链路的重点是：边界层、业务层、持久层各自负责自己的对象。

## 对象映射怎么做

### 1. 手动映射

字段不多时最直接，也最清晰。

```java
public UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .build();
}
```

优点：

- 最透明
- 不容易误拷贝敏感字段

缺点：

- 字段多时重复劳动较多

### 2. `BeanUtils.copyProperties`

适合原型期或字段高度一致的场景。

```java
UserResponse response = new UserResponse();
BeanUtils.copyProperties(user, response);
```

风险也很明显：

- 容易把不该拷贝的字段一起带过去
- 对重命名字段支持很弱

### 3. MapStruct

如果项目后续 DTO 数量很多，MapStruct 是更稳定的长期方案。

- 编译期生成代码
- 性能好
- 映射关系清晰

## `record` 还是 Lombok

对第 2 周阶段来说，可以这样判断：

- 入参 DTO：优先考虑 `record`
- 返回 VO：可以用 `record`，也可以用 Lombok
- Entity：本项目优先用 Lombok，不建议直接改成 `record`

原因：

- `record` 天然不可变，适合表达“请求体”和“只读返回值”
- Entity 往往要适配 ORM、序列化和框架反射，更适合 Lombok 普通类

## 反模式

### 1. Entity、DTO、VO 三合一

短期省事，长期一定混乱。

### 2. 直接把前端入参灌进 Entity

这样会让数据库字段设计反向绑架 API 设计。

### 3. 在 DTO 里放业务方法

DTO 更适合做“数据载体”，不是业务规则宿主。

## 配套示例解读

假设数据库字段是：

- `id`
- `username`
- `password`
- `email`
- `deleted`

前端真正需要的响应却只有：

- `id`
- `username`
- `email`

那么 `UserResponse` 的存在意义就是：

- 屏蔽敏感字段
- 稳定 API
- 明确前端能依赖什么

这和 TypeScript 里“数据库模型”和“接口返回类型”分开定义，本质上是同一个工程化动作。

## AI 辅助开发实战建议

> 请基于这张数据表和这个 Controller，帮我拆分出 `Request DTO`、`Response VO` 和 `Entity`：
> 1. Request DTO 需要补齐 Bean Validation 注解；
> 2. Response VO 只暴露前端真正需要的字段；
> 3. 不要直接返回 Entity；
> 4. 提供手动映射版本，如果字段太多再补一个 MapStruct 版本。

## 扩展阅读

1. [Baeldung: Entity to DTO Conversion](https://www.baeldung.com/entity-to-dto-conversion-spring)
2. [MapStruct Official Site](https://mapstruct.org/)
3. [Baeldung: Java Record Keyword](https://www.baeldung.com/java-record-keyword)
