# 17 - Spring Profiles 多环境配置指南：从 Node.js `.env` 到 `application-dev.yml`

这节要解决的不是“YAML 怎么写”，而是一个更工程化的问题：

> 同一套 Spring Boot 代码，为什么能在开发、测试、生产环境下加载不同配置，而且还能保持配置结构稳定？

如果你有 Node.js/TypeScript 背景，可以先这样映射：

| Node.js / TS 世界 | Spring Boot 世界 | 你应该怎么理解 |
| --- | --- | --- |
| `.env.development` / `.env.production` | `application-dev.yml` / `application-prod.yml` | 不同环境的差异化配置文件 |
| `NODE_ENV=production` | `SPRING_PROFILES_ACTIVE=prod` | 当前激活哪个环境 |
| `config/default.ts` | `application.yml` | 全环境共享的基础配置 |
| `dotenv` 加载环境变量 | Spring Boot 配置绑定 | 启动时自动装配配置 |
| `process.env.DB_HOST` | `${SPRING_DATASOURCE_URL}` | 从环境变量读取敏感配置 |

## 一、核心心智映射

Spring Profiles 的核心思想可以概括成一句话：

> 把“所有环境都相同”的配置放进 `application.yml`，把“只在某个环境不同”的配置放进 `application-{profile}.yml`。

对 Java 项目来说，这么做的价值非常高，因为你通常会同时面对这些差异：

- 开发环境连本机 MySQL / Redis
- 生产环境连服务器上的数据库和缓存
- 开发环境打开 SQL 日志
- 生产环境降低日志级别
- 开发环境可以启用 Swagger / Knife4j
- 生产环境必须关闭调试和文档入口

## 二、先看你手头脚手架的真实结构

参考 [`../java-web-starter/backend/src/main/resources/application.yml`](../java-web-starter/backend/src/main/resources/application.yml)、
[`../java-web-starter/backend/src/main/resources/application-dev.yml`](../java-web-starter/backend/src/main/resources/application-dev.yml)、
[`../java-web-starter/backend/src/main/resources/application-prod.yml`](../java-web-starter/backend/src/main/resources/application-prod.yml)：

```text
src/main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml
```

这就是 Spring Boot 里最经典、也最推荐的多环境配置结构。

## 三、三层配置各自负责什么

### 1. `application.yml`

它负责放“所有环境都通用”的配置。

例如你脚手架里的这些内容就适合放在这里：

- 服务端口
- 应用名
- 默认激活 profile
- Jackson 通用格式
- MyBatis-Plus 通用规则
- SpringDoc / Knife4j 的基础路径

对应示例见 [`../java-web-starter/backend/src/main/resources/application.yml`](../java-web-starter/backend/src/main/resources/application.yml)：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: java-web-starter
  profiles:
    active: dev
```

这里的关键点是：

- `application.yml` 不是“开发环境配置文件”
- 它是“默认基础配置文件”

### 2. `application-dev.yml`

它负责放开发环境的差异化配置。

比如你当前脚手架里：

- MySQL 连 `localhost:3306`
- Redis 连 `localhost:6379`
- 日志级别更详细
- MyBatis SQL 日志打开
- JWT 使用开发环境密钥

对应示例见 [`../java-web-starter/backend/src/main/resources/application-dev.yml`](../java-web-starter/backend/src/main/resources/application-dev.yml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/starter_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: root123456

  data:
    redis:
      host: localhost
      port: 6379
```

这和你在 Node.js 里写 `.env.development` 的心智非常接近。

### 3. `application-prod.yml`

它负责放生产环境的差异化配置，但通常不直接写死敏感信息。

你脚手架里的写法是：

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}
      port: ${SPRING_DATA_REDIS_PORT:6379}
```

这里有两个关键点：

1. 生产环境尽量通过环境变量注入敏感配置
2. `${VAR:default}` 可以给变量设置默认值

类比 Node.js：

```ts
const redisPort = process.env.SPRING_DATA_REDIS_PORT ?? "6379";
```

## 四、Spring Boot 是怎么决定加载哪个文件的

核心入口就是这个配置：

```yaml
spring:
  profiles:
    active: dev
```

它表示默认激活 `dev` 环境。

当 `dev` 被激活时，Spring Boot 会：

1. 先加载 `application.yml`
2. 再加载 `application-dev.yml`
3. 用后者覆盖前者中的同名配置

所以你可以把它理解成：

> `application.yml` 是基础层，`application-dev.yml` 是覆盖层。

## 五、怎么切换环境

### 方式 1：在 `application.yml` 里写默认值

例如当前脚手架默认就是：

```yaml
spring:
  profiles:
    active: dev
```

这适合本地开发阶段。

### 方式 2：启动时通过环境变量覆盖

生产环境更常见的写法是：

```bash
SPRING_PROFILES_ACTIVE=prod java -jar app.jar
```

或者在 Docker Compose 里：

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
```

### 方式 3：通过 JVM 参数指定

```bash
java -jar app.jar --spring.profiles.active=prod
```

通常优先级上，命令行参数和环境变量会覆盖文件里的默认值。

## 六、一个符合你当前项目的最小示例

### `application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: meetingroom
  profiles:
    active: dev
