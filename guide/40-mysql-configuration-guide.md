# 40-从 H2 迁移到 MySQL：生产级数据库配置指南

在 Java 开发中，本地开发常用 H2 内存数据库以实现“零配置即开即用”。但在进阶阶段或准备上线时，切换到 **MySQL** 是必经之路。

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

项目已预配置了 MySQL 的官方驱动。请确保 `pom.xml` 中包含以下依赖：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 二、 核心配置 (application.yml)

你需要修改 `src/main/resources/application.yml` 中的 `datasource` 部分。

### 2.1 修改连接信息

将原有的 H2 配置注释，并替换为 MySQL 配置。

> [!IMPORTANT]
> 请确保你本地已经安装了 MySQL，并预先创建了对应的数据库（如 `java_labs`）。

```yaml
spring:
  datasource:
    # 1. 驱动类
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 2. 连接 URL (注意时区 serverTimezone 和字符集 useUnicode)
    url: jdbc:mysql://localhost:3306/java_labs?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    # 3. 账号密码
    username: root
    password: your_password
```

---

## 三、 使用 Docker 快速启动 (推荐)

对于习惯 Node.js 开发流程的用户，使用 `Docker` 启动数据库是最快的方式。

### 3.1 什么是 Docker Compose？

如果说 Docker 是一个“集装箱”，那么 **Docker Compose** 就是“调度员”。它允许你通过一个 YAML 配置文件来定义和管理多个 Docker 容器。

对于本项目，我们只需要一个 `docker-compose.yml` 文件，就能一键完成 MySQL 的拉取、端口映射、环境变量设置和数据持久化。

#### 1. 在 macOS 上安装 Docker Compose
如果你没有安装 Docker Desktop，可以通过 Homebrew 单独安装命令行工具：
```bash
brew install docker-compose
```

#### 2. 核心常用命令
学会以下 4 个命令，就能应对 90% 的开发场景：

- **一键启动**: `docker-compose up -d`
  - `-d` (detached) 表示在后台运行，不会占用你的终端窗口。
- **查看状态**: `docker-compose ps`
  - 确认容器是否正在运行 (State: Up) 以及端口映射是否正确。
- **停止并销毁**: `docker-compose down`
  - 停止运行中的容器并将其删除。**注意：** 挂载的数据卷（`./mysql_data`）会被保留，数据不会丢失。
- **实时日志**: `docker-compose logs -f`
  - 如果数据库启动失败，通过此命令查看具体的错误信息。

### 3.2 启动 MySQL 容器

在项目根目录下创建一个 `docker-compose.yml` 文件：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: java-labs-mysql
    ports:
      - '3306:3306'
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: java_labs
    volumes:
      - ./mysql_data:/var/lib/mysql
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
      mode: always # 每次启动都执行，注意 MySQL 中表已存在时可能会报错
```

> [!TIP]
> 在 MySQL 中，建议在 SQL 语句开头加上 `DROP TABLE IF EXISTS xxx;` 以支持重复运行。

### 4.2 手动初始化 (生产推荐)

使用管理工具（如 Navicat, DBeaver 或命令行）直接运行 `src/main/resources/schema.sql`。

---

## 五、 Node.js 开发者视角对比

| 特性         | Node.js (Prisma/Knex) | Java (Spring Boot)                       |
| :----------- | :-------------------- | :--------------------------------------- |
| **连接池**   | `generic-pool`        | `HikariCP` (默认，性能极佳)              |
| **迁移工具** | `prisma migrate`      | `Flyway` 或 `Liquibase` (本阶段暂未引入) |
| **驱动**     | `mysql2`              | `mysql-connector-j`                      |
| **配置位置** | `.env`                | `application.yml`                        |

---

**下一阶段建议**：

1. 更新 `application.yml` 的数据库密码。
2. 尝试运行 `mvn spring-boot:run` 验证连接。
3. 如果遇到连接失败，请检查 MySQL 的服务状态及防火墙设置。
