# 补充 Docker Compose 基础指南 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `guide/40-mysql-configuration-guide.md` 的“三、 使用 Docker 快速启动”章节中，补充 Docker Compose 的定义、安装步骤和核心常用命令。

**Architecture:** 直接编辑 Markdown 文档，在“三、 使用 Docker 快速启动”标题下插入新小节。

**Tech Stack:** Markdown

---

### Task 1: 准备与定位

**Files:**
- Modify: `guide/40-mysql-configuration-guide.md`

- [ ] **Step 1: 读取文件内容并确定插入位置**

确认 `## 三、 使用 Docker 快速启动 (推荐)` 标题下方的位置。

### Task 2: 插入 Docker Compose 内容

**Files:**
- Modify: `guide/40-mysql-configuration-guide.md`

- [ ] **Step 1: 在标题下插入“3.1 什么是 Docker Compose？”及相关安装/命令说明**

```markdown
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
```

- [ ] **Step 2: 调整后续内容的衔接**

确保后续的 `docker-compose.yml` 示例和启动命令部分逻辑通顺。

- [ ] **Step 3: 验证文件修改是否符合预期**

确认 Markdown 渲染正常，代码块格式正确，章节层级无误。

- [ ] **Step 4: 提交更改**

```bash
git add guide/40-mysql-configuration-guide.md
git commit -m "docs: add Docker Compose guide for beginners"
```
