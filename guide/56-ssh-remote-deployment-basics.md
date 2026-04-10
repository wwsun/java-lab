# SSH 远程部署基础：第一次把 Java Web 应用放到服务器上

这节解决的不是“背几条 SSH 命令”，而是理解一条完整交付链路：

> 本地写好的 Spring Boot + Docker Compose + Nginx 项目，如何通过 SSH 放到一台远程 Linux 服务器上运行。

如果你有 Node.js/TypeScript 背景，可以先这样映射：

| Node.js / TS 世界 | Java / Docker 部署世界 | 你应该怎么理解 |
| --- | --- | --- |
| `ssh user@server` 登录机器 | `ssh user@server` | 进入远程运行环境 |
| 在服务器上 `git pull && npm run build && pm2 restart` | `git pull && docker compose up -d --build` | 在远程机器上更新并拉起服务 |
| `.env.production` | 服务器上的 `.env` / 环境变量 | 生产配置注入位置 |
| Nginx 反向代理 Node 应用 | Nginx 反向代理 Spring Boot + 前端 | 统一外部入口 |
| 云服务器 / VPS | Linux 服务器 | 实际部署目标 |

## 一、先建立 3 个关键心智

### 1. SSH 不是部署工具，它是“远程操作通道”

SSH 的本质不是“自动部署”，而是：

- 安全登录远程服务器
- 执行远程命令
- 传输文件

你可以把它理解成：

> 你从本地终端伸手进远程 Linux 机器的标准方式。

### 2. 部署不是“把代码传上去”这么简单

一套可用部署至少要包含：

- 代码或镜像
- 环境变量
- Docker / Docker Compose 运行时
- 数据库 / Redis / Nginx
- 启动与重启方式

所以 SSH 只是入口，不是全部。

### 3. 第一次部署时，优先追求“可重复的手工流程”

在学习阶段，最重要的不是一上来就写 GitHub Actions。

你更需要先会这一条：

1. SSH 登录
2. 拉代码
3. 配环境变量
4. `docker compose up -d --build`
5. 验证服务是否起来

当这条链路稳定后，再上 CI/CD 才有意义。

## 二、部署链路长什么样

一个最小的交付流程通常如下：

```text
本地开发机
  |
  |  git push / 上传代码
  v
远程 Linux 服务器
  |
  |  ssh 登录
  |  拉代码 / 更新配置
  |  docker compose up -d --build
  v
Nginx
  | \
  |  \-> /api/** -> Spring Boot
  |
  \----> /** -> frontend
```

这条链路和你前面学过的 4 个专题是连续的：

1. Docker 容器化
2. Docker Compose 编排
3. Spring Profiles 多环境配置
4. Nginx 反向代理
5. SSH 远程部署

## 三、第一次远程部署需要哪些前置条件

以一台常见 Linux 服务器为例，你通常需要准备：

- 一台可 SSH 登录的服务器
- 一个具备 sudo 权限的用户
- 已开放 SSH 端口（通常是 22）
- 服务器已安装 Docker Engine 和 Docker Compose v2
- 项目代码可通过 Git 拉取，或通过 SCP / Rsync 上传

对学习阶段来说，最小可交付条件可以压缩成：

1. 你能 `ssh` 进去
2. 服务器能运行 `docker compose`
3. 代码能放到服务器上

## 四、最基础的 SSH 命令

### 1. 登录服务器

```bash
ssh user@your-server-ip
```

例如：

```bash
ssh deploy@192.168.1.100
```

这表示你以 `deploy` 用户登录到目标机器。

### 2. 指定私钥登录

如果服务器使用密钥认证：

```bash
ssh -i ~/.ssh/id_rsa deploy@your-server-ip
```

更常见的做法是把私钥配置进 `~/.ssh/config`，这样就不用每次都写 `-i`。

### 3. 执行一条远程命令

```bash
ssh deploy@your-server-ip "docker compose ps"
```

这相当于“远程开一枪就回来”，很适合做状态检查。

