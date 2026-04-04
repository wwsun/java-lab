# 21-实战验证：IoC 与 DI 的“安全性”深度实验

本指南旨在通过两个维度（**运行时日志** vs **单元测试模拟**），让您彻底掌握 Spring Boot 依赖注入的设计哲学。

---

## 🎯 实验设计思路：为什么要“脱离 Spring”验证？

在 Spring 环境内，`@Autowired` 几乎是无感的。但真正的架构高手会思考：**“如果有一天我的代码离开了框架，它还能跑吗？”**

- **IoC 的本质**：是容器在帮我们管理生命周期。我们通过 `IoCDemoRunner` 观察容器内的“全家福”。
- **DI 的安全性**：是关于代码的**健壮性**。我们通过 `DISafetyTest` 模拟“忘记初始化依赖”的极端情况，观察两种注入方式的防御能力。

---

## 🧪 实验 1：Bean 的生命周期与容器盘点

### 📍 涉及代码
- [`IoCDemoRunner.java`](../src/main/java/com/javalabs/basics/IoCDemoRunner.java)
- [`EmployeeServiceImpl.java`](../src/main/java/com/javalabs/service/impl/EmployeeServiceImpl.java) 中的 `@PostConstruct`

### 🚀 运行与观察
1. **启动应用**：运行 `JavaLabsApplication` 或执行 `mvn spring-boot:run`。
2. **观察顺序**：
    - 首先看 `EmployeeServiceImpl`：你会看到 `🌟 [IoC 验证]` 日志，它在 Web 端口开启**之前**就打印了。说明 Bean 的初始化是一个后台预热过程。
    - 然后看 `IoCDemoRunner` 的盘点：注意 `employeeController` 的类型显示为 `EmployeeController`，这证明了它已被容器正确托管。

---

## 💉 实验 2：依赖注入的安全抗压测试

这是本次实验的重头戏，我们将模拟离开 Spring 的温床，通过手动 `new` 对象来体验注入差异。

### 📍 涉及代码
- [`DISafetyTest.java`](../src/test/java/com/javalabs/basics/DISafetyTest.java)

### 🚀 运行与演示步骤
1. **在 IDE 中定位到 `DISafetyTest`**。
2. **运行 `fieldInjectionTest`**：
    - **现象**：测试通过（意即：它确实抛出了 `NullPointerException`）。
    - **思考**：在 Node.js 中，如果您在 class 里写了一个字段没初始化就调用，也会报 `undefined is not a function`。属性注入的致命点在于：它是**隐式**的，编译器无法在写代码时提醒你忘了填这个坑。
3. **尝试破坏 `constructorInjectionTest`**：
    - **操作**：把测试代码中的 `new EmployeeController(mockService)` 改为 `new EmployeeController()`。
    - **现象**：**编译器直接变红！**
    - **结论**：这就是 **Constructor Injection (Lombok @RequiredArgsConstructor)** 的伟大之处。它利用 Java 语言的原生约束（构造函数签名），确保了没有任何一个 Controller 可以在依赖丢失的情况下“早产”。

---

## 📖 核心总结与 Node.js 对标

| 概念 | Spring 表现 | Node.js TS 类比 |
| :--- | :--- | :--- |
| **@Component** | 告诉 Spring：“请记住这个类” | 类似 NestJS 的 `@Injectable()` 注册 |
| **@PostConstruct** | “Bean 组装好后，立刻执行我” | 类似 `onModuleInit()` 钩子 |
| **构造器注入** | 利用 Java 构造函数传参 | `constructor(private readonly srv: Srv)` |

---

> [!TIP]
> **动手建议**：
> 请尝试修改 `IoCDemoRunner` 中的过滤器，看看能不能搜出 `jacksonObjectMapper` 这个由 Spring Boot 提供的神秘“JSON 转换元勋”。

**关联任务清单**：[task.md](../.gemini/antigravity/brain/e09d359a-7a64-4473-8d18-192ebd1ea234/task.md)
