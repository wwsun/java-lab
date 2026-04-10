# Docker Compose 核心指南：本地环境的“编排器”

这份文档聚焦一个目标：

> 让你搞清楚 `docker compose` 到底在帮 Java Web 项目做什么，以及为什么它特别适合本地开发阶段管理 MySQL、Redis、Nginx 这类基础设施。

如果你有 Node.js/TypeScript 背景，可以先这样类比：

| Node.js / TS 世界 | Docker Compose 世界 | 你应该怎么理解 |
| --- | --- | --- |
| `npm run dev` 只启动一个服务 | `docker run` | 适合单服务验证 |
| `pm2 ecosystem.config.js` | `docker compose.yml` | 多服务运行清单 |
| 一套 shell 脚本同时拉起 DB/Redis/app | `docker compose up -d` | 一条命令编排整套环境 |
| `localhost:3306` 连接本机 MySQL | `localhost:3306` 连接宿主机映射端口 | 只适用于宿主机访问 |
| 进程间用服务发现互联 | Compose 服务名互联 | 容器间直接用 `mysql:3306` |

## 一、为什么需要 Docker Compose

在 Java Web 项目里，你通常不会只有一个进程：

- Spring Boot 应用
- MySQL
- Redis
- Nginx
- 前端静态资源服务

如果每个服务都手动用一条 `docker run` 启动，会很快遇到这些问题：

- 启动顺序难记
- 端口映射容易冲突
- 网络互联配置分散
- 环境变量和数据卷零散难维护

Docker Compose 的价值，就是把“整套系统怎么启动”写成一个声明式清单。

## 二、核心心智映射

请先记住这 4 个概念：

### 1. `docker-compose.yml`

它是**系统级启动清单**。

不是单个服务的构建脚本，而是“这一套系统里有哪些服务、它们怎么连、端口怎么映射、数据怎么持久化”的统一定义。

### 2. `docker compose up -d`

它不是“只启动容器”。

它更准确的意思是：

1. 读取 Compose 配置
2. 准备镜像
3. 创建网络
4. 创建并启动容器
5. 后台运行

### 3. 服务名就是容器间主机名

在 Compose 网络里：

- `mysql` 就是 MySQL 服务主机名
- `redis` 就是 Redis 服务主机名

所以容器之间互联时，用的是：

- `mysql:3306`
- `redis:6379`

而不是 `localhost`

### 4. Compose 特别适合“基础设施进容器，业务代码留在 IDE”

本地开发阶段的黄金模式通常是：

- MySQL / Redis / Nginx 放进 Docker
- Java 业务代码继续在 IDEA 中运行和断点调试

这正是你当前项目采用的模式。

## 三、当前项目里的真实配置

参考 [`../docker-compose.yml`](../docker-compose.yml)：

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: java-labs-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: java_labs
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    container_name: java-labs-redis
    restart: always
    ports:
      - "6379:6379"
```

这个文件目前只编排了两个中间件：

- MySQL
- Redis

这说明当前仓库的本地开发策略是：

- 基础设施用 Compose 起
- Java 应用直接在 IDEA 里跑

这是一种很典型、也很高效的开发模式。

## 四、关键字段逐个理解

### 1. `services`

定义整套系统里的服务列表。

```yaml
services:
  mysql:
  redis:
```

这里的 `mysql` 和 `redis` 不只是配置块名字，它们在 Compose 网络里还会成为服务主机名。

### 2. `image`

表示直接使用某个现成镜像。

```yaml
image: mysql:8.0
```

意思是：

- 不从本地源码构建
- 直接拉官方镜像

这很适合数据库、缓存这类标准中间件。

### 3. `container_name`

显式指定容器名称。

```yaml
container_name: java-labs-mysql
```

这主要是为了命令行操作更直观，比如以后你执行：

```bash
docker logs java-labs-mysql
```

### 4. `environment`

注入容器运行时环境变量。

```yaml
environment:
  MYSQL_ROOT_PASSWORD: root
  MYSQL_DATABASE: java_labs
```

对 MySQL 来说，这些变量决定了：

- root 密码是什么
- 第一次启动时自动创建哪个数据库

### 5. `ports`

这是最容易误解的字段。

```yaml
ports:
  - "3306:3306"
```

含义是：

- 左边 `3306`：宿主机端口
- 右边 `3306`：容器内部端口

也就是：

- 你在 macOS 上访问 `localhost:3306`
- Docker 转发到 MySQL 容器里的 `3306`

### 6. `volumes`

用于数据持久化。

你当前仓库里已经预留了注释示例，但因为 Colima 下可能存在挂载权限问题，暂时没有启用：

```yaml
# volumes:
#   - ./mysql_data:/var/lib/mysql
```

它的意义是：

- 不把数据库数据只放在容器可写层
- 而是同步到宿主机目录

这样容器删掉后，数据仍可能保留。

### 7. `networks`

控制 Compose 里服务所在的网络。

你当前项目里定义了：

```yaml
networks:
  default:
    name: java-labs-network
```

这样做的好处是：

- 网络名称稳定
- 多个 Compose 文件将来更容易共享网络

## 五、`EXPOSE` 和 `ports` 不是一回事

这个点一定要分清。

`EXPOSE` 出现在 Dockerfile 里，作用更像“声明这个容器内部应用监听哪个端口”。

`ports` 出现在 Compose 里，作用是“把宿主机端口映射到容器端口”。

例如后端 Dockerfile 里可能有：

```dockerfile
EXPOSE 8080
```

这并不等于你的 macOS 已经能访问 `localhost:8080`。

只有 Compose 里再写：

```yaml
ports:
  - "8080:8080"