## 五、为什么生产环境更推荐 SSH Key，而不是密码

密码登录的主要问题是：

- 易被暴力破解
- 不适合自动化
- 团队协作下管理混乱

而 SSH Key 的模式是：

- 本地保留私钥
- 服务器保存公钥

部署时更常见、更安全的方式是：

1. 本地生成密钥对
2. 把公钥追加到服务器的 `~/.ssh/authorized_keys`
3. 以后直接无密码密钥登录

## 六、第一次手工部署的推荐步骤

下面是最适合学习阶段的一套顺序。

### 1. 登录服务器

```bash
ssh deploy@your-server-ip
```

### 2. 创建部署目录

```bash
mkdir -p ~/apps/java-web-starter
cd ~/apps/java-web-starter
```

### 3. 获取代码

如果用 Git：

```bash
git clone <repo-url> .
```

后续更新则执行：

```bash
git pull
```

### 4. 配置生产环境变量

根据脚手架里的 [`../java-web-starter/doc/deploy-guide.md`](../java-web-starter/doc/deploy-guide.md)，至少要准备：

```bash
MYSQL_ROOT_PASSWORD=your-strong-password
JWT_SECRET=your-256-bit-secret-key-change-in-production
```

通常会把这些变量写进：

- 项目根目录 `.env`
- 或部署平台注入的环境变量

### 5. 启动服务

```bash
docker compose up -d --build
```

这条命令的意思是：

- 重新构建需要构建的镜像
- 后台启动整套服务

### 6. 检查服务状态

```bash
docker compose ps
docker compose logs -f backend
docker compose logs -f frontend
```

### 7. 验证入口是否可访问

如果 Nginx 对外暴露 80 端口，那么通常验证：

- `http://your-server-ip/`
- `http://your-server-ip/api/...`

## 七、远程部署时，Spring Profiles 怎么配合

这一节和 [`54-spring-profiles-multi-env.md`](./54-spring-profiles-multi-env.md) 是连在一起的。

生产环境最关键的不是“复制 dev 配置”，而是：

- 激活 `prod`
- 把敏感配置改成环境变量注入

例如：

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/starter_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-password
SPRING_DATA_REDIS_HOST=redis
JWT_SECRET=your-strong-secret
```

如果你的 `backend` 也跑在 Compose 网络里，那么这里的数据库和 Redis 地址就不该再是 `localhost`，而应该是：

- `mysql`
- `redis`

## 八、为什么服务器里常用 `docker compose up -d --build`

本地开发时，你可能更常用：

```bash
docker compose up -d
```

而在远程部署时更常见的是：

```bash
docker compose up -d --build
```

因为部署场景下，你通常希望：

- 先基于最新代码构建镜像
- 再启动最新版本服务

一句话记忆：

- 本地开发偏“复用已有镜像”
- 远程部署偏“显式构建后启动”

## 九、最常见的部署后验证项

第一次部署后，不要只看“容器启动了没有”，而要看下面 4 类结果：

### 1. 服务是否运行

```bash
docker compose ps
```

### 2. 应用是否正常输出日志

```bash
docker compose logs -f backend
```

### 3. 入口是否可访问

```bash
curl http://127.0.0.1/
curl http://127.0.0.1/api/health
```

### 4. 关键依赖是否真的连通

典型症状包括：

- Spring Boot 启动失败，报数据库连接错误
- Redis 主机名解析失败
- Nginx 转发后 502

这类问题都不是“SSH 有问题”，而是运行配置有问题。

## 十、第一次部署最容易踩的坑

### 1. 服务器上没有 Docker Compose v2

症状：

- `docker compose` 命令不存在

### 2. 还在用开发环境配置

症状：

- 后端连 `localhost:3306`
- 但数据库实际跑在另一个容器里

### 3. Nginx 能起，但反向代理 502

症状：

- 入口页面打开失败
- `/api/` 请求返回 502

常见原因：

- 后端服务没起来
- 服务名写错
- 后端端口写错

### 4. 只会 `up`，不会看日志

很多新手一看到容器没起来，就反复执行：

```bash
docker compose up -d --build
```

这通常没有帮助。真正该看的往往是：

```bash
docker compose logs -f backend
docker compose logs -f nginx
```

### 5. 把服务器当成“远程本地机”

例如：

- 误以为服务器里也能用 `localhost` 访问别的容器
- 误以为本地路径挂载在服务器上也存在

远程部署必须重新区分：

- 宿主机
- 容器
- 容器网络

## 十一、一个最小部署命令清单

如果你只想记住第一次部署最小闭环，记这 6 条命令就够了：

```bash
ssh deploy@your-server-ip
cd ~/apps/java-web-starter
git pull
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

