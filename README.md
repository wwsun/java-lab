# Java Labs

这个项目是我用于从 Node.js/TypeScript 转型到 Java 后端开发的学习与实战工程。在这里，我将通过代码实践来掌握 Java、Spring Boot 以及微服务技术栈。本项目的学习理念是以 AI Agent 为向导，建立正确的架构与工程心智。

## 项目环境

- **JDK版本**: 21
- **构建工具**: Maven
- **IDE**: IntelliJ IDEA
- **开发框架**: Sprint Boot 3
- **测试框架**: JUnit 5

## 目录结构

- `src/main/java/`: 核心业务代码目录
- `src/test/java/`: 单元测试代码目录
- `pom.xml`: Maven 依赖及插件构建配置
- `LEARNING-ROADMAP.md`: 个人专属的 4 周全栈学习路线图与指南

## 快速开始

1. **环境准备**：确保已安装 Docker 和 Docker Compose。在项目根目录下运行以下命令启动中间件（MySQL & Redis）：
   ```bash
   docker compose up -d
   ```
2. **导入项目**：将本项目直接导入或使用 IntelliJ IDEA 打开。
3. **运行应用**：在 IDEA 中找到 `JavaLabsApplication.java`，右键点击并选择 `Run` 或 `Debug`。
4. **验证环境**：在终端运行以下命令以验证环境是否正常：
   ```bash
   mvn clean test
   ```
5. **开始学习**：遵循 `LEARNING-ROADMAP.md` 里的步骤开始实践代码，逐步构建功能！
