# Node.js / TypeScript → Java 概念映射速查

本文件供 AI Agent 在回答用户问题时查阅，帮助将 Java 概念映射到用户熟悉的 Node.js/TypeScript 概念，降低认知负担。

## 工具链与项目结构

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| `package.json` | `pom.xml` | 依赖声明 + 构建配置 |
| `npm install` | `mvn clean install` | 安装依赖并构建 |
| `node_modules/` | `~/.m2/repository/` | 本地依赖缓存目录 |
| `npm run build` | `mvn clean package` | 打包构建产物 |
| `npx create-app` | Spring Initializr | 脚手架生成工具 |
| `tsconfig.json` | `pom.xml` 的 `<properties>` | 编译器配置 |

## 框架与 IoC

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| NestJS `@Injectable()` | Spring `@Service` / `@Component` | Bean 注册到容器 |
| NestJS `@Controller()` | Spring `@RestController` | HTTP 请求处理器 |
| NestJS `@Module()` | Spring `@Configuration` | 模块/配置类 |
| NestJS `constructor(private svc: Svc)` | Spring `@Autowired` / 构造器注入 | 依赖注入 |
| NestJS `@Get()` / `@Post()` | Spring `@GetMapping` / `@PostMapping` | 路由装饰器 |
| NestJS `@Param()` / `@Body()` | Spring `@PathVariable` / `@RequestBody` | 参数提取 |
| Express middleware | Spring `Filter` / `@ControllerAdvice` | 全局拦截器 |
| NestJS Exception Filter | Spring `@ControllerAdvice` + `@ExceptionHandler` | 全局异常处理 |
| NestJS Pipe（参数校验） | `spring-boot-starter-validation` + `@Valid` | 入参校验 |

## 数据库与 ORM

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| TypeORM / Prisma | MyBatis-Plus | 首选 ORM |
| `@Entity()` | `@TableName` + Lombok `@Data` | 实体类注解 |
| Prisma `findMany` | `mapper.selectList(wrapper)` | 条件查询 |
| Prisma `create` | `mapper.insert(entity)` | 插入记录 |
| Prisma schema | DDL SQL + Entity 类 | 数据模型定义 |
| 数据库连接池（默认） | HikariCP | Spring Boot 默认连接池 |

## 异步与并发

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| `async/await` + Event Loop | Java 21 虚拟线程 | 无需 async 传染，底层自动非阻塞 |
| `Promise.all()` | `CompletableFuture.allOf()` | 并发等待多个任务 |
| `setTimeout` | `Thread.sleep()` / `ScheduledExecutorService` | 延迟执行 |
| Node.js 单线程事件循环 | Java 21 虚拟线程调度器 | 原理类似，都是 M:N 映射 |
| Worker Threads | `ExecutorService` / 平台线程 | 真正的并行计算 |

## 配置与环境

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| `process.env.KEY` | `@Value("${key}")` | 读取配置项 |
| `.env` 文件 | `application.yml` | 应用配置文件 |
| `.env.development` | `application-dev.yml` | 多环境配置 |
| `dotenv` | Spring Boot 自动加载 | 无需额外库 |

## 类型系统

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| TypeScript `interface` | Java `interface` | 接口定义 |
| TypeScript `type` / DTO interface | Java `record`（Java 16+）| 不可变数据载体 |
| TypeScript `class` | Java `class` | 普通类 |
| TypeScript 泛型 `<T>` | Java 泛型 `<T>` | 语法相似，但有类型擦除 |
| TypeScript `?.` 可选链 | Java `Optional<T>` | 空值安全处理 |
| TypeScript `enum` | Java `enum` | 枚举类型 |

## 测试

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| Jest | JUnit 5 + Mockito | 测试框架组合 |
| `describe` / `it` | `@Test` 方法 | 测试用例定义 |
| `jest.fn()` | `Mockito.mock()` | Mock 对象创建 |
| `jest.spyOn()` | `Mockito.spy()` | 监听方法调用 |
| `expect(x).toBe(y)` | `Assertions.assertEquals(y, x)` | 断言方式 |
| `beforeEach` | `@BeforeEach` | 测试前置钩子 |

## 分布式与微服务

| Node.js / TypeScript 概念 | Java 对应概念 | 备注 |
|--------------------------|--------------|------|
| Docker Compose 服务编排 | Spring Cloud Alibaba | 国内主流微服务体系 |
| Consul / Etcd 服务发现 | Nacos 注册中心 | 服务注册与配置中心 |
| `axios` HTTP 客户端调用 | OpenFeign 声明式 HTTP 客户端 | 跨服务调用 |
| API Gateway（Express/Fastify） | Spring Cloud Gateway | 统一流量入口 |
| Bull/BullMQ 消息队列 | RabbitMQ / RocketMQ | 异步消息处理 |
| `ioredis` Redis 客户端 | Spring Data Redis + Redisson | Redis 集成 |
