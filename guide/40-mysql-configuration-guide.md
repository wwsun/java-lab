# 40-从 H2 迁移到 MySQL 与 Redis：生产级基础设施配置指南

在 Java 开发中，本地开发常用 H2 内存数据库以实现“零配置即开即用”。但在进阶阶段或准备上线时，切换到 **MySQL** (持久化存储) 和 **Redis** (高速缓存) 是必经之路。

---

## 〇、 macOS (Intel) 本地安装指南 (Homebrew)

对于希望在本地系统直接运行 MySQL 而不使用 Docker 的用户，请按以下步骤操作：

### 1. 安装 MySQL

我们推荐安装 8.0 版本以获得最佳的稳定性。

```bash
brew install mysql@8.4
```

### 2. 配置环境变量

安装完成后，如果 mysql 命令没找到，需要将 MySQL 的二进制路径添加到你的 Shell 配置文件中（通常是 `~/.zshrc`）：

```bash
echo 'export PATH="/usr/local/opt/mysql@8.4/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### 3. 管理 MySQL 服务

使用 Homebrew Services 管理服务非常方便：

- **启动并设置开机自启**: `brew services start mysql@8.4`
- **停止服务**: `brew services stop mysql@8.4`

### 4. 安全加固与密码设置

安装后，默认 `root` 用户没有密码。运行以下命令进行初始化：

```bash
mysql_secure_installation
```

如果执行报错的话 `Error: Can't connect to local MySQL server through socket '/tmp/mysql.sock' (2)`

解决方案如下：

```bash
# 停止服务
brew services stop mysql@8.4

# 删除旧数据（请确保没有重要数据！）
rm -rf $HOMEBREW_PREFIX/var/mysql

# 重新安装并初始化
brew postinstall mysql@8.4
brew services start mysql@8.4
```

**关键步骤提示：**

- **VALIDATE PASSWORD COMPONENT**: 建议选 `N`（开发环境可跳过复杂的密码强度校验）。
- **New password**: 设置你的数据库密码（请务必记住）。
- **Remove anonymous users?**: 选 `Y`。
- **Disallow root login remotely?**: 选 `Y`（仅限本地连接）。
- **Remove test database?**: 选 `Y`。
- **Reload privilege tables now?**: 选 `Y`。

### 5. 验证连接

```bash
mysql -u root -p
```

成功进入 `mysql>` 提示符即表示安装成功。

---

## 一、 依赖检查 (pom.xml)

项目已预配置了 MySQL 和 Redis 的驱动。请确保 `pom.xml` 中包含以下依赖：

```xml
<!-- MySQL 驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Redis 启动器 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

---

## 二、 核心配置 (application.yml)

你需要修改 `src/main/resources/application.yml` 中的配置。

### 2.1 修改数据源信息

将原有的 H2 配置注释，并替换为 MySQL 配置。

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/java_labs?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果没设密码则留空
```

---

## 三、 使用 Docker 快速启动 (推荐)

对于习惯 Node.js 开发流程的用户，使用 `Docker` 启动中间件是最快的方式。

### 3.1 什么是 Docker Compose？

如果说 Docker 是一个“集装箱”，那么 **Docker Compose** 就是“调度员”。它允许你通过一个 YAML 配置文件来定义和管理多个 Docker 容器。

### 3.2 启动 MySQL & Redis 容器

在项目根目录下已经提供了一个 `docker-compose.yml` 文件：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: java-labs-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: java_labs
    ports:
      - '3306:3306'
    volumes:
      - ./mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    container_name: java-labs-redis
    restart: always
    ports:
      - '6379:6379'
    volumes:
      - ./redis_data:/data
```

运行命令启动：

```bash
docker-compose up -d
```

---

## 四、 数据库初始化 (SQL)

### 4.1 自动初始化 (适合开发环境)

Spring Boot 依然支持通过 `schema.sql` 和 `data.sql` 自动初始化：

```yaml
spring:
  sql:
    init:
      mode: always # 每次启动都执行
```

> [!TIP]
> 在 MySQL 中，建议在 SQL 语句开头加上 `DROP TABLE IF EXISTS xxx;` 以支持重复运行。

---

## 五、 Node.js 开发者视角对比

| 特性 | Node.js (Prisma/Knex) | Java (Spring Boot) |
| :--- | :--- | :--- |
| **连接池** | `generic-pool` | `HikariCP` (默认，性能极佳) |
| **缓存客户端** | `ioredis` / `node-redis` | `Jedis` / `Lettuce` (通过 Spring Data Redis 封装) |
| **驱动** | `mysql2` | `mysql-connector-j` |
| **配置位置** | `.env` | `application.yml` |

---

## 六、 Redis 基础：高效缓存的奥秘

作为内存数据库，Redis 的核心价值在于 **速度**。在进入 Java 代码集成前，你需要掌握其核心数据结构及其应用场景。

### 6.1 五大基本数据结构

| 数据结构 | 描述 | 类比 Node.js 对象 | 典型场景 |
| :--- | :--- | :--- | :--- |
| **String (字符串)** | 最基本的 KV 存储，支持过期时间。 | `string / number` | 缓存对象 (JSON)、计数器、分布式锁。 |
| **Hash (哈希)** | 字段和值的映射表。 | `Object / Map` | 存储用户信息、对象属性变更。 |
| **List (列表)** | 有序字符串列表（双向链表）。 | `Array` | 消息队列、最新动态列表。 |
| **Set (集合)** | 无序且唯一的字符串集合。 | `Set` | 点赞列表、共同好友（交集计算）。 |
| **ZSet (有序集合)** | 每个成员关联一个分数 (score) 的集合。 | `N/A` | 排行榜 (分数排名)、延迟队列。 |

### 6.2 典型应用场景

1.  **热点数据缓存 (Cache Aside)**：
    *   将查询结果存入 Redis，下次查询先看缓存，避免压力全部涌向 MySQL。
    *   *Node.js 对标*：配合 `Map` 或本地内存缓存，但在分布式环境下必须用 Redis。
2.  **分布式 Session/Token**：
    *   存储用户登录态（如 JWT 黑名单），实现多实例共享 Session。
3.  **计数器与限流**：
    *   利用 `INCR` 命令实现高并发下的文章阅读量统计或接口防刷。
4.  **分布式锁 (Redlock)**：
    *   在多台服务器同时执行定时任务时，确保只有一个实例在运行。
5.  **排行榜**：
    *   利用 ZSet 的自动排序特性，实时展示积分榜。

---

**下一阶段建议**：

1. 确保 `docker-compose up -d` 已拉起 MySQL 和 Redis。
2. 更新 `application.yml` 的连接配置。
3. 查阅 `@guide/40-redis-integration-essentials.md` 学习如何在 Java 中操作这些数据结构。
