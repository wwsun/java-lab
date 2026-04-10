# Docker 容器化入门：从 Node.js 发布流程到 Java Web 交付

你现在已经不缺“怎么把接口写出来”的能力，Docker 这一节真正要补的是另一套心智模型：

- 代码怎么变成**可交付产物**
- 产物怎么在**一致环境**里运行
- 多个服务怎么被**统一编排**

对于有 Node.js/TypeScript 背景的人，可以先把它类比成下面这张表：

| Node.js / TS 世界 | Docker 世界 | 你应该怎么理解 |
| --- | --- | --- |
| `package.json` + `scripts` | `Dockerfile` | 定义“怎么构建、怎么运行” |
| `npm run build` 产物 | Docker Image | 可重复分发的发布包 |
| `node dist/main.js` / `pm2 start` | Container | 基于镜像启动出来的运行实例 |
| `docker run ...` | 单服务启动命令 | 适合单个服务验证 |
| `docker-compose.yml` | 多服务编排清单 | 一次拉起 MySQL、Redis、Nginx、应用 |
| `.env` | `environment` / `env_file` | 运行时配置注入 |
| 本地 `data/` 目录 | Volume | 容器数据持久化 |

## 一、核心心智映射

如果用一句话概括：

> Docker 不是“虚拟机简化版”，而是“把应用运行环境也一起打包的交付标准”。

对 Java 来说，这尤其重要。因为 Java 应用通常至少会涉及：

- JDK/JRE 版本
- Maven 构建环境
- 数据库和缓存
- 反向代理
- 环境变量与配置文件

如果这些东西全靠“手工在机器上装”，就会出现典型问题：

- 你的机器能跑，别人机器跑不起来
- 线上和本地的 JDK 版本不一致
- MySQL、Redis 版本漂移
- 交付步骤依赖口口相传

Docker 的价值就是把这套环境声明出来，并让它可以重复执行。

## 二、先分清 4 个概念

### 1. Dockerfile

Dockerfile 是**镜像构建说明书**。

它回答的是：

- 基于什么基础镜像
- 复制哪些文件进去
- 执行哪些构建命令
- 容器启动时跑什么命令

你可以把它理解成：

> `Dockerfile = package.json scripts + CI 构建脚本 + 运行启动命令`

### 2. Image（镜像）

镜像是 Dockerfile 构建出来的**只读模板**。

类比 Node.js：

- 不是源码目录
- 更像“带运行环境的发布包”
- 可以被反复启动成多个容器

### 3. Container（容器）

容器是镜像的**运行实例**。

类比 Node.js：

- 镜像像构建好的发布包
- 容器像真正跑起来的 `node app.js` 进程

但容器比普通进程多了几层隔离：

- 独立文件系统视图
- 独立网络命名空间
- 独立环境变量

### 4. Compose

Compose 解决的是“单个容器能跑，但系统不是单个容器”的问题。

你的 Java Web 项目通常至少有：

- backend
- mysql
- redis
- nginx
- frontend

这时候你不可能每次都手写 5 条 `docker run`。

所以需要 `docker-compose.yml` 作为**系统级启动清单**。

## 三、为什么 Java 项目比 Node 项目更需要 Docker

Node 项目本地开发时，很多人只装一个 Node 版本就能跑大部分服务。

Java 项目往往更容易受以下因素影响：

- JDK 版本差异
- Maven 依赖缓存和构建环境
- Spring Boot 配置与中间件依赖
- MySQL / Redis 版本兼容性

所以在 Java 里，Docker 的位置更像“工程标准件”，而不只是“可选部署方式”。

## 四、先看你手头项目里的真实例子

### 1. 后端 Dockerfile 在做什么

参考 [`../java-web-starter/backend/Dockerfile`](../java-web-starter/backend/Dockerfile)：

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
# 先下载依赖，利用镜像层缓存
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

这段配置的核心意思是：

1. 第一阶段用 Maven + JDK 21 构建 Jar
2. 第二阶段只保留运行所需的 JRE
3. 最终镜像更小、更干净

这就是**多阶段构建**。

Node.js 类比：

- 第一阶段像 `npm ci && npm run build`
- 第二阶段像只把 `dist/` 和运行时所需文件拷进一个更轻的运行环境

### 2. 前端 Dockerfile 在做什么

参考 [`../java-web-starter/frontend/Dockerfile`](../java-web-starter/frontend/Dockerfile)：

