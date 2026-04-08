# 02 - Maven 核心实战技巧

## 核心心智映射 (Core Mental Mapping)

如果你把 Maven 对标为 npm/yarn，其实你不需要背诵厚厚的手册。在日常开发中，掌握以下映射关系足以应对 95% 的场景：

| 场景 | Node.js / NPM | Java / Maven | 心智映射 |
| :--- | :--- | :--- | :--- |
| **包标识** | `react@18.0.0` | GAV (GroupId:ArtifactId:Version) | 三维坐标精确定位 |
| **国内加速** | `npm config set registry...` | `settings.xml` (Mirror) | 全局配置加速下载 |
| **依赖分析** | `npm ls` | `mvn dependency:tree` | 解决 jar 包冲突的利器 |
| **排除依赖** | `pnpm.patchedDependencies` | `<exclusions>` | 剔除有冲突的传递依赖 |

---

## 概念解释 (Conceptual Explanation)

### 1. 传递性依赖 (Transitive Dependency)
当你引入库 A，而库 A 依赖库 B 时，库 B 会被自动下载到你的项目中。这在 Java 中可能导致 **“Jar 包冲突”**（即两个不同的库依赖了不同版本的库 B）。

### 2. 最短路径优先原则
Maven 遇到版本冲突时，会优先选择在依赖路径中最短的那个版本。如果路径长度一致，则优先选先声明的。

### 3. `<dependencyManagement>`
用于在父项目中“锁定”版本号。子项目引用这些依赖时无需写版本号，确保全项目版本统一。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 依赖树查看
```bash
mvn dependency:tree
```
这是解决 `ClassNotFoundException` 或 `NoSuchMethodError` 的第一生产力。

### 排除特定依赖
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>library-a</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## 典型用法 (Typical Usage)

### 配置阿里云镜像
在 `~/.m2/settings.xml` 中配置，加速下载：
```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>central</mirrorOf>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察上述的 `<exclusions>` 片段：
在集成某些旧库时，它们可能会自带一些过时的日志框架（如 `log4j`）。为了防止与现代的 `logback` 冲突，我们使用 `<exclusion>` 标签显式地将其从依赖链中剔除。这类似于在 Node.js 环境中，通过 `resolutions` (Yarn) 或 `overrides` (NPM) 来强制锁定某个深层依赖的版本。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当遇到由于 Jar 包版本不一致导致的报错时，直接把错误堆栈发给 Agent：

> **最佳实践 Prompt**:
> "我遇到了这个报错：`[贴入报错信息]`。
> 1. 请帮我分析是否由于依赖冲突导致。
> 2. 请运行 `mvn dependency:tree` 并帮我检查冲突的路径。
> 3. 请告诉我应该在哪个依赖中添加 `<exclusion>` 来修复它。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Maven: Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html) - 深入理解 Maven 是如何解析依赖的。
2. [阿里云云效 Maven](https://developer.aliyun.com/mvn/guide) - 国内最权威的镜像库及配置指南。
