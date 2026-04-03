# 从 Node.js 到 Java：面向 Java Web 应用开发的实战学习指南

作为资深的 Node.js 与 TypeScript 开发者，在如今 Generative AI 和 Agent 爆发的时代，学习一门新语言（Java）的最终目的不再是强记语法规则，而是**建立正确的"心智模型"和"架构视野"，从而能够精准地向 AI Agent（如 Claude Code）下达架构级指令并进行全栈视角的 Code Review**。

基于这个核心哲学，你有着极大的转型优势：

1. **强类型的平滑跨越**：掌握 TS 的你，能毫无障碍地读懂 AI 生成的强类型面向对象业务代码；若你熟悉 NestJS，对于 Spring 生态（IoC、DI、AOP）的理念也会倍感亲切。
2. **数据库思维复用**：把繁琐的建表和基础 CRUD 交给 AI，自己专注于数据建模设计和业务逻辑验证。
3. **现代语法红利**：JDK 17 引入的 Records、密封类（Sealed Classes）、增强的 Switch 表达式等特性，让 Java 代码风格更接近你熟悉的 TypeScript。

> [!note] JDK 版本策略
> 本学习路线**以 JDK 17 为基准版本**。JDK 17 是当前最广泛使用的长期支持版（LTS），绝大多数企业项目和框架均以此为最低兼容版本。JDK 21 的特性（如虚拟线程）会在必要时作为 **Optional 拓展**标注说明，帮助你了解 Java 并发的演进方向。

按计划我们需要投入约 4 周的业余时间（工作日每晚 2-3 小时，周末每天 6-8 小时，总计约 100 小时）。为了实现向"指挥 AI 写 Java"的范式跃迁，我们需要采用**"架构概念映射 + 提示词 (Prompt) 驱动验证"**的高效策略。

以下是为你量身定制的四周学习路线。

## 第一周：Java 基础速成与生态映射

首周目标是建立 Java 的语法感知，熟悉开发工具链，并完成从 Node/TS 到 Java 的心智模型转换。

### 语法与生态映射（Day 1-2）

- [x] **开发工具配置**：下载安装 IntelliJ IDEA，放弃轻型编辑器。
- [x] 安装 jdk17
- [x] 安装 maven
- [x] **Maven 基础构建**：配置 Maven（对标 npm/pnpm），理解 `pom.xml` 核心结构（类比 `package.json`）。
- [x] **Maven 指令实操**：在终端运行并掌握 `mvn clean install` 等高频指令。
- [x] **现代语法实操验证**：手写代码验证 JDK 17 核心特性（Records、`var` 关键字、Switch 表达式、多行文本块、密封类）。
- [x] **🤖 AI 协同语法映射**：向 Claude 提问："把这段复杂的 Node.js `Array.reduce` 逻辑用 Java Stream API 重写"，观察并对比两者的语法惯用语（Idioms）差异。
- [x] **IDEA 调试技巧 (Optional)**：练习设置基础断点、条件断点，并学会在 Debug 面板中使用 Evaluate Expression，这是 Java 开发的杀手锏。

### 进阶语法与集合框架（Day 3-4）

- [x] **对象与泛型**：理解并写代码验证 Java 泛型的"类型擦除（Type Erasure）"机制。
- [x] **集合实操**：编写测试代码，分别使用 `ArrayList`, `HashSet`, `HashMap` 进行增删改查，对齐 JS 内置数据结构。
- [x] **Lambda 与 Stream**：在一组对象集合上，用 Stream API 实现类似 JS `map/filter/reduce` 的数据转换与过滤。
- [x] **Stream 综合练习**：完成一个包含 `collect(Collectors.groupingBy())`、`flatMap`、`Optional` 组合使用的综合练习，确保 Stream API 熟练度。
- [x] **异常处理体系**：理解 Checked vs Unchecked Exception 的设计哲学，编写自定义业务异常类。
- [x] **Java 时间 API**：掌握 `java.time.LocalDateTime`、`Instant`、`DateTimeFormatter` 等核心类的日常使用。

### 并发概念速览与周末综合练习（Day 5-周末）

> [!note] 并发学习策略
> 在典型 Java Web 应用中，线程管理主要交给 Spring Boot 框架层（内嵌 Tomcat 线程池）处理。这里只需理解核心概念，不必深挖线程池参数调优。

