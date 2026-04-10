# 17 - Lombok Essentials：用注解消灭 Java 样板代码

## 核心心智映射

对 Node.js / TypeScript 开发者来说，Lombok 最像“编译期自动补全器”。

- TypeScript 里声明一个类或接口，很多访问器代码根本不用写
- Java 里如果完全手写，`getter`、`setter`、构造器、`toString()` 会迅速淹没业务逻辑
- Lombok 在编译期帮你生成这些重复代码，源码保持简洁，最终 `.class` 文件仍然是标准 Java 字节码

| 场景 | Node.js / TypeScript | Java + Lombok | 你应该怎么理解 |
| --- | --- | --- | --- |
| 数据对象定义 | `interface` / `type` / class | `@Data` / `@Getter` / `@Setter` | 用注解声明“需要哪些样板能力” |
| 依赖注入 | 构造函数参数属性 | `@RequiredArgsConstructor` | 用 `final` 字段表达依赖不可变 |
| 链式构建 | 对象字面量 / builder helper | `@Builder` | 字段多时可读性更高 |
| 日志字段 | 手写 `logger` | `@Slf4j` | 避免重复声明日志对象 |

## 概念解释

### 1. Lombok 到底做了什么

Lombok 是 Annotation Processor，不是运行时框架。

- 你执行 `mvn compile` 时，Lombok 会在编译阶段改写 AST
- 源码里没写的方法，编译后会真实存在于字节码中
- 这也是为什么 IDE 没配好时会“代码全红”，但命令行编译可能仍然成功

### 2. 为什么 Java 项目里几乎都会用 Lombok

因为它解决的是 Java 的高频重复劳动：

- Entity 要写字段访问器
- DTO / VO 要写构造器和 `toString()`
- Service / Controller 要写构造器注入
- 日志类要反复声明 `Logger`

这类重复代码没有业务价值，但会显著拉低可读性。

## 环境配置

### 1. Maven 依赖

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 2. IntelliJ IDEA 配置

必须确认两件事：

1. 安装或启用 Lombok 插件
2. 开启 `Annotation Processing`

路径通常是：

`Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors`

如果这里没开，你会看到“源码里没有 getter/setter，所以 IDE 报错”的假红线。

## 本项目里的推荐用法

### 1. Entity / DTO / VO

在这个学习项目里，Entity 和大部分 DTO 都优先使用 Lombok。

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
}
```

推荐组合：

- Entity：`@Data` + `@NoArgsConstructor` + `@AllArgsConstructor`
- 字段很多的 DTO：可额外加 `@Builder`
- 只读对象：可考虑 `@Value` 或 `record`

### 2. Spring 依赖注入

比起字段注入，更推荐构造器注入。

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
}
```

这里的心智模型很像 TypeScript 的构造函数参数依赖：

- 依赖必须在创建时就注入
- `final` 表示依赖不可变
- 单元测试时也更容易手动 new 出对象

### 3. 日志字段

```java
@Slf4j
@Component
public class LoginService {

    public void login(String username) {
        log.info("user login, username={}", username);
    }
}
```

不再需要每个类都手写一遍 `private static final Logger logger = ...`。

## 常用注解速查

- `@Data`
  一次性生成 `getter`、`setter`、`toString()`、`equals()`、`hashCode()`
- `@Getter` / `@Setter`
  当你不想直接用 `@Data` 时，更细粒度
- `@Builder`
  适合字段多、可选参数多的对象
- `@NoArgsConstructor`
  适合序列化、反序列化、ORM 框架
- `@AllArgsConstructor`
  快速生成全参构造器
- `@RequiredArgsConstructor`
  为所有 `final` 字段生成构造器，是 Spring 注入常用方案
- `@Slf4j`
  自动生成日志对象
- `@Value`
  生成不可变对象，类似“只读版 DTO”

## 最佳实践

### 1. 优先用 `@RequiredArgsConstructor` 代替字段注入

这比 `@Autowired` 标在字段上更稳健：

- 依赖一眼可见
- 测试更容易
- 对象状态更完整

### 2. Entity 不要手写一堆重复访问器

本项目约定就是：

- Entity 使用 Lombok
- 不手写成批的 `getter/setter`

这能把注意力留给字段设计、映射关系和业务逻辑。

### 3. `boolean` 字段命名要小心

P3C 对布尔命名比较敏感，例如：

- 推荐：`private Boolean deleted;`
- 谨慎使用：`private Boolean isDeleted;`

原因是 Lombok 生成访问器时，`isXxx` 命名容易让 JSON 序列化、框架反射和团队约定出现歧义。

## 反模式

### 1. 在复杂继承体系里无脑使用 `@Data`

`equals()` / `hashCode()` 可能没有把父类字段算进去。

如果确实存在继承结构，需要显式考虑：

```java
@EqualsAndHashCode(callSuper = true)
```

### 2. 在业务服务类上滥用 `@Data`

Service / Controller 通常不需要 `setter`。

这些类更常见的组合应该是：

- `@Service` / `@RestController`
- `@RequiredArgsConstructor`
- `@Slf4j`

而不是 `@Data`。

### 3. 忘记 IDEA 的注解处理配置

这是 Lombok 初学者最常见的问题，排查优先级很高。

## 配套示例解读

假设你有一个 `Book` 实体：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private String author;
}
```

虽然源码只有字段，但编译后你仍然可以直接调用：

- `book.getTitle()`
- `book.setAuthor("xxx")`
- `book.toString()`

这就是 Lombok 的核心价值: 让“数据对象的噪音”从源码里消失。

## AI 辅助开发实战建议

可以直接让 AI 按项目规范重构旧代码：

> 请把这个 Java 类重构为 Lombok 写法，并遵守以下约束：
> 1. Entity / DTO 使用 Lombok 注解，不手写 getter/setter；
> 2. Spring 依赖注入改为 `final` + `@RequiredArgsConstructor`；
> 3. 如果存在 `boolean` 字段，请检查命名是否符合 P3C 约定；
> 4. 如果类是业务服务类，不要使用 `@Data`。

## 扩展阅读

1. [Project Lombok Official Features](https://projectlombok.org/features/all)
2. [Baeldung: Introduction to Lombok](https://www.baeldung.com/intro-to-project-lombok)
3. [Alibaba Java Manual / P3C](https://github.com/alibaba/p3c)
