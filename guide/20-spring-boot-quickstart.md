# 20-Spring Boot 启蒙：从手动 new 到 IoC 容器的多维跨越

欢迎来到 Java 后端开发的核心。如果您曾使用过 Node.js 的 **NestJS** 框架，您会发现 Spring 的设计哲学简直如出一辙。

## 1. 核心模型对标：手动 vs 自动

| 概念 | Node.js (Pure JS) | Node.js (NestJS) | **Java (Spring Boot)** |
| :--- | :--- | :--- | :--- |
| **对象创建** | `const srv = new Service()` | `@Injectable()` 类 | **`@Service` / `@Component`** |
| **依赖获取** | `import { srv } from '...'` | `constructor(private srv: Srv)` | **`@Autowired` / 构造器注入** |
| **容器** | 无 | `Module` | **`ApplicationContext` (IoC 容器)** |

---

## 2. 什么是 IoC (控制反转)？

**IoC (Inversion of Control)** 不是一种工具，而是一种 **“控制权的让渡”**。

- **过去**：程序员是独裁者。什么时候需要 `UserMapper`？我自己写 `new UserMapper()`。
- **现在 (Spring)**：程序员是“资源申领者”。我告诉 Spring：“我这个 `UserService` 需要一个 `UserMapper`”，Spring 就会在启动时帮你 new 好，并自动塞（注入）进去。

### 为什么这样做更好？
1.  **极易测试**：你可以随心所欲地替换掉 Mock 对象。
2.  **解耦**：类与类之间不再直接“咬死”，而是通过容器“松耦合”关联。
3.  **单例管理**：大部分业务类（Bean）在 Spring 中默认都是单例，节省内存。

---

## 3. 核心注解：告诉 Spring 怎么做

在 Spring 中，我们通过 **注解 (Annotations)** 来下达指令：

- **`@Component`**：通用组件标记（相当于“我是一个 Bean”）。
- **`@Service`**：语义化的业务逻辑标记。
- **`@Repository`**：数据访问层标记（数据库相关）。
- **`@Controller / @RestController`**：处理 HTTP 请求的标记。
- **`@Autowired`**：告诉容器，“把那个 Bean 给我注入进来！”

---

## 4. 动手验证：Bean 的生命周期

我们已经在 [JavaLabsApplication.java](../src/main/java/com/javalabs/JavaLabsApplication.java) 中通过 `@SpringBootApplication` 开启了自动扫描。

### 验证步骤：
1.  启动主类。
2.  观察控制台：Spring 会递归扫描 `com.javalabs` 及其子包下的所有带注解的类。
3.  创建实例并存入 **BeanFactory**（IoC 容器的底层实现）。

---
> [!TIP]
> **指挥 AI 的提示词：** 
> *“将这个普通的 POJO 类转化为一个由 Spring 托管的单例 Service，并为其配置一个能够被自动注入的 Mapper 占位符。”*

**参考资料**：
- [Spring Reference Guide - IoC Container](https://docs.spring.io/spring-framework/reference/core/beans.html)
- [Baeldung: Guide to Spring Annotations](https://www.baeldung.com/spring-annotations-resource-inject-autowire)
