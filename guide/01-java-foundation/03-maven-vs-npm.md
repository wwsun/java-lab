# 03 - Maven 核心体系与 npm 对比指南

## 核心心智映射 (Core Mental Mapping)

作为 Node.js 开发者，你可能已经习惯了灵活的 `scripts` 和扁平的 `node_modules`。Maven 的哲学则更偏向“约定优于配置” (Convention over Configuration)。

| 领域 | Node.js / NPM | Java / Maven | 心智映射 |
| :--- | :--- | :--- | :--- |
| **元数据文件** | `package.json` | `pom.xml` | 项目的“身份证” |
| **锁定文件** | `pnpm-lock.yaml` | `<dependencyManagement>` | Maven 通常在父 pom 中控制版本 |
| **本地缓存** | `node_modules/` | `~/.m2/repository/` | Maven 是全局缓存，非项目内安装 |
| **依赖名称** | `package-name` | GAV (GroupId:ArtifactId:Version) | Java 使用三维坐标精确定位 |
| **自定义脚本** | `npm run xxx` | Lifecycle (生命周期) | Maven 使用预定义的流水线阶段 |

---

## 概念解释 (Conceptual Explanation)

### 1. GAV 坐标
Maven 使用三个维度来唯一定位一个包：
-   **GroupId**: 组织域名反转（如 `com.google`），类似 `@types` 这种 Scope。
-   **ArtifactId**: 项目名称（如 `guava`），即包名。
-   **Version**: 版本号（如 `31.0-jre`）。

### 2. Maven 生命周期 (Lifecycle)
Maven 不像 npm scripts 那样随意命名，它内置了严格的流水线。当你执行一个阶段时，它前面的所有阶段都会自动运行：
`clean` (清理) -> `compile` (编译) -> `test` (单元测试) -> `package` (打包) -> `install` (安装到本地仓库)。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 依赖声明
在 `pom.xml` 中引入依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.3.0</version>
    <scope>compile</scope> <!-- 作用域：compile(默认), test(类比 devDependencies), runtime, provided -->
</dependency>
```

### 常用指令
-   `mvn clean`: 删除 `target/` 目录。
-   `mvn compile`: 将 `.java` 编译为 `.class`。
-   `mvn test`: 运行 JUnit 测试。
-   `mvn package`: 生成 `.jar` 文件。
-   `mvn install`: 将 jar 包放入本地 `~/.m2` 目录，供其他本地项目引用。

---

## 典型用法 (Typical Usage)

### 标准的构建流程
在终端中，最高频的组合命令是：

```bash
mvn clean install -DskipTests
```
> **Tip**: `-DskipTests` 类似于在紧急发布时跳过繁琐的测试环节。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `pom.xml` 的 `<parent>` 标签：
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>
```
这在 Maven 中叫 **“父子继承”**。它预定义了大量的插件配置和依赖版本号，让子项目只需引用 `artifactId` 而无需写 `version`，保证了大型项目中依赖版本的一致性。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

Maven 的 XML 极其繁琐，请务必指挥 Agent 处理：

> **最佳实践 Prompt**:
> "我想在项目中集成 MySQL 驱动和 MyBatis-Plus，请帮我修改 `pom.xml`。
> 1. 请前往 Maven Central 查找最新稳定版。
> 2. 确保依赖的作用域（Scope）设置正确。
> 3. 如果有版本冲突，请告诉我如何使用 `<exclusions>` 排除它们。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Maven Official: Introduction to the Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) - 理解构建核心逻辑。
2. [MvnRepository](https://mvnrepository.com/) - Java 界的 npmjs.com，查依赖必备。
