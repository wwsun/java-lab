# Maven 核心体系与 npm 对比指南

对于习惯了 Node.js（npm/yarn/pnpm）体系的前端开发者，Java 世界的包管理与构建工具最具代表性的便是 Maven（以及近几年也非常流行的 Gradle）。在本项目中，我们使用 Maven。

本文将帮助您快速建立起从 Node.js 到 Java 的工程化心智模型。

## 核心心智映射 (Mental Model Mapping)

### 1. 核心文件对比

| Node.js / TypeScript | Java (Maven) | 核心作用 |
| --- | --- | --- |
| `package.json` | `pom.xml` (Project Object Model) | 声明依赖、插件以及项目元信息 |
| `package-lock.json` / `pnpm-lock.yaml` | (Maven 默认没有锁定文件，通常固定版本号或用 `<dependencyManagement>`) | 锁定具体依赖图 |
| `node_modules/` | `~/.m2/repository/` (全局) | 第三方依赖被下载存放的目录 |
| `dist/` 或 `build/` | `target/` | 编译后生成的字节码和最终包存放的位置 |

### 2. 依赖管理 (`<dependencies>`)

在 `pom.xml` 中引入一个包，您需要提供**依赖坐标 (GAV)**，相当于 NPM 中的包名。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId> <!-- 类似 NPM 的 scope, 如 @babel -->
    <artifactId>spring-boot-starter-web</artifactId> <!-- 类似 NPM 的 package name, 如 core -->
    <version>3.2.0</version> <!-- 类似 NPM 的 version -->
    <scope>test</scope> <!-- 【注意】如果是 test，相当于放进了 devDependencies -->
</dependency>
```

### 3. 生命周期与脚本 (Lifecycle & Scripts)

在 `package.json` 中，我们可以随意定义 `scripts`：
```json
"scripts": {
  "clean": "rm -rf dist",
  "build": "tsc",
  "test": "jest"
}
```

但在 Maven 中，**生命周期是强制规定好的**，主要分为几个阶段，且依次执行（当你执行后面阶段时，前面阶段会自动被执行）：

- `mvn clean`：清除 `target/` 目录（类似 `rm -rf dist`）
- `mvn compile`：编译业务代码（类似 `tsc`）
- `mvn test`：执行单元测试（类似 `jest`）
- `mvn package`：打包成 `.jar` 归档文件（类似 `webpack` 打出 dist 文件）
- `mvn install`：将打好的 `.jar` 安装到本地电脑的 `~/.m2` 目录中，供其他本地 Java 项目使用。

所以，日常开发中最高频的命令就是：
```bash
mvn clean install
```
它会帮你：清理 -> 编译 -> 跑测试 -> 打包 -> 安装到本地仓库。

## 面向 AI 时代的开发建议

1. **不用手写 XML**：找依赖时，不要手写 `<dependency>` 标签。去 [MvnRepository](https://mvnrepository.com/) 搜索包名，直接把 XML 块复制过来；或者直接让 Claude Code 帮你添加指定包的依赖。
2. **多模块协作 (Monorepo)**：Maven 的设计天生支持多模块（Multi-module 就像如今 Pnpm Workspace 时代的 Monorepo），理解了 `pom.xml` 的层级继承，你就理解了 Java 微服务切分的基石。
