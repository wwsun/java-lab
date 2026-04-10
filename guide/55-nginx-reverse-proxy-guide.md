# Nginx 反向代理指南：从 Express 网关到前后端统一入口

这节要解决的问题不是“记住几条 Nginx 指令”，而是建立一个工程心智：

> 在前后端分离的 Java Web 项目里，为什么还需要 Nginx 作为统一入口，它到底帮你解决了什么问题？

如果你有 Node.js/TypeScript 背景，可以先这样映射：

| Node.js / TS 世界 | Nginx 世界 | 你应该怎么理解 |
| --- | --- | --- |
| Express / NestJS 网关层 | Nginx Reverse Proxy | 统一接收请求，再转发到不同服务 |
| `app.use("/api", apiRouter)` | `location /api/ { proxy_pass ... }` | 按路径分流请求 |
| `express.static()` | Nginx 静态资源处理 | 专门负责前端静态文件分发 |
| `req.headers["x-forwarded-for"]` | `proxy_set_header X-Forwarded-For ...` | 透传真实客户端信息 |
| PM2 / Node 进程不直接裸露给公网 | 应用服务只暴露给 Nginx | 入口统一、职责分离 |

## 一、Nginx 在这个项目里扮演什么角色

对于前后端分离架构，Nginx 常常不是“可选优化”，而是入口层标准件。

它通常负责三件事：

1. 把 `/` 请求交给前端页面
2. 把 `/api/**` 请求转发给 Spring Boot
3. 在入口层统一处理压缩、超时、Header 转发等横切能力

你可以把它理解成：

> 一个比 Express 更专职的“网关层”和“静态资源服务器”。

## 二、先看你手头脚手架的真实配置

参考 [`../java-web-starter/nginx/default.conf`](../java-web-starter/nginx/default.conf)：

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        proxy_pass http://frontend:80;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
    }
}
```

虽然真实文件里还有 Header、WebSocket、Timeout、Gzip 等配置，但主线就这两条：

- `/` 转给 `frontend`
- `/api/` 转给 `backend`

这就是一个最典型的“路径分流”式反向代理。

## 三、什么叫“反向代理”

先区分一下正向代理和反向代理：

- 正向代理：客户端主动通过代理去访问外部服务
- 反向代理：客户端只知道代理地址，不直接知道后端真实服务

在这个项目里，浏览器只访问 Nginx：

```text
http://localhost/
http://localhost/api/users
```

浏览器并不需要知道：

- 前端容器叫 `frontend`
- 后端容器叫 `backend`
- Spring Boot 实际监听 `8080`

这些细节都被 Nginx 隐藏掉了。

## 四、为什么前后端分离还要统一入口

如果没有 Nginx，浏览器可能要直接访问：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080/api`

这会带来几个典型问题：

1. 前后端地址分裂
2. 跨域配置更复杂
3. 线上入口不统一
4. 静态资源和 API 缺少统一治理层

有了 Nginx 以后，浏览器只认一个入口：

- `http://localhost/`

然后由 Nginx 负责内部转发。

## 五、逐行拆解当前配置

### 1. `server`

```nginx
server {
    listen 80;
    server_name localhost;
}
```

`server` 可以理解成一台虚拟站点。

- `listen 80` 表示监听 80 端口
- `server_name localhost` 表示匹配 `localhost` 这个主机名

类比 Node.js：

```ts
app.listen(80);
```

但 Nginx 的 `server` 不只是“监听”，还承担站点级路由规则定义。

### 2. `location /`

```nginx
location / {
    proxy_pass http://frontend:80;
}
```

这表示：

- 所有匹配 `/` 的请求
- 都代理到 `frontend` 服务的 80 端口

这里的 `frontend` 不是魔法字符串，而是 Docker Compose 网络里的服务名。

### 3. `location /api/`

```nginx
location /api/ {
    proxy_pass http://backend:8080/api/;
}
```

这表示：

- 所有 `/api/` 开头的请求
- 交给 `backend:8080`

这里本质上就像：

```ts
app.use("/api", proxyToBackend);
```

### 4. `proxy_set_header`

当前配置里还有这一组：

```nginx
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
```

它们的作用是把客户端原始请求信息继续透传给后端。

这对后端非常重要，因为 Spring Boot 往往需要知道：

- 原始 Host 是什么
- 客户端真实 IP 是什么
- 请求原本是 HTTP 还是 HTTPS

如果不传这些 Header，后端看到的可能只有 Nginx 自己的信息。

### 5. WebSocket 支持

当前 `/api/` 配置里还有：

```nginx
proxy_http_version 1.1;
proxy_set_header Upgrade $http_upgrade;
proxy_set_header Connection "upgrade";
```

这通常是为了 WebSocket 或类似长连接升级协议做兼容。

即使当前业务还没用上，这也是一个比较稳妥的网关层默认配置。

### 6. Timeout

```nginx
proxy_connect_timeout 60s;
proxy_send_timeout 60s;
proxy_read_timeout 60s;
```

这三项分别控制：

- 连接后端超时
- 向后端发送请求超时
- 读取后端响应超时

如果没有这些配置，默认超时行为有时会不够清晰，问题排查也更麻烦。

### 7. Gzip

```nginx
gzip on;
gzip_types text/plain text/css application/json application/javascript;
```

这是入口层压缩优化。

它的价值是：

- 前端静态资源更小
- JSON API 响应更省带宽

这属于很典型的“放在网关层统一处理”的能力。

## 六、为什么这里要按路径分流

当前脚手架的入口设计非常典型：

- 页面请求走 `/`
- API 请求走 `/api/`

