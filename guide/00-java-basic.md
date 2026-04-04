# 00-Java 101：给 Node.js 开发者的“从 0 到 1”入门指南

欢迎来到 Java 的世界！作为资深的 Node.js/TypeScript 开发者，您已经具备了优秀的编程心智。Java 对您而言并不是外语，而是一门“语法更严谨、架构更厚重”的方言。

## 1. 概念对标：环境底座

在 Node.js 中，你只需一个 `node` 命令。在 Java 中，我们需要区分以下三个概念：

| 缩写 | 全称 | Node.js 对标 | 核心定位 |
| :--- | :--- | :--- | :--- |
| **JDK** | Java Development Kit | **Node.js + NPM + SDK** | **开发必选**。包含了编译器 (`javac`) 和全套开发工具。 |
| **JRE** | Java Runtime Environment | **Node Runtime** | **运行必选**。仅包含跑代码所需的环境。 (JDK 11+ 已将其整合) |
| **JVM** | Java Virtual Machine | **V8 引擎** | **灵魂核心**。负责跨平台运行字节码，掌管内存管理与垃圾回收。 |

---

## 2. 编译链路：为什么不能直接跑路径？

在 Node.js 中，运行代码通常是：`node app.js`。
在 Java 中，代码需要经历一段 **“工业化加工”**：

1.  **源码 (.java)**：你写的代码。
2.  **编译器 (javac)**：将源码“翻译”成 **字节码 (.class)**。
    - *类比*：这就像 TypeScript 编译成 JavaScript。
3.  **运行时 (JVM)**：解释并执行字节码。
    - *优势*：**Write Once, Run Anywhere**。只要有 JVM，同样的字节码可以在 Linux、Windows、甚至智能冰箱上运行。

---

## 3. 面向对象：第一条铁律

在 JS 中，你可以直接写全局函数。**在 Java 中，一切皆为对象，代码必须写在 `class` 里。**

### 入口函数 (Entry Point)
每一个 Java 的可执行程序都必须有一个“点火开关”：

```java
public class HelloJava {
    // 固定的签名：公有的、静态的、无返回值的入口方法
    public static void main(String[] args) {
        System.out.println("Hello Java!");
    }
}
```

---

## 4. 类型系统：不仅仅是标记

- **JS/TS**：类型更多是为了开发时的静态检查。
- **Java**：类型是 **底层内存分配** 的依据。
    - **基本类型 (Primitive)**：如 `int`, `long`, `boolean`。响应极快，存在栈里。
    - **引用类型 (Reference)**：如 `String`, `List`, `User`。存在堆里，通过指针引用。

---

## 5. 项目骨架：Maven 101

在前端，你有 `package.json`。在 Java 中，我们常用的构建工具是 **Maven**：

- **`pom.xml`**：对标 `package.json`。定义了项目坐标、插件版本和依赖仓库。
- **目录规范**：
    - `src/main/java`：放业务代码（对标 `src/`）。
    - `src/test/java`：放单元测试（对标 `tests/`）。

---

## 6. 如何高效学习此指南？

本系列文档已按从易到难的顺序编号：
1.  **101 入门**：即本指南 [00-java-basic.md](./00-java-basic.md)。
2.  **工具链与规范**：参见 [01-maven-vs-npm-guide.md](./01-maven-vs-npm-guide.md) 和 [09-java-project-structure.md](./09-java-project-structure.md)。
3.  **语法进阶**：参见 [03-stream-api-mapping.md](./03-stream-api-mapping.md) 与 [14-java-lambda-essentials.md](./14-java-lambda-essentials.md)。
4.  **并发内功**：也是本项目的核心特色，从 [13-concurrency-models.md](./13-concurrency-models.md) 开启。

---
> [!TIP]
> **指挥 AI 的第一步：** 不要试图强记所有语法，只需理解 Java 的 **“显式声明”** 和 **“面向对象”** 哲学。遇到问题，请对 Agent 提问：“这个逻辑在 Java 的面向对象体系下该如何组织？”。

**参考资料**：
- [Oracle Java Documentation](https://docs.oracle.com/en/java/)
- [Java 17 官方新特性解读](https://www.oracle.com/java/technologies/javase/17-relnotes.html)
