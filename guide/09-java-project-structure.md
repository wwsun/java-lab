# 09-Java 项目结构与包管理规则 (Project Structure & Packaging)

Java 项目目录结构看似繁琐，实则极具规范性。掌握了这一套 **Maven/Gradle 标准布局**，你就掌握了全球 Java 后端开发的通用语言。

## 1. 标准目录布局 (Standard Directory Layout)

```text
project-root/ (根目录)
├── pom.xml                 # 依赖与构建配置 (类比 package.json)
├── target/                 # 编译输出目录 (编译产生的 .class 文件，不入 git，类比 dist/build)
└── src/                    # 源码根目录
    ├── main/               # 生产环境代码
    │   ├── java/           # 所有的 Java 源代码
    │   └── resources/      # 所有的非 Java 资源 (Config, SQL, 静态模板)
    └── test/               # 测试环境代码
        ├── java/           # 所有的单元测试/集成测试代码
        └── resources/      # 测试专用的配置文件
```

## 2. 包 (Package) 的物理约束

在 Java 中，包名的声明必须与该文件所在的文件夹深度完全一致。

**示例**：
- 文件内容：`package com.javalabs.service;`
- 存放位置：`src/main/java/com/javalabs/service/MyService.java`

> **关键差异**：在 Node.js 中，文件移动了位置只需修改 `import` 路径；在 Java 中，如果你移动了文件位置，**包声明 (package declaration)** 必须同步修改。

## 3. 下一阶段：Spring Boot 典型工程分层

在实际 Web 开发中，我们通常会在主包名下进行如下分层：

1.  **`controller`**：负责 HTTP 请求的接收与参数校验（对标 Express/NestJS 的 Router/Controller）。
2.  **`service`**：核心业务逻辑处理（计算、事务整合）。
3.  **`repository` / `mapper`**：专注数据库交互（CRUD）。
4.  **`entity` / `model`**：数据库表映射的对象。
5.  **`dto` (Data Transfer Object)**：对前端暴露的视图对象。
6.  **`config`**：存放项目配置类（如分页配置、跨域配置）。

## 4. Node.js vs Java 结构对比

| 特性 | Node.js (常见习惯) | Java (强制约定) |
| --- | --- | --- |
| **源码目录** | `src/` 或根目录平铺 | `src/main/java/` |
| **测试目录** | `tests/` 或与源码混放 | `src/test/java/` |
| **配置文件** | `.env`, `config.json` | `src/main/resources/application.yml` |
| **构建产物** | `dist/`, `build/` | `target/` |
| **模块引用** | 相对路径或 Alias | 唯一的全限定包名 (Fully Qualified Name) |

---
**提示**：在 IDEA 中，你可以右键点击 `java` 文件夹选择 **New -> Package**，输入 `com.javalabs.util`，IDEA 会自动为你创建多层级的物理文件夹。
