# 20-Spring Boot 核心启蒙：现代 Java 后端的工业级范式

欢迎来到 Java 生态的基石 —— **Spring Boot 3.x**。本指南将带您通过 **工程源码** 领略其两大杀手锏：**控制反转 (IoC)** 与 **约定优于配置 (CoC)**。

---

## 1. 核心模型对标：从 Node 到 Java

| 场景         | Node.js (Express/NestJS)         | **Java (Spring Boot)**         | **设计哲学**                      |
| :----------- | :------------------------------- | :----------------------------- | :-------------------------------- |
| **路由层**   | `@Controller()` / `router.get()` | **`@RestController`**          | 处理输入输出的“门卫”              |
| **业务层**   | `@Injectable()` Service          | **`@Service`**                 | 纯粹的领域逻辑处理器              |
| **依赖注入** | `constructor(private srv: Srv)`  | **`@RequiredArgsConstructor`** | “我不需要关心对象怎么 new 出来的” |
| **配置**     | `process.env` / `.env`           | **`application.yml`**          | 类型安全、中心化配置              |
| **设计哲学** | 灵活/手动 (Manual)               | **约定优于配置 (CoC)**         | “能不写的就不写”                  |

---

## 2. Spring Boot 的灵魂：约定优于配置 (CoC)

**Convention Over Configuration** 的核心思想是：**“如果你遵循某种约定，框架就会自动帮你完成剩下的工作。”**

### 🛡️ 案例：为什么我们的项目能直接运行？

观察我们的项目结构：

- **约定 A**：代码必须在 `src/main/java` 下。
- **约定 B**：配置文件必须叫 `application.properties/yml` 并放在 `resources` 下。
- **约定 C**：主启动类（带 `@SpringBootApplication`）所在包及其子包会被自动扫描。只要你遵循这个包名结构，你的 Controller 甚至不用注册就能生效！

---

## 3. 深入理解 IoC：从“独裁”到“托管”

**IoC (Inversion of Control)** 反转了“创建对象的权力”。

### ❌ 过去：开发者是“独裁者” (Manual new)

你负责控制一切。如果你在 `OrderService` 中需要 `AliPayProcessor`，你必须亲自 `new` 出来。

```java
public class OrderService {
    // 开发者必须亲自动手创建实例，类与类之间死死“咬合”在一起
    private AliPayProcessor payProcessor = new AliPayProcessor("app_id", "secret");

    public void processOrder() {
        payProcessor.pay(100);
    }
}
```

**后果**：如果你想换成“微信支付”，你必须修改 `OrderService` 的源码。代码极难测试，无法 Mock。

### ✅ 现在：Spring 是“大管家” (Spring Managed)

你只需提需求，管家帮你找资源。

```java
@Service
@RequiredArgsConstructor // Lombok 自动生成构造器
public class OrderService {
    // 我只需声明：“我需要一个实现了 PaymentProcessor 接口的对象”
    // 至于它是 AliPay 还是 WeChatPay，Spring 启动时会自动“塞”进来
    private final PaymentProcessor payProcessor;

    public void processOrder() {
        payProcessor.pay(100);
    }
}
```

### ⚡ “反转”了什么？

1.  **创建权的权力反转**：从“我主动创建对象”变成了“我被动接受对象”。
2.  **配置的权力反转**：支付网关的秘钥不再写死在代码里，而是由 Spring 从外部配置中心注入。

---

## 4. 源码解剖：我们在工程中写了什么？

### 4.1 `JavaLabsApplication`：项目的“神经中枢”

```java
@SpringBootApplication // 复合注解 = 配置类 + 自动装配开启 + 包扫描开启
public class JavaLabsApplication {
    public static void main(String[] args) {
        // 这一行执行后：1. 启动嵌入式 Tomcat 2. 扫描所有 Bean 3. 建立路由映射图
        SpringApplication.run(JavaLabsApplication.class, args);
    }
}
```

### 4.2 `GreetingController`：简单的 HTTP 转换

```java
@RestController // 告诉 Spring：这个类的返回值直接转为 JSON
public class GreetingController {

    @GetMapping("/api/greeting") // 映射 GET 路径
    public GreetingResponse sayHello(@RequestParam(defaultValue = "Java") String name) {
        // 返回 Java 17 Record，效果等同于 TS 的 Interface 模型
        return new GreetingResponse("Hello, " + name + "!", System.currentTimeMillis());
    }

    public record GreetingResponse(String message, long timestamp) {}
}
```

### 4.3 `EmployeeController`：工业级 CRUD 指挥家

它是最能体现 **三层架构** 的地方：

- **Web 层对开**：使用 `@PostMapping`、`@PutMapping` 等注解精确对应 HTTP 方法。
- **响应控制**：使用 `ResponseEntity.status(HttpStatus.CREATED)` 控制精确的 201 状态码，而非永远返回 200。
- **参数解耦**：`@RequestBody` 自动处理 JSON -> Java 对象的反序列化。

---

## 5. 深度实战演练：IoC & CoC 的联手威力

### 5.1 自动配置 (CoC) 的奇迹

当你在 `pom.xml` 加入 H2 数据库依赖，Spring 会“脑补”出你的数据库连接池。你不需要写一行 JDBC 代码就能直接注入 `DataSource`。

### 5.2 极致简单的单元测试 (IoC 优势)

在 `EmployeeControllerTest` 中，我们使用了 `@MockBean`：

```java
@MockBean
private EmployeeService employeeService;
```

由于 Controller 不自己 `new` 服务，我们可以轻而易举地在测试时把“真服务”换成“假 Mock”，这在强耦合的传统代码中简直是天方夜谭。

---

> [!TIP]
> **本阶段实战示例**：
>
> - 异常处理：[GlobalExceptionHandler.java](../src/main/java/com/javalabs/exception/GlobalExceptionHandler.java)
> - 控制器逻辑：[EmployeeController.java](../src/main/java/com/javalabs/controller/EmployeeController.java)
> - 单元测试：[EmployeeControllerTest.java](../src/test/java/com/javalabs/controller/EmployeeControllerTest.java)

**参考资料**：

1. [Spring Boot: Build System](https://docs.spring.io/spring-boot/3.5/reference/using/build-systems.html)
2. [Spring Boot: Structuring Your Code](https://docs.spring.io/spring-boot/3.5/reference/using/structuring-your-code.html)
3. [Spring Boot 官方文档 - 自动配置原理](https://docs.spring.io/spring-boot/3.5/reference/using/auto-configuration.html#page-title)
4. [Baeldung - Spring @Value 注解全指南](https://www.baeldung.com/spring-value-annotation)