```dockerfile
FROM node:20-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

核心意思：

- Node 镜像只负责构建前端静态资源
- 最终由 Nginx 托管 `dist/`

这其实就是前端部署的标准姿势，不是 React 特有，也不是 Java 特有。

### 3. Compose 在做什么

参考 [`../docker-compose.yml`](../docker-compose.yml) 和 [`../java-web-starter/nginx/default.conf`](../java-web-starter/nginx/default.conf)：

- Compose 负责把 MySQL、Redis、前后端、Nginx 放到同一个系统里
- Nginx 负责把 `/api/**` 转发给后端，把 `/` 转给前端

你可以把 Nginx 理解成：

> Express/NestJS 世界里的统一网关层，只是这里换成了专业反向代理。

## 五、Dockerfile 必学字段

### `FROM`

指定基础镜像。

```dockerfile
FROM eclipse-temurin:21-jre-alpine
```

意思是：运行环境基于 JRE 21，而不是完整 JDK。

### `WORKDIR`

设置容器内工作目录。

```dockerfile
WORKDIR /app
```

类似：

```ts
process.chdir("/app");
```

### `COPY`

复制文件进入镜像。

```dockerfile
COPY src ./src
```

### `RUN`

在构建镜像时执行命令。

```dockerfile
RUN mvn package -DskipTests -B
```

注意：`RUN` 发生在**构建阶段**，不是容器启动时。

### `EXPOSE`

声明容器预期监听的端口。

```dockerfile
EXPOSE 8080
```

它更像文档声明，不等于宿主机已经能访问。真正决定端口映射的是 Compose 里的 `ports`。

### `ENTRYPOINT` / `CMD`

定义容器启动时执行的命令。

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

这相当于：

```bash
java -jar app.jar
```

## 六、Compose 必学字段

以你当前仓库里的 [`../docker-compose.yml`](../docker-compose.yml) 为例：

```yaml
services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: java_labs

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

这里要理解 5 个字段：

### `services`

定义系统中的服务集合。

### `image`

表示直接使用现成镜像，不从本地源码构建。

比如：

- `mysql:8.0`
- `redis:7-alpine`

### `build`

表示根据当前目录下的 Dockerfile 构建镜像。

比如未来你的 `backend` 服务通常会这样写：

```yaml
backend:
  build:
    context: ./backend
  ports:
    - "8080:8080"
```

### `ports`

宿主机端口映射到容器端口。

```yaml
ports:
  - "3306:3306"
```

左边是你电脑访问的端口，右边是容器内进程监听的端口。

### `environment`

注入运行时环境变量。

对于 Spring Boot，这通常会映射到：

- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_REDIS_HOST`

## 七、一个最小的 Spring Boot 容器化例子

下面这个例子只展示“后端服务容器化”的最小闭环。

### Dockerfile

```dockerfile
# 基于 JDK 21 运行时镜像
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制构建产物到容器内
COPY target/demo.jar app.jar

# 声明应用监听端口
EXPOSE 8080

# 启动 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Compose

```yaml
services:
  app:
    build:
      context: .
    ports:
      - "8080:8080"
    environment:
      # 指定 Spring 运行环境
      SPRING_PROFILES_ACTIVE: dev
```

## 八、你在这个阶段最该掌握的 3 件事

### 1. 会区分“构建”和“运行”

很多初学者把下面几件事混在一起：

- Maven 打包
- Docker 构建镜像
- Docker 启动容器

你必须把它们拆开：

1. `mvn package`
2. `docker build`
3. `docker run` 或 `docker compose up`

### 2. 会区分“镜像”和“容器”

一个镜像可以启动多个容器。

这就像：

- 一份构建产物
- 可以启动多个进程实例

### 3. 会区分“宿主机”和“容器网络”

这是最容易踩坑的地方。

例如：

- 你在 macOS 上连 MySQL，通常用 `localhost:3306`
- 但如果 backend 也跑在 Compose 里，它访问 MySQL 往往应该用 `mysql:3306`

因为在 Compose 网络里，服务名本身就是主机名。

## 九、最佳实践

- 后端镜像优先使用多阶段构建，减少最终镜像体积。
- 运行时镜像只保留 JRE，不把 Maven 和源码带进去。
- 生产容器尽量使用非 root 用户运行。
- 基础设施用 Compose 管，业务配置走环境变量或独立配置文件。
- 本地开发阶段可以只把 MySQL/Redis/Nginx 放进容器，Java 进程继续在 IDEA 中调试。

## 十、常见反模式

- 把“构建工具”和“运行环境”混在一个大镜像里，导致镜像臃肿。
- 直接在容器里手工改配置，导致环境不可复现。
- 把数据库数据完全放在容器可写层里，容器删掉后数据丢失。
- 宿主机访问和容器间访问混用 `localhost`，结果服务互联失败。
- 每次改一行代码就重建整套容器，本地开发效率会非常差。

## 十一、配套代码示例解读

你可以按这个顺序读现有文件：

1. [`../java-web-starter/backend/Dockerfile`](../java-web-starter/backend/Dockerfile)
   看多阶段构建、非 root 用户、JRE 运行时。
2. [`../java-web-starter/frontend/Dockerfile`](../java-web-starter/frontend/Dockerfile)
   看前端静态资源构建和 Nginx 托管。
3. [`../java-web-starter/nginx/default.conf`](../java-web-starter/nginx/default.conf)
   看网关转发规则。
4. [`../docker-compose.yml`](../docker-compose.yml)
   看 MySQL/Redis 的本地编排。

## 十二、AI 辅助开发实战建议

你可以直接这样给 Agent 下指令：

> 请基于当前 Spring Boot 3 项目生成一个生产可用的 Dockerfile。要求：
> 1. 使用多阶段构建；
> 2. 运行时镜像只保留 JRE；
> 3. 使用非 root 用户；
> 4. 补充中文注释；
> 5. 同时生成 `docker-compose.yml`，包含 MySQL 8.4、Redis 7、Nginx。

或者：

> 请审查我的 `docker-compose.yml`，重点检查：
> 1. 端口映射是否合理；
> 2. 服务间主机名是否正确；
> 3. 是否缺少数据卷；
> 4. 是否适合本地开发调试。

## 十三、这一节学完后的验收标准

如果你已经能独立回答下面 4 个问题，说明 Docker 容器化这节算入门了：

1. Dockerfile、Image、Container、Compose 分别是什么？
2. 为什么后端 Dockerfile 常常要做多阶段构建？
3. 为什么容器里访问 MySQL 往往写 `mysql:3306` 而不是 `localhost:3306`？
4. 本地开发时，为什么常常只把中间件放进 Docker，而把 Java 进程继续放在 IDEA 里跑？

## 十四、扩展阅读

1. [41-docker-compose-essentials.md](./41-docker-compose-essentials.md)
2. [36-packaging-and-running-guide.md](./36-packaging-and-running-guide.md)
3. [README.md](../README.md)
