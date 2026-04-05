# Java 工程打包与运行：Maven Package & Fat JAR

在 Node.js 中，我们直接 `npm start` 运行源码。在 Java 中，为了生产环境的部署，我们通常将代码及其所有依赖打包成一个 **Fat JAR (也称为 Uber JAR)**。

## 1. 核心流程对照：从 Node.js 到 Java

| 步骤 | Node.js | Java (Maven + Spring Boot) |
| :--- | :--- | :--- |
| **构建逻辑** | 不需要传统编译 (TS 除外) | `mvn compile` (Java -> Class) |
| **安装依赖** | `npm install` | `mvn install` |
| **打包产物** | 通常是一堆 `node_modules` | 单个 `.jar` 文件 |
| **运行产物** | `node app.js` | `java -jar app.jar` |

## 2. Fat JAR 的奥秘

传统的 JAR 包只包含你写的代码（Class 文件）。而 Spring Boot 项目在打包时，会使用 `spring-boot-maven-plugin` 将你的代码以及 `pom.xml` 中定义的所有三方库（如 MyBatis, HikariCP 等）全部塞入一个 JAR。这就是 **"Fat" (胖)** 的由来——它包含了运行所需的一切。

## 3. 手把手打包与运行指引

### 3.1 打包 (Package)
在项目根目录下执行：
```bash
mvn clean package -DskipTests
```
- **`clean`**：清除旧的构建结果 (`target/` 目录)。
- **`package`**：执行编译、测试 (如有)、打包全流程。
- **`-DskipTests`**：跳过单元测试（加速构建）。

### 3.2 运行 (Run)
打包成功后，产物在 `target/` 目录下。你可以通过以下命令运行它：
```bash
java -jar target/java-labs-1.0-SNAPSHOT.jar
```
你会看到熟悉的 Spring Boot 启动日志。

### 3.3 本地快速运行 (推荐开发使用)
如果你只是想在开发时运行，不需要打包：
```bash
mvn spring-boot:run
```

## 4. 生产环境部署建议
在真实的 CI/CD 流程中，通常会将生成的 `.jar` 文件放入 Docker 镜像：
1.  **Dockerfile 示例**：
    ```dockerfile
    FROM openjdk:21-slim
    COPY target/*.jar app.jar
    ENTRYPOINT ["java", "-jar", "/app.jar"]
    ```
2.  **配置外部化**：生产环境的数据库连接信息不应硬编码在 JAR 内部，可以通过命令行参数覆盖：
    ```bash
    java -jar app.jar --spring.datasource.password=prod-pwd
    ```

---
**扩展阅读：**
- [Maven Package Documentation](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- [Spring Boot Maven Plugin Reference](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)
- [Baeldung: Create a Fat JAR with Maven](https://www.baeldung.com/maven-fat-jar)