- [ ] **并发基础认知**：理解 Java 多线程模型与 Node.js 单线程事件循环的本质差异。
- [ ] **基础线程**：手写 `Thread` 和 `Runnable` 范例，体验多线程的启动。
- [ ] **复现并发冲突**：不用锁写一段多线程共享计数器代码，观察数据不一致现象。
- [ ] **加锁解决冲突**：分别使用 `synchronized` 关键字和 `ReentrantLock` 修复并发冲突。
- [ ] **线程池基础**：了解 `ExecutorService` 的基本用法，理解线程池相比手动创建线程的优势。
- [ ] **线程安全集合**：单独测试 `ConcurrentHashMap`，理解何为线程安全的集合操作。
- [ ] **线程池参数配置 (Optional)**：使用 `ThreadPoolExecutor` 创建自定义线程池，对比不同核心线程数、最大线程数及拒绝策略的表现。
- [ ] **JDK 21 虚拟线程体验 (Optional, 需要 JDK 21)**：用 `Thread.ofVirtual()` 和 `Executors.newVirtualThreadPerTaskExecutor()` 感受轻量级并发——底层自动非阻塞 I/O，代码却像同步般直白。类比 Node.js 事件循环的 M:N 映射思想。
- [ ] **多线程调试 (Optional)**：尝试在 IDEA 中将断点设置为 Thread 模式，观察并控制多线程的交替执行。

---

**📚 第一周推荐资料**

