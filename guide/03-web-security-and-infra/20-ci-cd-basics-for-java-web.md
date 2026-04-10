# 20 - CI/CD 入门指南：从手工 SSH 部署到自动化流水线

这节要解决的不是“背 GitHub Actions 语法”，而是理解一条工程演进路径：

> 为什么你已经会手工 SSH 部署之后，还需要 CI/CD，以及它到底在自动化哪些步骤。

如果你有 Node.js/TypeScript 背景，可以先这样映射：

| Node.js / TS 世界 | Java Web 世界 | 你应该怎么理解 |
| --- | --- | --- |
| GitHub Actions 跑 `npm test && npm run build` | GitHub Actions 跑 `mvn verify` + 前端构建 | 自动化构建与校验 |
| 推送后自动部署到服务器 | 推送后自动执行 Docker Compose 部署 | 自动化交付 |
| Vercel / Railway 自动化发布 | 自建服务器上的 CI/CD | 你自己负责部署链路 |
| `.github/workflows/*.yml` | `.github/workflows/*.yml` | GitHub 的流水线定义 |
| `.gitlab-ci.yml` | `.gitlab-ci.yml` | GitLab 的流水线定义 |

## 一、先建立 3 个关键心智

### 1. CI 和 CD 不是一回事

先拆开理解：

- **CI（Continuous Integration）持续集成**
  重点是自动化执行测试、构建、基础校验，确保代码合并前后始终可用。
- **CD（Continuous Delivery / Deployment）持续交付 / 持续部署**
  重点是把已经通过校验的版本自动发布到目标环境。

一句话记忆：

- CI 关注“能不能安全合并”
- CD 关注“能不能稳定交付”

### 2. CI/CD 不是替代手工部署，而是固化手工部署

你前面已经学了 SSH 远程部署。那套手工流程其实就是 CI/CD 的原型：

1. 拉代码
2. 执行测试
3. 构建产物
4. 登录服务器
5. 部署并启动

CI/CD 做的事情，本质上只是把这几步写成机器可重复执行的脚本。

### 3. 在学习阶段，先会“设计流水线”，再生成具体模板

一条可靠流水线通常至少要回答：

- 什么时候触发
- 在哪个环境跑
- 先跑什么检查
- 哪一步产出什么
- 什么条件下允许部署

如果这些没想清楚，直接生成 YAML，大概率只是“会跑的配置”，不是“可维护的流水线”。

## 二、为什么你现在需要 CI/CD

当项目还在本地实验阶段时，手工运行命令已经够用。

但一旦进入脚手架和交付阶段，问题就变了：

- 如何确保后端改动不会把构建打坏
- 如何确保前端构建产物始终可生成
- 如何确保部署步骤不依赖手工记忆
- 如何让“主分支始终可部署”

这时候 CI/CD 的价值就出来了：

1. 自动执行校验
2. 自动执行构建
3. 自动执行部署
4. 自动保留日志和结果

## 三、先看你脚手架里已有的材料

参考 [`../java-web-starter/doc/deploy-guide.md`](../java-web-starter/doc/deploy-guide.md)，当前已经给了一版 GitHub Actions 模板，但还没有真正落地的：

- `.github/workflows/*.yml`
- `.gitlab-ci.yml`

这说明当前阶段更适合先把心智补齐，再去生成最终模板文件。

## 四、一条最小 CI/CD 流水线长什么样

对于你现在的前后端分离 Java Web 项目，一条最小流水线通常包含 3 段：

### 1. Backend Job

负责：

- 安装 JDK
- 缓存 Maven 依赖
- 执行 `mvn clean verify`

### 2. Frontend Job

负责：

- 安装 Node.js
- 缓存 npm 依赖
- 执行 `npm ci`
- 执行 `npm run build`

### 3. Deploy Job

负责：

- 在主分支或发布分支触发
- 连接远程服务器
- 更新代码或同步构建产物
- 执行 `docker compose up -d --build`

可以把它理解成：

```text
代码提交
  |
  +--> Backend 校验
  |
  +--> Frontend 校验
         |
         v
      全部通过
         |
         v
      Deploy
```

## 五、GitHub Actions 的最小心智模型

GitHub Actions 的核心结构其实很简单：

### 1. `on`

定义触发条件。

例如：

```yaml
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
```

意思是：

- 推送到 `main` / `develop` 时触发
- 对 `main` 发起 PR 时也触发

### 2. `jobs`

定义要执行的任务集合。

例如：

- `backend`
- `frontend`
- `deploy`

### 3. `steps`

定义 job 内的具体执行步骤。

例如：

- checkout 代码
- 安装 Java
- 跑测试
- 构建前端

## 六、GitLab CI 的最小心智模型

GitLab CI 的抽象略有不同，但本质一样。

它通常会先定义：

- `stages`

例如：

```yaml
stages:
  - test
  - build
  - deploy
```

然后每个 job 会被分配到某个 stage：

- `backend-test`
- `frontend-build`
- `deploy-prod`

一句话记忆：

- GitHub Actions 更强调 `jobs`
- GitLab CI 更强调 `stages + jobs`

## 七、你这个项目最适合的流水线设计

结合你当前技术栈：

- 后端：Spring Boot + Maven + JDK 21
- 前端：Vite + React + TypeScript + npm
- 部署：Docker Compose + Nginx + SSH

推荐的最小流水线设计是：

### PR 校验流水线

触发时机：

- 对 `main` 发起 Pull Request

执行内容：

- backend test
- frontend build

目的：

- 防止坏代码进入主分支

### 主分支部署流水线

触发时机：

- push 到 `main`

执行内容：

- backend test
- frontend build
- SSH 部署到服务器

目的：

- 主分支保持可部署状态

## 八、GitHub Actions 最小示例

