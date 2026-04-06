# 规范：为 macOS (Intel) 补充 MySQL 安装指南

## 1. 目标 (Purpose)
在 `guide/40-mysql-configuration-guide.md` 中补充针对 macOS (Intel Chip) 用户使用 Homebrew 安装和配置 MySQL 8.0 的详细指南，以降低本地开发环境搭建的门槛。

## 2. 变更范围 (Scope)
- **目标文件**: `guide/40-mysql-configuration-guide.md`
- **新增内容**: 在现有文档的“一、依赖检查”之前插入一个新的章节“〇、 macOS (Intel) 本地安装指南 (Homebrew)”。
- **技术细节**:
  - 使用 `brew install mysql@8.0`（锁定版本以确保与教程一致）。
  - 环境变量配置说明（针对 zsh）。
  - 服务启动/停止/自启动命令。
  - `mysql_secure_installation` 安全加固步骤。

## 3. 详细设计 (Detailed Design)

### 3.1 目录结构调整
保持现有目录结构不变，仅对文件内容进行插入操作。

### 3.2 插入内容正文
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
- **仅启动一次**: `mysql.server start`
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
```

## 4. 验证计划 (Verification Plan)
- **文档检查**: 确保 Markdown 语法正确，代码块显示正常。
- **逻辑检查**: 步骤是否连贯，命令是否针对 Intel Mac (路径 `/usr/local/opt/`) 准确无误。
- **冲突检查**: 确保新章节与后续的 Docker 章节（推荐方式）不冲突，明确本地安装与 Docker 安装是可选替代方案。