- [Maven 官方入门指南](https://maven.apache.org/guides/getting-started/)：快速建立对 POM、依赖坐标、生命周期的正规认知。
- [Baeldung: Java 基础系列](https://www.baeldung.com/category/java/tag/java-core/)：最实在的 Java 代码参考，几乎你能想到的所有知识点都有详尽示例，对标 MDN。
- 《On Java 8》/《On Java 中文版》：不需要通读，但遇到泛型、集合或多线程疑惑时，把它作为权威案头字典。

## 第二周：Spring Boot 核心与数据库接入

目标是掌握国内 Java 开发标准的 Web 框架体系，能够徒手拉起一个带有数据库 CRUD 的 RESTful API 接口，并建立正确的接口设计规范意识。

### Spring Boot 启蒙（Day 1-2）

- [ ] **脚手架生成**：使用 Spring Initializr 平台生成脚手架（选用 Java 17, Spring Boot 3.x, Maven, Spring Web）并导入 IDEA。
- [ ] **IoC 与 DI 验证**：写两组类验证有无 `@Autowired` 时的实例化过程及依赖关系的绑定。
- [ ] **RESTful API 全方法实操**：编写 `@RestController` 实现完整的 `GET`/`POST`/`PUT`/`DELETE` 接口，覆盖资源的增删改查。
- [ ] **RESTful 接口设计规范**：掌握 URL 命名规范（名词复数 `/api/users`、嵌套资源 `/api/users/{id}/tasks`）、HTTP Method 语义映射。
- [ ] **HTTP 状态码规范**：在接口中正确使用 `ResponseEntity` 返回语义化状态码——`200 OK`、`201 Created`、`400 Bad Request`、`401 Unauthorized`、`403 Forbidden`、`404 Not Found`、`500 Internal Server Error`。
- [ ] **参数捕获能力测试**：编写验证代码成功通过 `@PathVariable`, `@RequestParam`, `@RequestBody` 获取前端传入的数据。
- [ ] **中间件机制理解**：掌握 Spring Boot 的 `Filter`（类比 Express middleware）和 `HandlerInterceptor`（类比 NestJS Guard/Interceptor）的执行顺序与应用场景，编写一个请求日志记录 Filter。

### 数据库接入实战（Day 3-4）

> [!info] ORM 选型建议
> 国内环境中不需要死磕 JPA/Hibernate，直接学习 **MyBatis** 和 **MyBatis-Plus**。它类似于 Prisma 或 TypeORM，且在处理复杂 SQL 时更具灵活性。同时，MyBatis-Plus 通过内置方法帮你避免裸写 SQL，但遇到复杂场景时仍需理解 SQL 本身。

- [ ] **关系型数据库基础**：理解表、字段、主键（Primary Key）、外键（Foreign Key）的概念，编写一组带有关联关系的 DDL（如 `users` 表和 `tasks` 表的一对多关系）。
- [ ] **索引认知**：理解索引的本质（B+树）和使用时机——查询条件字段、关联字段（外键）、排序字段应加索引；低基数字段（如性别）不适合单独加索引。
- [ ] **依赖引入与配置**：引入 MyBatis-Plus 及 MySQL 驱动依赖。
- [ ] **连接池调优**：在配置文件中设置并熟悉 HikariCP 核心配置。
- [ ] **ORM 层搭建**：编写基于数据表的 Entity 实体类（Lombok 注解）及对应的 Mapper 接口。
- [ ] **CRUD 测试**：利用 MyBatis-Plus 内置的基础方法（`selectById`, `insert`, `updateById`, `deleteById`）体验增删改查。
- [ ] **N+1 查询问题**：理解 ORM 的 N+1 查询陷阱（类比 Prisma 的 `include` / TypeORM 的 `eager loading`）。在 MyBatis-Plus 中通过 `@Select` 联表查询或自定义 XML Mapper 中的 `<resultMap>` + `<association>` 解决。
- [ ] **🤖 AI 骨架生成提效**：跳过手打模板，尝试对 AI 发送指令："基于这份 DDL SQL，生成自带 Lombok 注解的 MyBatis-Plus Entity 实体和与其配套的一组标准 CRUD Controller / Service"，并 Review 其生成的依赖路径是否符合你的层级结构。

### 实战整合与规范（Day 5-周末）

- [ ] **全局响应封装**：定义并使用规范化的统一 API 返回实体（`Result<T>`）。
- [ ] **全局异常处理**：编写 `@ControllerAdvice` 截获常见业务异常并格式化输出。
- [ ] **校验框架集成**：引入 `spring-boot-starter-validation` 处理接口入参校验。
- [ ] **单元测试入门**：引入 `spring-boot-starter-test`，写一个基于 JUnit 5 和 Mockito 的基础测试用例，掌握 AAA 模式（Arrange-Act-Assert）。
- [ ] **工程打包与运行**：在项目根目录运行 `mvn clean package` 体验 Fat JAR 的打包机制，并用 `java -jar` 命令将服务独立运行起来。
- [ ] **周末闭环实战**：写一个带有 JWT 鉴权的"书籍管理系统"，打通 Controller -> Service -> Mapper -> DB 的完整闭环。

---

**📚 第二周推荐资料**

- [Spring Initializr](https://start.spring.io/)：Java 届最核心的脚手架工具平台。
- [Baeldung: Spring Boot 专栏](https://www.baeldung.com/spring-boot)：比官方文档更平易近人的 Spring 生态最佳实践大全，手把手教你集成一切。
- [MyBatis-Plus 官方指南](https://baomidou.com/)：国内企业极其青睐的 ORM 增强框架，全中文且示例极多，能极大提高开发速度。

## 第三周：Web 应用进阶——安全、缓存与工程规范

目标是掌握典型 Java Web 应用的核心横切关注点：安全认证、缓存加速、API 文档、日志监控，这些是从"能跑"到"能上线"的关键一步。

### 安全认证与 Web 安全防范（Day 1-2）

> [!note] 认证方案
> 采用 **Spring Security + JWT** 方案。Spring Security 是 Spring 生态的事实标准安全框架，虽然学习曲线稍陡，但掌握后能覆盖绝大多数 Web 应用的认证授权需求。类比 NestJS 中的 Guard + Passport 组合。

- [ ] **Spring Security 基础配置**：理解 Security Filter Chain 的工作原理，配置基础安全过滤器链。
- [ ] **JWT 完整实现**：实现令牌的生成、验证、续期全流程。
- [ ] **基于注解的权限控制**：使用 `@PreAuthorize` 实现接口级的角色权限控制。
- [ ] **CORS 跨域配置**：正确配置前后端分离架构下的跨域请求（类比 Express 中的 `cors` 中间件）。
- [ ] **常见 Web 安全风险防范**：了解并配置对以下攻击的防御措施——SQL 注入（MyBatis-Plus 参数化查询已默认防御）、XSS（输入过滤 + 输出转义）、CSRF（Spring Security 默认启用 CSRF Token）、接口限流防刷（结合 Redis 实现简单的滑动窗口限流）。

### Redis 集成与 API 文档（Day 3-4）

- [ ] **Redis 概念与应用场景**：理解 Redis 作为内存数据库的核心定位，梳理典型应用场景——缓存加速、Session 存储、接口限流、排行榜、分布式锁。
- [ ] **Redis 基础数据结构**：掌握五种核心数据结构的特点与适用场景——`String`（缓存/计数器）、`Hash`（对象字段存储）、`List`（消息队列/最新列表）、`Set`（去重/交集运算）、`Sorted Set`（排行榜/延迟队列）。
- [ ] **Spring Boot 接入 Redis**：引入 `spring-boot-starter-data-redis` 并配置连接参数，分别通过 `RedisTemplate` 和 `StringRedisTemplate` 操作不同数据结构。
- [ ] **缓存模式实践**：实现 Cache-Aside 模式（先查缓存，未命中再查 DB 并回填缓存）。
- [ ] **Spring Cache 注解**：使用 `@Cacheable`, `@CacheEvict`, `@CachePut` 简化缓存逻辑。
- [ ] **缓存常见问题认知**：理解缓存穿透、击穿、雪崩的概念及基础规避思路（空值缓存、随机过期时间）。
- [ ] **API 文档集成**：集成 Knife4j 或 SpringDoc（Swagger UI）生成在线 API 文档，让前后端协作更高效。

### 日志、监控与工程规范（Day 5-周末）

- [ ] **日志体系配置**：配置 SLF4J + Logback，掌握日志分级（DEBUG/INFO/WARN/ERROR）和文件滚动策略。
- [ ] **应用监控端点**：引入 Spring Boot Actuator，配置健康检查和应用信息端点。
- [ ] **接口幂等性方案**：理解并实现 Token 令牌机制，确保关键接口的幂等性。
- [ ] **🤖 AI 协同安全审计**：让 Agent 对你的认证鉴权代码做一次完整的安全审查，识别 JWT 密钥管理、Token 过期策略等常见安全漏洞。

---

**📚 第三周推荐资料**

- [Baeldung: Spring Security 系列](https://www.baeldung.com/security-spring)：最全面的 Spring Security 教程合集，覆盖 JWT、OAuth2、RBAC 等主流方案。
- [Spring Data Redis 官方文档](https://docs.spring.io/spring-data/redis/reference/)：Redis 集成的权威参考指南。
- [Knife4j 官方文档](https://doc.xiaominfo.com/)：国内最流行的 Swagger 增强 UI，中文文档齐全。

## 第四周：综合实战——任务管理系统与部署

最终目标是完成一个完整的前后端分离项目，体验从数据建模到容器化部署的全流程，证明你已具备独立开发 Java Web 应用的能力。

### 任务管理系统开发（Day 1-2）

> [!info] 实战项目
> 选用"任务管理系统"作为综合实战项目，涵盖用户认证、CRUD、状态流转、权限控制等典型 Web 应用场景。

- [ ] **需求分析与数据建模**：设计用户表、任务表、标签表等核心数据模型，编写 DDL。
- [ ] **🤖 AI 生成项目骨架**：让 Agent 基于数据模型生成完整的 Entity + Mapper + Service + Controller 层骨架代码。
- [ ] **核心业务实现**：完成任务的 CRUD、分页查询、条件搜索、状态流转（待做 → 进行中 → 已完成）。
- [ ] **认证鉴权集成**：集成第三周的 JWT + Spring Security 实现用户注册、登录、权限拦截。

### Docker 容器化、Nginx 与部署（Day 3-4）

- [ ] **编写 Dockerfile**：将 Spring Boot 应用打包为 Docker 镜像，理解多阶段构建（Multi-stage Build）的优化思路。
- [ ] **Docker Compose 开发环境**：编排应用 + MySQL + Redis 三件套的本地开发环境，一键启动。
- [ ] **多环境配置管理**：配置 `application-dev.yml` / `application-prod.yml`，理解 Spring Profiles 机制。
- [ ] **Nginx 反向代理配置**：在 Docker Compose 中加入 Nginx 容器，配置反向代理将请求转发到 Spring Boot 应用。理解 Nginx 在前后端分离架构中的角色——静态资源托管 + API 反向代理 + 负载均衡。
- [ ] **SSH 远程部署基础**：练习通过 SSH 连接远程服务器，完成 Docker 环境安装、镜像拉取和容器启动的基本操作流程。
- [ ] **🤖 AI 生成 CI/CD 配置**：让 Agent 分别生成 GitHub Actions 和 GitLab CI（`.gitlab-ci.yml`）的工作流配置，实现自动化构建、镜像推送和远程部署。

### 性能基础、通知模块与收尾（Day 5-周末）

- [ ] **接口性能基准测试**：使用 Apache Benchmark (`ab`) 或 `wrk` 对核心接口做压力测试，建立性能基线。
- [ ] **慢 SQL 排查与索引优化**：了解 `EXPLAIN` 分析执行计划，为高频查询添加合适的索引。
- [ ] **通知模块 (Optional, 消息队列体验)**：引入 RabbitMQ，实现"任务分配时发送通知"的异步解耦流程，体验消息队列在 Web 应用中的典型用法。
- [ ] **项目收尾**：完善 API 文档、补充关键路径的单元测试、整理代码结构。
- [ ] **周末回顾总结**：走读全部代码，整理学习笔记，梳理从 Node.js 到 Java 的关键认知跃迁。

---

**📚 第四周推荐资料**

- [Docker 官方入门指南](https://docs.docker.com/get-started/)：从零理解容器化概念和实践。
- [Spring Boot Docker 最佳实践](https://www.baeldung.com/dockerizing-spring-boot-application)：如何高效地将 Spring Boot 应用容器化。
- [Nginx 入门指南](https://nginx.org/en/docs/beginners_guide.html)：理解 Nginx 核心配置（server、location、proxy_pass）的官方文档。
- [GitHub Actions 官方文档](https://docs.github.com/en/actions/quickstart)：CI/CD 工作流配置的权威参考。
- [RabbitMQ Tutorials (Spring AMQP 版)](https://www.rabbitmq.com/tutorials)：手把手带你用 Spring Boot 构建各种常见消息通信模型的官方优质教程。

---

## 进阶路线：分布式与微服务架构（后续学习）

> [!note] 进阶学习
> 以下内容不在 4 周基础路线之内，但作为 Java 后端工程师的成长方向，当你需要应对更复杂的业务场景时，可以按需展开学习。

### 分布式中间件

- [ ] **Redis 分布式锁**：集成 Redisson，理解看门狗（WatchDog）机制如何解决锁超时问题。
- [ ] **缓存高可用**：深入理解缓存穿透（布隆过滤器）、击穿（互斥锁）、雪崩（随机过期）的工程化解决方案。
- [ ] **消息队列进阶**：深入 RabbitMQ 或 RocketMQ，掌握死信队列、延迟消息、消费幂等性等高级特性。
- [ ] **分布式事务**：了解 Seata 等分布式事务框架的 AT 模式和 TCC 模式。

### 微服务架构体系

- [ ] **服务注册与配置中心**：本地部署 Nacos Server，实现服务注册、发现和动态配置管理。
- [ ] **服务间通信**：使用 OpenFeign 实现声明式跨服务调用，告别硬编码 HTTP。
- [ ] **API 网关**：搭建 Spring Cloud Gateway 作为统一流量入口，配置路由转发和全局鉴权过滤器。
- [ ] **服务拆分实战**：将单体应用拆分为多个微服务模块，体验从单体到微服务的架构演进。
- [ ] **JDK 21 虚拟线程生产应用 (需要 JDK 21)**：在微服务中配置 `spring.threads.virtual.enabled: true`，用虚拟线程承载 Servlet 请求，对比传统线程池模式的吞吐量差异。

**📚 进阶推荐资料**

- [Redisson 官方 Wiki (中文完整版)](https://github.com/redisson/redisson/wiki/目录)：从这里学不仅是学锁工具类的用法，更是理解分布式锁机制的最佳教科书。
- [Spring Cloud Alibaba 官方资源](https://spring.io/projects/spring-cloud-alibaba)：国内大厂事实上的微服务基础标准。
- [Spring Cloud Gateway 官方参考](https://docs.spring.io/spring-cloud-gateway/reference/index.html)：着重翻阅其 Route Predicate 和 Filter 的配置清单。

---

## 给前端老鸟的特别避坑指南

最后，在你的 4 周逆袭之旅中，这里有几个重要的提醒：

1. **绝对不要用轻量级编辑器盲敲 Java**：前端习惯了 VSCode 的轻量灵活，但在 Java 极高的工程复杂度面前，IntelliJ IDEA 的自动导包、代码重构、Maven 依赖面板才是保障开发效率的终极武器。
2. **警惕 NPE（NullPointerException）**：TypeScript 拥有严格的空值检查 (`?.`、`??`)，而 Java 虽然引入了 `Optional`，但在大量历史项目中，极其容易遭遇空指针异常。保持严谨的数据校验逻辑。
3. **拥抱注解（Annotations）**：Java 偏爱注解式编程。遇到不认识的 `@Xxx`，大胆按下 Ctrl/Cmd 进入源码查看其实现原理和 `Javadoc` 的说明。
4. **强硬防范 Agent 的版本"幻觉"**：当前 AI 的训练语料里有海量沉淀 10 年之久的 Java 8 古董代码（如 `java.util.Date`），经常会生成倒退的历史语法。**因此在让 Agent 生成代码时，务必强调："我是基于 Java 17 和 Spring Boot 3+ 环境"**。配合查收 IDEA 实时的语法警告，防止引狼入室！
5. **前期不要死磕底层源码**：Spring 源码深不可测。在这短短 4 周内，你需要的是**把组件积木正确拼装起来**。一旦能熟练运用主流组件且建立起架构模型的大盘观念，依靠 Claude Code 去执行并补充 CRUD，你就已经完成核心的心智跨越了。

带着你在 Node 时代的积累，跳出单纯的语言桎梏，这段转型之旅将使你的服务端技术视野更加开阔。