下面是一版适合你当前项目结构的最小教学版示例：

```yaml
name: CI-CD

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
          cache: maven
      - name: Build Backend
        working-directory: backend
        run: mvn clean verify -B

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: npm
          cache-dependency-path: frontend/package-lock.json
      - name: Build Frontend
        working-directory: frontend
        run: |
          npm ci
          npm run build

  deploy:
    if: github.ref == 'refs/heads/main'
    needs: [backend, frontend]
    runs-on: ubuntu-latest
    steps:
      - name: Deploy via SSH
        run: echo "replace with real deploy script"
```

这版配置的重点不在“已经可生产”，而在于你能看懂它的结构。

## 九、GitLab CI 最小示例

对应的 GitLab CI 教学版可以这样理解：

```yaml
stages:
  - test
  - build
  - deploy

backend-test:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  script:
    - cd backend
    - mvn clean verify -B

frontend-build:
  stage: build
  image: node:20
  script:
    - cd frontend
    - npm ci
    - npm run build

deploy-prod:
  stage: deploy
  script:
    - echo "replace with real deploy script"
  only:
    - main
```

你可以看到，它和 GitHub Actions 的差异主要在表达形式，不在核心逻辑。

## 十、部署 job 到底自动化了什么

这一点一定要和前面的 SSH 教学串起来。

你在 `56-ssh-remote-deployment-basics.md` 里学到的是手工流程：

1. SSH 登录
2. `git pull`
3. `docker compose up -d --build`
4. 检查日志

CI/CD 的 deploy job 本质上只是把这些命令改成自动执行。

例如伪代码：

```bash
ssh deploy@server <<'EOF'
cd ~/apps/java-web-starter
git pull
docker compose up -d --build
docker compose ps
EOF
```

所以如果手工流程没跑通，自动化部署也大概率不会真稳定。

## 十一、Secrets 应该放在哪里

这是 CI/CD 里最重要的安全问题之一。

像下面这些东西都不应该直接写进仓库：

- SSH 私钥
- 服务器 IP 和敏感账号
- JWT_SECRET
- 数据库密码

正确做法通常是：

- GitHub Actions 用 `Repository Secrets`
- GitLab CI 用 `CI/CD Variables`

也就是说：

> 流水线定义在仓库里，敏感值保存在平台 Secret 仓库里。

## 十二、你这个项目最常见的 CI/CD 设计错误

### 1. 没有先做 PR 校验，直接 push 就部署

风险：

- 主分支被坏构建污染

### 2. 后端和前端放在同一个 job 里硬跑

风险：

- 日志难读
- 缓存复用差
- 失败定位差

### 3. 部署 job 不依赖前置校验

风险：

- 构建没通过也可能被部署

### 4. 把 SSH 私钥直接写进 YAML

风险：

- 直接泄露生产环境访问能力

### 5. 手工部署都没跑通就强上 CI/CD

风险：

- 流水线会变成“自动化报错机”

## 十三、最佳实践

- 先手工跑通 SSH 部署，再上自动化。
- PR 和主分支部署分开设计，不要一条流水线包打天下。
- 后端和前端拆成独立 job，便于缓存和排查。
- 部署只在主分支或受控分支触发。
- 所有敏感配置放进 Secrets / Variables，不进仓库。

## 十四、常见反模式

- 流水线只会 build，不会 test。
- 把所有逻辑塞进一个超长 shell 脚本步骤里。
- 把 dev 配置和 prod 部署逻辑混在一起。
- 不区分 PR 校验和正式部署。
- 忽略失败日志，只盯着“有没有绿色对勾”。

## 十五、配套代码示例解读

建议按下面顺序阅读：

1. [`../java-web-starter/doc/deploy-guide.md`](../java-web-starter/doc/deploy-guide.md)
   看脚手架里已有的 GitHub Actions 模板雏形。
2. [`56-ssh-remote-deployment-basics.md`](./56-ssh-remote-deployment-basics.md)
   看 deploy job 的手工原型是什么。
3. [`54-spring-profiles-multi-env.md`](./54-spring-profiles-multi-env.md)
   看生产环境配置如何注入。
4. [`41-docker-compose-essentials.md`](./41-docker-compose-essentials.md)
   看部署动作最终落在哪里。

## 十六、AI 辅助开发实战建议

你可以直接这样给 Agent 下指令：

> 请基于当前前后端分离项目生成一套最小可用的 GitHub Actions 工作流，要求：
> 1. backend 和 frontend 分成独立 job；
> 2. PR 时只做校验，不做部署；
> 3. push 到 main 时才执行部署；
> 4. 部署步骤基于 SSH + Docker Compose；
> 5. 用简体中文注释关键逻辑。

或者：

> 请同时生成 GitHub Actions 和 `.gitlab-ci.yml` 两套模板，并说明：
> 1. 两者在 stage / jobs 表达上的差异；
> 2. 哪些变量应该放到 Secrets；
> 3. 哪些步骤属于 CI，哪些属于 CD。

## 十七、这一节学完后的验收标准

如果你能独立回答下面 4 个问题，就算这节已经入门：

1. CI 和 CD 的区别是什么？
2. 为什么 CI/CD 不是替代手工部署，而是固化手工部署？
3. 为什么 deploy job 必须依赖前面的 backend / frontend 校验结果？
4. 为什么 SSH 私钥和 JWT_SECRET 不能直接写进流水线 YAML？

## 十八、扩展阅读

1. [`56-ssh-remote-deployment-basics.md`](./56-ssh-remote-deployment-basics.md)
2. [`../java-web-starter/doc/deploy-guide.md`](../java-web-starter/doc/deploy-guide.md)
3. [`41-docker-compose-essentials.md`](./41-docker-compose-essentials.md)