这 6 条命令已经覆盖了：

- 登录
- 更新代码
- 启动服务
- 查看状态
- 排查错误

## 十二、最佳实践

- 第一次部署先走手工 SSH 流程，跑通后再考虑自动化。
- 生产环境优先使用 SSH Key，不要依赖密码登录。
- 生产配置优先用 `.env` 或环境变量注入，不把密钥写进仓库。
- 部署后先看 `docker compose ps`，再看日志，再看入口连通性。
- 先验证单机单套部署流程，再考虑灰度、滚动更新、CI/CD。

## 十三、常见反模式

- 没跑通过手工部署就直接上 GitHub Actions。
- 生产环境继续沿用 `application-dev.yml` 的 `localhost` 配置。
- 只会执行 `docker compose up -d --build`，不会看 `logs`。
- 把数据库密码、JWT 密钥直接提交进 Git 仓库。
- 误把 SSH 当成部署本身，而不是部署通道。

## 十四、配套代码示例解读

建议按下面顺序阅读：

1. [`../java-web-starter/doc/deploy-guide.md`](../java-web-starter/doc/deploy-guide.md)
   看脚手架里的部署清单和环境变量说明。
2. [`54-spring-profiles-multi-env.md`](./54-spring-profiles-multi-env.md)
   看生产环境配置为什么必须从环境变量注入。
3. [`55-nginx-reverse-proxy-guide.md`](./55-nginx-reverse-proxy-guide.md)
   看远程部署后，为什么最终入口还是 Nginx。
4. [`41-docker-compose-essentials.md`](./41-docker-compose-essentials.md)
   看服务编排和日志排查的基础命令。

## 十五、AI 辅助开发实战建议

你可以直接这样给 Agent 下指令：

> 请基于当前 Docker Compose 项目生成一份“第一次 SSH 远程部署操作清单”，要求：
> 1. 包含服务器准备、代码拉取、环境变量配置、服务启动、日志检查；
> 2. 明确区分宿主机地址和容器服务名；
> 3. 使用简体中文注释；
> 4. 补充常见故障排查。

或者：

> 请审查我的远程部署步骤，重点检查：
> 1. 是否错误使用了 `localhost`；
> 2. 是否遗漏了 `SPRING_PROFILES_ACTIVE=prod`；
> 3. 是否缺少日志和健康检查步骤；
> 4. 是否存在把敏感配置写死进仓库的问题。

## 十六、这一节学完后的验收标准

如果你能独立回答下面 4 个问题，就算这节已经入门：

1. SSH 在部署流程中扮演什么角色？
2. 为什么第一次部署先走手工 SSH 流程，比直接上 CI/CD 更合理？
3. 为什么远程容器内连接 MySQL / Redis 时通常用服务名而不是 `localhost`？
4. 为什么部署完成后，`docker compose ps` 和 `docker compose logs` 比重复执行 `up -d --build` 更重要？

## 十七、扩展阅读

1. [`54-spring-profiles-multi-env.md`](./54-spring-profiles-multi-env.md)
2. [`55-nginx-reverse-proxy-guide.md`](./55-nginx-reverse-proxy-guide.md)
3. [`../java-web-starter/doc/deploy-guide.md`](../java-web-starter/doc/deploy-guide.md)
