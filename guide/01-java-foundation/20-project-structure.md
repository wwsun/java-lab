# 20 - Java 项目结构与包管理规则

## 核心心智映射 (Core Mental Mapping)

Java 项目目录结构看似繁琐，实则极具规范性。这种“强约束”能让任何一个 Java 开发者在接手新项目时，三秒钟内找到代码位置。

| 区域 | Node.js / TypeScript | Java (Maven 标准) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **源码根目录** | `src/` | `src/main/java/` | 极其固定的分层路径 |
| **配置文件** | `.env` / `config.json` | `src/main/resources/` | 与代码物理隔离的资源区 |
| **测试代码** | `tests/` / `__tests__/` | `src/test/java/` | 测试代码与生产代码镜像对称 |
| **构建产物** | `dist/` / `build/` | `target/` | 编译生成的字节码存放处 |
| **模块路径** | 相对/绝对路径引用 | 唯一的“全限定包名” | 物理路径 = 逻辑包名 |

---

## 概念解释 (Conceptual Explanation)

### 1. 标准目录布局 (Standard Directory Layout)
-   **`src/main/java`**: 存放所有的 Java 源代码。
-   **`src/main/resources`**: 存放配置文件（如 `application.yml`）、SQL 脚本、静态模板。
-   **`target/`**: **不要将其加入 Git**。它由 Maven 自动生成，存放编译后的 `.class` 文件。

### 2. 包 (Package) 的物理约束
在 Java 中，**包名必须与文件夹路径完全一致**。
-   如果你声明了 `package com.javalabs.service;`
-   那么该文件**必须**存放在 `src/main/java/com/javalabs/service/` 目录下。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 包声明 (Package Declaration)
每个 Java 文件的第一行非注释代码必须是包声明：
```java
package com.javalabs.controller;
```

### 启动类 (Application Entry)
Spring Boot 项目通常有一个带有 `@SpringBootApplication` 注解的启动类。在 IDEA 中，它就是你的“点火开关”。

---

## 典型用法 (Typical Usage)

### Spring Boot 典型分层结构
1.  **`controller`**: 处理 HTTP 请求（对标 Route/Controller）。
2.  **`service`**: 核心业务逻辑（事务、计算）。
3.  **`mapper` / `repository`**: 数据库交互。
4.  **`entity` / `model`**: 数据库表映射对象。
5.  **`dto`**: 对前端暴露的传输对象。

### 开发中的“点火”流程
1.  找到 `XxxApplication.java`。
2.  点击右上角或左侧的 **Debug (小虫子)** 图标启动。
3.  观察控制台输出：`Started XxxApplication in ... seconds` 且 `port(s): 8080` 即代表启动成功。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察项目中的 `pom.xml` 配置：
```xml
<groupId>com.javalabs</groupId>
<artifactId>java-labs</artifactId>
<version>0.0.1-SNAPSHOT</version>
```
这定义了你的项目“坐标”。当你创建包时，通常会以 `groupId.artifactId` 开头（如 `com.javalabs.service`），这形成了一个天然的命名空间，防止与其他库发生冲突。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

在请求 Agent 生成代码前，先告知它你的项目规范：

> **最佳实践 Prompt**:
> "我正在基于 Maven 标准结构开发 Spring Boot 应用。主包名是 `com.javalabs`。
> 请帮我生成一个处理用户注册的逻辑：
> 1. 生成一个 `UserEntity` 放在 `entity` 包。
> 2. 生成一个 `UserService` 接口和实现类。
> 3. 确保 package 声明符合 `src/main/java` 的物理路径约定。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Maven: Introduction to the Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) - 深入了解背后的设计哲学。
2. [Spring Boot: Structuring Your Code](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.structuring-your-code) - 官方对分层架构的建议。
