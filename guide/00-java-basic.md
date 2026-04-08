# 00 - Java 101：给 Node.js 开发者的入门指南

## 核心心智映射 (Core Mental Mapping)

如果你熟悉 Node.js，Java 对你来说并不是外语，而是一门“语法更严谨、架构更厚重”的方言。以下是你的心智模型转换地图：

| 领域 | Node.js / TypeScript | Java 生态 | 心智映射 |
| :--- | :--- | :--- | :--- |
| **运行时** | V8 Engine | JVM (Java Virtual Machine) | 都是字节码/二进制转换中心 |
| **包管理** | NPM / PNPM | Maven / Gradle | 负责依赖下载与版本控制 |
| **项目元数据** | `package.json` | `pom.xml` (Maven) | 核心配置与坐标声明 |
| **编译/转译** | `tsc` (TS -> JS) | `javac` (Java -> Class) | 将源码转换为可执行格式 |
| **函数组织** | 全局函数、箭头函数 | 必须写在 `class` 内 | Java 中“一切皆对象” |

---

## 概念解释 (Conceptual Explanation)

### JVM, JRE 与 JDK 的关系
1.  **JVM (Java Virtual Machine)**: 灵魂核心。负责运行字节码，掌管内存管理与垃圾回收（对标 V8）。
2.  **JRE (Java Runtime Environment)**: 运行环境。包含 JVM 和基础类库（对标 Node 运行时）。
3.  **JDK (Java Development Kit)**: 开发工具包。包含 JRE + 编译器 + 调试工具（对标 Node + NPM + SDK）。

### 字节码 (Bytecode)
Java 源码 (`.java`) 会先编译成字节码 (`.class`)。这样做的好处是 **“一次编写，到处运行”**：只要机器上有 JVM，就能跑同一套字节码，无需关心底层 OS。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### 入口点 (Entry Point)
Java 程序的启动必须从一个固定的“点火开关”开始：

```java
public class HelloWorld {
    // 固定的方法签名：公有的、静态的、无返回值的 main 方法
    public static void main(String[] args) {
        // 标准输出打印
        System.out.println("Hello Java!");
    }
}
```

### 类型系统
- **基本类型 (Primitives)**: `int`, `long`, `boolean`, `double` 等。直接存值在栈中，性能极高。
- **引用类型 (References)**: `String`, `List`, `User` 等。存的是对象的内存地址，存放在堆中。

---

## 典型用法 (Typical Usage)

### 基础类定义
在 Java 中，一个文件通常对应一个 `public class`，且文件名必须与类名一致。

```java
public class UserProfile {
    // 私有字段，对标 TS 的 private
    private String username;
    
    // 构造函数
    public UserProfile(String username) {
        this.username = username;
    }

    // 实例方法
    public void printWelcome() {
        System.out.println("Welcome, " + username);
    }
}
```

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `public static void main(String[] args)`：
1.  **`public`**: 让 JVM 可以在类外部调用这个方法。
2.  **`static`**: 意味着 JVM 不需要实例化这个类就能直接调用该方法（类似于 Node 中的静态导出）。
3.  **`void`**: 声明没有返回值。
4.  **`String[] args`**: 命令行参数，对标 Node 中的 `process.argv`。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

作为前端老鸟，不要强记所有 Java 语法，利用 Agent 的映射能力：

> **最佳实践 Prompt**:
> "我正尝试在 Java 中实现一个功能。在 TypeScript 中我会这样写：`[贴入 TS 代码]`。
> 请帮我基于 Java 21 的语法惯例（Idioms）重写它，并说明 Java 中对应的面向对象组织方式。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Oracle: Learn Java Tutorials](https://dev.java/learn/) - 官方入门教程，最权威的语料库。
2. [Baeldung: Java Basics](https://www.baeldung.com/category/java/tag/java-core/) - 实战派首选，含有大量对比示例。
