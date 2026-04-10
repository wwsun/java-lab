# 20 - MySQL 与 Redis 开发环境配置：优先走 Docker Compose

## 核心心智映射

如果用 Node.js 的经验类比：

- `docker compose up -d` 很像一条命令把数据库、缓存、网关都拉起来
- `application.yml` 很像后端项目的 `.env + config.ts`
- HikariCP 很像 Java 世界里默认帮你接好的连接池

第 2 周你应该先把“应用能连上稳定的 MySQL 和 Redis”这件事跑通，而不是先纠结数据库高级特性。

## 本项目的默认方案

根据当前项目约束，开发环境优先这样做：

1. 用户手动启动 Colima
2. 在项目根目录执行 `docker compose up -d`
3. 用 MySQL 8.4 和 Redis 7 作为本地中间件

这比本机直装数据库更适合学习和后续部署，因为：

- 环境更接近线上
- 重建成本低
- 多个项目之间不容易互相污染

## 第一步：准备 Docker 运行环境

### 1. 启动 Colima

本项目默认通过 Colima 提供 Docker Runtime。

```bash
colima start
```

如果你之前已经启动过，可以先检查：

```bash
docker ps
```

### 2. 启动中间件

在项目根目录执行：

```bash
docker compose up -d
```

相比旧版 `docker-compose`，这里统一使用 Docker Compose V2 命令写法。

## 第二步：确认 Maven 依赖

`pom.xml` 至少要有这两类依赖：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

你可以把它理解成：

- `mysql-connector-j` 对应 Node.js 的 `mysql2`
- `spring-boot-starter-data-redis` 对应 Java 版 Redis 客户端整合包

## 第三步：配置 `application.yml`

典型配置如下：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/java_labs?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

这里可以这样理解：

- `datasource` 是数据库连接配置
- `hikari` 是连接池参数
- `data.redis` 是 Redis 客户端配置

## 第四步：理解为什么这里是 MySQL + Redis

### MySQL 负责持久化

典型放这些数据：

- 用户
- 订单
- 会议室
- 预约记录

### Redis 负责高频访问和临时状态

典型放这些数据：

- 登录态 / 黑名单
- 热点缓存
- 验证码
- 幂等 Token

所以不要把 Redis 理解成“另一个数据库”，它更像系统性能层和状态层。

## 第五步：数据库初始化

开发环境常见两种方式。

### 1. Spring Boot 自动执行 SQL

```yaml
spring:
  sql:
    init:
      mode: always
```

适合练习阶段快速验证。

### 2. 通过 Docker Compose 挂载初始化脚本

更接近交付场景，也更适合团队共享环境。

如果你后面要做 meetingroom 项目，这种方式会更自然。

## 本机安装 MySQL 的备用方案

如果你暂时不想使用 Docker，也可以本机安装 MySQL 8.4。

```bash
brew install mysql@8.4
brew services start mysql@8.4
mysql_secure_installation
mysql -u root -p
```

但这条路只建议作为备用方案，不是主路径。

原因很简单：

- 和项目约束不一致
- 后续切到部署练习时还要再换一套思路

## Node.js 开发者视角对比

| 能力 | Node.js 常见方案 | Java / Spring Boot 对应方案 |
| --- | --- | --- |
| MySQL 驱动 | `mysql2` | `mysql-connector-j` |
| Redis 客户端 | `ioredis` / `node-redis` | Spring Data Redis |
| 配置文件 | `.env` + 配置模块 | `application.yml` |
| 连接池 | 驱动或 ORM 自带 | HikariCP |

## 常见问题

### 1. `Communications link failure`

通常说明：

- MySQL 容器没起来
- 端口没映射成功
- 配置写错了主机或端口

### 2. Redis 能连上，但序列化结果很难看

这是序列化器没配好，属于后续 Redis 集成专题的内容，继续看 [../03-web-security-and-infra/05-redis-integration-essentials.md](../03-web-security-and-infra/05-redis-integration-essentials.md)。

### 3. 本机 MySQL 和 Docker MySQL 冲突

如果都占用 `3306`，你会在启动容器时看到端口冲突。学习阶段建议只保留一种方案。

## 下一步建议

跑通顺序建议是：

1. 先确认 `docker compose up -d` 能正常启动 MySQL / Redis
2. 再确认 Spring Boot 能正常连接
3. 最后再进入 MyBatis-Plus CRUD、缓存和事务主题
