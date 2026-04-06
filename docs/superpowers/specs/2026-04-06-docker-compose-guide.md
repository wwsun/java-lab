# 规范：在 MySQL 配置指南中补充 Docker Compose 基础

## 1. 目标 (Purpose)
在 `guide/40-mysql-configuration-guide.md` 的“三、 使用 Docker 快速启动”章节中，补充 Docker Compose 的基础知识、安装方法及常用命令。这旨在帮助开发者理解 Docker Compose 的作用，并学会如何在 macOS 环境下通过 Homebrew 安装和管理它。

## 2. 变更范围 (Scope)
- **目标文件**: `guide/40-mysql-configuration-guide.md`
- **新增内容**: 在“三、 使用 Docker 快速启动”标题下，插入一个新的子章节“3.1 什么是 Docker Compose？”。
- **技术细节**:
  - 简要解释 Docker Compose 的概念。
  - 使用 `brew install docker-compose` 作为 macOS 安装推荐方式。
  - 补充 `up -d`, `ps`, `down`, `logs -f` 等核心常用命令。

## 3. 详细设计 (Detailed Design)

### 3.1 目录结构调整
保持现有目录结构不变，仅对文件内容进行插入操作。

### 3.2 插入内容正文
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

## 4. 验证计划 (Verification Plan)
- **文档检查**: 确保 Markdown 语法正确，特别是嵌套的列表和代码块。
- **逻辑检查**: 确认插入位置在 `docker-compose.yml` 示例代码之前，以便用户在尝试运行命令前先了解工具。
- **准确性检查**: 确认 Homebrew 命令 `brew install docker-compose` 在当前 macOS 环境下的准确性。
