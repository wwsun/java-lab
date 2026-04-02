# Java 代码规范与静态检查工具指南

作为资深的 Node.js/TypeScript 开发者，你可能已经习惯了 `eslint` + `prettier` 的"全家桶"式体验。在 Java 生态中，代码质量保障由多个层级的工具共同完成。

## 1. 概念映射对比

| 功能描述 | Node.js / TypeScript | Java 工具 | 核心差异点 |
| :--- | :--- | :--- | :--- |
| **代码风格 (Style)** | `eslint` (style rules) | **Checkstyle** | 配置非常精细，通常由架构师定义并分发 xml 配置文件。 |
| **代码格式化 (Format)** | `prettier` | **Google Java Format** | 极其强制，减少团队内部关于空格和换行的争论。 |
| **逻辑风险 (Logic/Bug)** | `eslint` (logic rules) | **PMD** / **SpotBugs** | 擅长检测：空指针风险、未关闭的资源、死循环、并发隐患。 |
| **全栈检测 (Security)** | `snyk` / `eslint-plugin-security` | **SonarQube / SonarLint** | 工业级标准，涵盖安全漏洞、代码覆盖率。 |
| **IDE 实时提示** | ESLint VSCode Plugin | **SonarLint (推荐)** | **必装插件**。实时高亮，提供修复代码建议，体验最接近 ESLint。 |

## 2. 现代生产环境的最佳实践：SonarLint

对于你目前（第一周）的学习阶段，我不建议你立刻折腾复杂的 `pom.xml` 插件配置（那往往是项目初始化时做的事）。

### 💡 你的第一步策略
1.  **安装插件**：在 IntelliJ IDEA 插件市场搜索并安装 **SonarLint**。
2.  **实时修复**：它会像 ESLint 一样，在你的编辑器里用波浪线标出有问题的地方。
3.  **学习 Java Idioms**：当你 hover 在错误上时，它会告诉你为什么这么写不好（例如："不要使用 `new Thread()`，请使用线程池"），这对你理解 Java 的最佳实践极有帮助。

## 3. Maven 工程中的集成示例

在项目的后期阶段，你会看到 `pom.xml` 中类似这样的配置，它们会在打包时强制检查：

```xml
<!-- Checkstyle 示例 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
        <failOnViolation>true</failOnViolation> <!-- 校验失败则构建失败 -->
    </configuration>
</plugin>
```

## 4. 扩展阅读
1.  [SonarLint 官方文档](https://www.sonarsource.com/products/sonarlint/)：了解如何在本地 IDE 获得免费的静态分析。
2.  [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)：Java 届最权威的代码风格指南。
