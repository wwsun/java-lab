# 04 - Java 代码规范与静态检查工具指南

## 核心心智映射 (Core Mental Mapping)

作为资深的 Node.js/TypeScript 开发者，你可能已经习惯了 `eslint` + `prettier` 的“一站式”体验。在 Java 生态中，代码质量保障由多个层级的工具协同完成。

| 功能描述 | Node.js / TypeScript | Java 工具栈 | 核心差异点 |
| :--- | :--- | :--- | :--- |
| **代码格式化** | `prettier` | **Spotless / GJF** | Java 对换行和括号位置有更严格的工业共识 |
| **静态分析 (Lint)** | `eslint` | **SonarLint / Checkstyle** | Checkstyle 偏风格，SonarLint 偏逻辑漏洞 |
| **缺陷检测** | `eslint` (logic) | **PMD / SpotBugs** | 擅长抓取空指针、资源未关闭等深度 Bug |
| **IDE 助手** | ESLint Plugin | **SonarLint (必装)** | 实时反馈，体验最接近前端开发 |

---

## 概念解释 (Conceptual Explanation)

### 1. 静态代码分析 (Static Code Analysis)
在代码运行前，通过工具扫描 AST（抽象语法树）来识别潜在的编程错误、安全弱点或偏离规范的写法。

### 2. 检查风格 (Style) vs 检查缺陷 (Defect)
- **Checkstyle**: 解决的是“空格还是 Tab”、“括号放哪”的问题（类似 Prettier）。
- **PMD / SpotBugs**: 解决的是“这里可能报空指针”、“数据库连接没关”等逻辑问题（类似 ESLint 的逻辑规则）。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### Maven 插件集成
在 `pom.xml` 中配置 Checkstyle，可以在编译打包时自动强制检查：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
        <failOnViolation>true</failOnViolation> <!-- 校验失败则阻止构建 -->
    </configuration>
</plugin>
```

---

## 典型用法 (Typical Usage)

### 🚀 第一步策略：SonarLint 实时驱动
对于初学者，不要折腾复杂的 XML 配置，直接在 IDE 层面解决问题：
1.  **安装插件**: 在 IntelliJ IDEA 插件市场搜索 **SonarLint** 并重启。
2.  **实时修复**: 代码中出现的黄色/蓝色波浪线就是 SonarLint 提示。
3.  **学习最佳实践**: 点击提示会展示“坏代码”对比“好代码”的示例，这是学习 Java 惯用语（Idioms）的最快路径。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察以下代码，SonarLint 会给出对应的警告：

```java
public void process(String input) {
    if (input == null) {
        return;
    }
    // 警告：不要使用魔法值 "admin"，请定义常量
    if (input.equals("admin")) { 
        System.out.println("Welcome");
    }
}
```
**SonarLint 建议**: 应改为 `if ("admin".equals(input))`。
**原因**: 这样可以避免当 `input` 为 `null` 时触发空指针异常（虽然上面已经 check 了，但这是 Java 编写防御性代码的通用直觉）。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

当你面对一坨旧 Java 代码感到恐惧时：

> **最佳实践 Prompt**:
> "请作为高级 Java 架构师，根据阿里巴巴 P3C 规范或 Google Style Guide 对这段代码进行 Review：`[贴入代码]`。
> 1. 请指出其中的逻辑缺陷（如空指针风险）。
> 2. 请指出不符合 Java 21 现代语法的写法（如可以使用 Record 替代 POJO）。
> 3. 请直接给出重构后的代码，并要求符合 SonarLint 的零警告标准。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [SonarLint for IntelliJ](https://www.sonarsource.com/products/sonarlint/features/jetbrains/) - 开发必备的指南。
2. [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) - 全球开发者公认的圣经。
3. [Alibaba Java P3C](https://github.com/alibaba/p3c) - 国内大厂面试与开发的基准。
