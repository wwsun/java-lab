# 为 macOS (Intel) 补充 MySQL 安装指南 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `guide/40-mysql-configuration-guide.md` 中补充使用 Homebrew 安装 MySQL 8.0 的详细指南。

**Architecture:** 直接编辑 Markdown 文档，在“依赖检查”章节之前插入新章节。

**Tech Stack:** Markdown

---

### Task 1: 准备与定位

**Files:**
- Modify: `guide/40-mysql-configuration-guide.md`

- [ ] **Step 1: 读取文件内容并确定插入位置**

确认 `# 二、 核心配置 (application.yml)` 之前的位置。

### Task 2: 插入安装指南内容

**Files:**
- Modify: `guide/40-mysql-configuration-guide.md`

- [ ] **Step 1: 在“一、 依赖检查”之前插入“〇、 macOS (Intel) 本地安装指南 (Homebrew)”**

```markdown
## 〇、 macOS (Intel) 本地安装指南 (Homebrew)

对于希望在本地系统直接运行 MySQL 而不使用 Docker 的用户，请按以下步骤操作：

### 1. 安装 MySQL 8.0
我们推荐安装 8.0 版本以获得最佳的稳定性。
```bash
brew install mysql@8.0
```

### 2. 配置环境变量
安装完成后，需要将 MySQL 的二进制路径添加到你的 Shell 配置文件中（通常是 `~/.zshrc`）：
```bash
echo 'export PATH="/usr/local/opt/mysql@8.0/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### 3. 管理 MySQL 服务
使用 Homebrew Services 管理服务非常方便：
- **启动并设置开机自启**: `brew services start mysql@8.0`
- **停止服务**: `brew services stop mysql@8.0`

### 4. 安全加固与密码设置
安装后，默认 `root` 用户没有密码。运行以下命令进行初始化：
```bash
mysql_secure_installation
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
```

- [ ] **Step 2: 验证文件修改是否符合预期**

确认 Markdown 渲染正常，代码块格式正确。

- [ ] **Step 3: 提交更改**

```bash
git add guide/40-mysql-configuration-guide.md
git commit -m "docs: add MySQL installation guide for macOS Intel"
```
