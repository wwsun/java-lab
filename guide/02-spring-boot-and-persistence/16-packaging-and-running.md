# 16 - Java 工程打包与运行：Maven Package & Fat JAR

## 核心心智映射 (Core Mental Mapping)

在 Node.js 中，我们直接 `npm start` 运行源码。在 Java 中，为了生产环境的稳定与解耦，我们通常将代码及其所有依赖打包成一个单一的可执行文件。

| 步骤 | Node.js (Runtime) | Java (Compile + Package) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **构建** | 通常无需编译 (TS 除外) | **`mvn compile`** | 源码转字节码 (.class) |
| **依赖** | `node_modules` 文件夹 | **内置于 JAR 包中** | 打包即分发 |
| **产物** | 一堆文件目录 | **单个 .jar 文件 (Fat JAR)** | 极简分发单元 |
| **运行** | `node app.js` | **`java -jar app.jar`** | 跨平台一次性启动 |
| **配置** | `.env` 或 `process.env` | **`application.yml` + 命令行参数** | 动态覆盖能力 |

---

## 概念解释 (Conceptual Explanation)

### 1. 什么是 Fat JAR (也叫 Uber JAR)？
普通的 JAR 包只包含你写的代码，运行它需要你在 CLASSPATH 中手动列出所有依赖库。Spring Boot 通过 `spring-boot-maven-plugin` 将你的代码及其所有依赖（如 MyBatis, Tomcat 宿主等）全部塞入一个 JAR。这就是“肥包”的由来。

### 2. Maven 生命周期
-   **`clean`**: 清理之前的旧产物。
-   **`compile`**: 检查代码语法并编译。
-   **`package`**: 运行测试并打包。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 常用打包命令
```bash
# 清理并打包，跳过单元测试（加速）
mvn clean package -DskipTests
```

### 运行时参数覆盖
你不需要修改 jar 包内部的配置文件，直接在命令行覆盖即可：
```bash
java -jar app.jar --server.port=9090 --spring.profiles.active=prod
```

---

## 典型用法 (Typical Usage)

### 1. 开发阶段本地运行
无需打包，直接启动热重载：
```bash
mvn spring-boot:run
```

### 2. 生成环境启动
建议使用 JVM 调优参数并后台运行：
```bash
nohup java -Xms512m -Xmx512m -jar app.jar &
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 **`target/` 目录**:
当你执行 `mvn package` 后，`target/` 下会出现一个 `java-labs-xxx.jar`。
-   你可以把它复制到任何安装了 JRE 21 的服务器上直接运行。
-   它不再依赖源码中的 `src/` 或 `pom.xml`，因为所有的元数据和 Class 都已经在包里了。
-   这种“编译一次，到处运行”的特性是 Java 占据大型企业后端市场的核心优势。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

打包与部署通常结合 CI/CD 流程。

> **最佳实践 Prompt**:
> "我需要为一个 Spring Boot 3 + Java 21 应用编写高效的 Dockerfile。
> 1. 请使用『分阶段构建 (Multi-stage Build)』：第一步用 Maven 编译，第二步提取 JAR 到轻量级 JRE 镜像。
> 2. 请利用 Spring Boot 3.2 的『分层 Jar (Layered JAR)』特性优化 Docker 镜像层的缓存。
> 3. 请生成对应的 docker-compose.yml 文件，包含数据库依赖和环境变量注入。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Spring Boot Maven Plugin Guide](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/) - 官方插件手册。
2. [Baeldung: Create a Fat JAR with Maven](https://www.baeldung.com/maven-fat-jar) - 经典教程。
3. [Dzone: Why Fat JAR is a good idea](https://dzone.com/articles/the-fat-jar-is-dead-long-live-the-fat-jar) - 深度探讨分发架构。