```

宿主机访问才真正打通。

一句话记忆：

- `EXPOSE` = 容器内部声明
- `ports` = 宿主机访问入口

## 六、`localhost` 和服务名的区别

这是 Docker 初学者最容易踩坑的点。

### 宿主机访问容器

如果你在 macOS 上访问 MySQL：

```text
localhost:3306
```

这是因为 Compose 用 `ports` 把容器端口映射到了宿主机。

### 容器访问自己

如果一个容器访问 `localhost`，它访问的是**自己这个容器**。

不是宿主机，也不是别的容器。

### 容器访问同网络下其他容器

如果 backend 和 mysql 都跑在 Compose 里，那么 backend 应该这样连数据库：

```text
mysql:3306
```

而不是：

```text
localhost:3306
```

记住这三个场景：

1. 宿主机访问容器：`localhost:端口`
2. 容器访问自己：`localhost:端口`
3. 容器访问同网络其他容器：`服务名:端口`

## 七、`up`、`start`、`stop`、`down` 的区别

这个也是 Compose 操作里最重要的基础。

| 命令 | 会创建容器 | 会启动容器 | 会删除容器 | 典型场景 |
| --- | --- | --- | --- | --- |
| `docker compose up -d` | 会 | 会 | 不会 | 第一次启动、配置变更后重建环境 |
| `docker compose start` | 不会 | 会 | 不会 | 已有容器，只想重新开机 |
| `docker compose stop` | 不会 | 会停止 | 不会 | 临时停服务，之后还要继续用 |
| `docker compose down` | 不会 | 会停止 | 会 | 彻底收掉这套环境 |

一句话记忆：

- `stop/start` 是“关机/开机”
- `down/up` 是“拆掉/重建”

## 八、为什么本地开发通常只把中间件放进 Compose

很多初学者会问：

> 为什么不把 Java 应用也一起放进 Docker，本地岂不是更统一？

答案是：本地开发阶段，调试效率更重要。

如果 Java 代码继续在 IDEA 中运行，你会得到这些收益：

1. 断点调试更直接
2. 热更新更快
3. 不用每次改代码都重建镜像

所以本地开发阶段常见的最佳实践是：

- Docker Compose 托管 MySQL / Redis / Nginx
- Java 应用留在 IDEA 里跑

而到了部署阶段，再把应用本身也镜像化。

## 九、在本项目中的推荐工作流

结合 [`../README.md`](../README.md) 和当前仓库配置，推荐流程如下：

### 1. 启动 Docker Runtime

macOS 下使用 Colima：

```bash
colima start
```

### 2. 启动中间件

```bash
docker compose up -d
```

### 3. 在 IDEA 中启动 Spring Boot

此时你的 Java 应用可以连接：

- MySQL：`localhost:3306`
- Redis：`localhost:6379`

### 4. 查看运行状态

```bash
docker compose ps
```

### 5. 结束开发后收环境

如果只是临时停机：

```bash
docker compose stop
```

如果想连容器和网络一起清掉：

```bash
docker compose down
```

## 十、常见反模式

- 把 `localhost` 当成“别的容器”地址使用。
- 以为 Dockerfile 里的 `EXPOSE` 就等于宿主机已开放端口。
- 每次只是临时停服务却直接 `down`，结果下次又得重新 `up`。
- 本地开发时把 Java 业务代码也强行塞进容器，导致调试体验极差。
- 在 Colima 挂载存在权限问题时直接照搬 Linux 下的数据卷方案，结果 MySQL 启动失败。

## 十一、AI 辅助开发实战建议

你可以直接这样给 Agent 下指令：

> 请帮我审查当前 `docker-compose.yml`，重点检查：
> 1. `ports` 是否合理；
> 2. 是否需要 `volumes`；
> 3. 服务名是否适合容器间访问；
> 4. 是否适合 macOS + Colima 本地开发。

或者：

> 请在现有 `docker-compose.yml` 中新增 `backend`、`frontend`、`nginx` 服务，要求：
> 1. 使用相对路径 build；
> 2. 为后端注入数据库和 Redis 连接信息；
> 3. 为前端保留独立构建阶段；
> 4. 输出完整的服务互联说明。

## 十二、配套代码示例解读

建议按下面顺序结合项目代码阅读：

1. [`../docker-compose.yml`](../docker-compose.yml)
   理解中间件编排、端口映射和网络。
2. [`53-docker-containerization-basics.md`](./53-docker-containerization-basics.md)
   理解 Dockerfile、镜像、容器和 Compose 的关系。
3. [`../java-web-starter/backend/Dockerfile`](../java-web-starter/backend/Dockerfile)
   看后端镜像如何构建。
4. [`../java-web-starter/nginx/default.conf`](../java-web-starter/nginx/default.conf)
   看 Nginx 如何充当前后端统一入口。

## 十三、扩展阅读

1. [`53-docker-containerization-basics.md`](./53-docker-containerization-basics.md)
2. [`../README.md`](../README.md)
3. [`36-packaging-and-running-guide.md`](./36-packaging-and-running-guide.md)