这种设计的好处是：

1. 路由边界清晰
2. 便于前后端协作
3. 便于 Nginx 做统一分流
4. 后续如果拆微服务，也更容易扩展更多路径前缀

例如未来可以继续扩展：

- `/api/auth/` → 认证服务
- `/api/admin/` → 管理后台服务
- `/static/` → 独立静态资源域

## 七、`proxy_pass` 最容易踩的坑

Nginx 初学者最容易在这里犯错，尤其是路径尾部的 `/`。

当前配置：

```nginx
location /api/ {
    proxy_pass http://backend:8080/api/;
}
```

这里尾部带 `/`，意味着 Nginx 会按它的 URI 重写规则来拼接路径。

对于学习阶段，你先记住一个工程结论就够了：

> `location` 和 `proxy_pass` 末尾的斜杠组合，会直接影响转发后的真实路径。

所以以后改 Nginx 时，不能只看“主机和端口对不对”，还要看：

- `location` 有没有尾 `/`
- `proxy_pass` 有没有尾 `/`

如果这里写错，常见现象就是：

- 后端明明有接口，Nginx 转发后却变 404

## 八、Nginx 和 Docker Compose 的关系

在这个脚手架里，Nginx 能写：

```nginx
proxy_pass http://frontend:80;
proxy_pass http://backend:8080/api/;
```

前提是这些服务在同一个 Docker 网络里。

也就是说：

- `frontend` 是服务名
- `backend` 是服务名

这和你前面学到的 Compose 服务发现是完全一致的。

所以 Nginx 配置其实不是孤立知识，它依赖你已经理解：

- 容器网络
- 服务名访问
- 端口映射

## 九、当前脚手架为什么是“前端容器 + 外层 Nginx”

你会注意到一个有意思的点：

- `frontend` 容器本身就已经是一个 Nginx
- 外部还有一个统一入口 Nginx

这意味着当前脚手架是“两层前端入口”模式：

1. `frontend` 容器内部 Nginx 负责静态文件托管
2. 外层 Nginx 负责统一入口和 API 反向代理

这不是唯一方案，但在模板工程里是合理的，因为：

- 职责清晰
- 前端构建和入口路由可以分开演进
- 后面替换内部静态托管策略时，外层入口不必大改

## 十、一个更贴近交付场景的心智图

```text
浏览器
  |
  v
Nginx 统一入口
  | \
  |  \
  |   -> /api/** -> Spring Boot backend
  |
  -> /** -> frontend 服务
```

这就是交付工程师视角下最典型的一层入口架构。

## 十一、最佳实践

- 前后端统一入口优先使用路径分流，结构简单且直观。
- 在反向代理层统一补齐 `X-Forwarded-*` 相关 Header。
- 把压缩、超时、WebSocket 支持放在入口层统一治理。
- API 前缀尽量固定，例如统一使用 `/api/`。
- 生产环境避免让后端服务直接裸露给公网，优先通过 Nginx 暴露。

## 十二、常见反模式

- 前端、后端各自暴露一个公网入口，导致域名和路由管理混乱。
- 忘记透传 `X-Forwarded-For`，后端日志里拿不到真实客户端 IP。
- 修改 `proxy_pass` 时忽略末尾 `/`，导致路径拼接异常。
- 在本地和生产使用完全不同的 URL 组织方式，导致联调和部署逻辑脱节。
- 把所有问题都堆到 Spring Boot 内部处理，而不是在网关层先做统一治理。

## 十三、配套代码示例解读

建议按下面顺序阅读：

1. [`../java-web-starter/nginx/default.conf`](../java-web-starter/nginx/default.conf)
   先看当前真实配置。
2. [`41-docker-compose-essentials.md`](./41-docker-compose-essentials.md)
   理解 Nginx 为什么能直接访问 `frontend` 和 `backend` 服务名。
3. [`53-docker-containerization-basics.md`](./53-docker-containerization-basics.md)
   把容器、镜像、Compose 和网关层串起来。
4. [`54-spring-profiles-multi-env.md`](./54-spring-profiles-multi-env.md)
   理解为什么不同部署方式下，后端连接配置会不同。

## 十四、AI 辅助开发实战建议

你可以直接这样给 Agent 下指令：

> 请基于当前前后端分离项目生成 Nginx `default.conf`，要求：
> 1. `/` 转发到前端服务；
> 2. `/api/` 转发到 Spring Boot 后端；
> 3. 保留 `X-Forwarded-*` 相关 Header；
> 4. 添加 Gzip 和超时配置；
> 5. 代码注释使用简体中文。

或者：

> 请审查我的 Nginx 反向代理配置，重点检查：
> 1. `proxy_pass` 路径拼接是否正确；
> 2. 是否遗漏真实 IP / Host 透传；
> 3. 是否适合前后端分离架构；
> 4. 是否存在可以下沉到网关层的统一配置。

## 十五、这一节学完后的验收标准

如果你能独立回答下面 4 个问题，就算这节已经入门：

1. Nginx 在前后端分离架构里为什么适合做统一入口？
2. `location /` 和 `location /api/` 分别在做什么？
3. 为什么 Nginx 能写 `frontend:80` 和 `backend:8080`？
4. 为什么 `proxy_pass` 末尾的 `/` 不能随便改？

## 十六、扩展阅读

1. [`41-docker-compose-essentials.md`](./41-docker-compose-essentials.md)
2. [`53-docker-containerization-basics.md`](./53-docker-containerization-basics.md)
3. [`54-spring-profiles-multi-env.md`](./54-spring-profiles-multi-env.md)