```

### `application-dev.yml`

```yaml
spring:
  datasource:
    # 开发环境连接本机通过 Docker 暴露出来的 MySQL
    url: jdbc:mysql://localhost:3306/meetingroom?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: root

  data:
    redis:
      # 开发环境连接本机通过 Docker 暴露出来的 Redis
      host: localhost
      port: 6379
```

### `application-prod.yml`

```yaml
spring:
  datasource:
    # 生产环境从部署平台或 Docker 注入变量
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}
      port: ${SPRING_DATA_REDIS_PORT:6379}
```

## 七、为什么开发环境写 `localhost`，而容器内常常要写服务名

这个点和 Docker Compose 是连在一起的。

### 开发环境

如果你的 Java 应用是在 IDEA 中直接运行，那么它连 MySQL / Redis 时通常写：

- `localhost:3306`
- `localhost:6379`

因为此时应用跑在宿主机上，而中间件通过 `ports` 映射到了宿主机。

### 容器内环境

如果将来你的 `backend` 也放进 Docker Compose，那么后端就不该再连：

- `localhost:3306`

而应该连：

- `mysql:3306`

因为在 Compose 网络里，`mysql` 是服务名。

这也是为什么“多环境配置”必须和“部署方式”一起设计。

## 八、关键语法与常见写法

### 1. 占位符语法

```yaml
url: ${SPRING_DATASOURCE_URL}
```

表示从环境变量读取值。

### 2. 带默认值的占位符

```yaml
port: ${SPRING_DATA_REDIS_PORT:6379}
```

表示如果环境变量不存在，就用 `6379`。

### 3. 同名字段覆盖

基础配置：

```yaml
logging:
  level:
    root: INFO
```

生产环境覆盖：

```yaml
logging:
  level:
    root: WARN
```

Spring Boot 最终会用 `application-prod.yml` 的值。

## 九、核心类 / 配置入口 / 运行方式

这节不依赖某个特定 Java 类，但你需要掌握 3 个关键入口：

### 1. `spring.profiles.active`

决定当前激活哪个 profile。

### 2. `application-{profile}.yml`

定义 profile 专属配置。

### 3. 环境变量注入

比如：

- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATA_REDIS_HOST`
- `JWT_SECRET`

这些变量在 Docker、CI/CD、服务器部署里都会频繁出现。

## 十、最佳实践

- `application.yml` 只放全环境共享配置，不要把开发环境专属地址塞进去。
- `application-dev.yml` 可以连本机 `localhost`，方便 IDEA 调试。
- `application-prod.yml` 优先通过环境变量读取敏感配置，不要写死密码。
- 生产环境关闭 Swagger / Knife4j、降低日志级别、关闭调试型配置。
- 数据源、Redis、JWT 这类敏感或环境相关配置都要显式分层。

## 十一、常见反模式

- 把所有环境配置都塞进一个 `application.yml`，后期维护会非常混乱。
- 在 `application-prod.yml` 里直接提交生产数据库密码。
- 本地用 `localhost`，容器内也照抄 `localhost`，结果容器互联失败。
- 把 `dev` 写成默认 profile 后就忘了在生产环境覆盖，导致线上误用开发配置。
- 在多个文件里重复定义太多相同配置，导致覆盖关系不清晰。

## 十二、配套代码示例解读

建议按下面顺序阅读现有文件：

1. [`../java-web-starter/backend/src/main/resources/application.yml`](../java-web-starter/backend/src/main/resources/application.yml)
   看基础配置层。
2. [`../java-web-starter/backend/src/main/resources/application-dev.yml`](../java-web-starter/backend/src/main/resources/application-dev.yml)
   看开发环境如何连接 `localhost`。
3. [`../java-web-starter/backend/src/main/resources/application-prod.yml`](../java-web-starter/backend/src/main/resources/application-prod.yml)
   看生产环境如何通过环境变量注入。
4. [`41-docker-compose-essentials.md`](./41-docker-compose-essentials.md)
   把多环境配置和 Docker 网络、端口映射串起来。

## 十三、AI 辅助开发实战建议

你可以直接这样给 Agent 下指令：

> 请基于当前 Spring Boot 3 项目生成一套 `application.yml`、`application-dev.yml`、`application-prod.yml`。
> 要求：
> 1. 开发环境连接本机 Docker 暴露的 MySQL / Redis；
> 2. 生产环境全部通过环境变量注入；
> 3. 开发环境开启 SQL 日志，生产环境关闭；
> 4. 关键字段补充中文注释。

或者：

> 请审查我的 Spring Profiles 配置，重点检查：
> 1. 是否把敏感信息写进了仓库；
> 2. `localhost` 和服务名使用是否符合部署方式；
> 3. 是否存在 profile 覆盖关系混乱的问题。

## 十四、这一节学完后的验收标准

如果你能独立回答下面 4 个问题，就算这节已经入门：

1. `application.yml` 和 `application-dev.yml` 的职责分别是什么？
2. Spring Boot 激活 `dev` profile 时，配置覆盖顺序是什么？
3. 为什么开发环境常写 `localhost`，而容器内常写 `mysql`？
4. 为什么生产环境应该优先从环境变量读取数据库和 JWT 配置？

## 十五、扩展阅读

1. [`41-docker-compose-essentials.md`](./41-docker-compose-essentials.md)
2. [`53-docker-containerization-basics.md`](./53-docker-containerization-basics.md)
3. [`36-packaging-and-running-guide.md`](./36-packaging-and-running-guide.md)
