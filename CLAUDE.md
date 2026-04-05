# Java Labs

一个从 Node.js/TypeScript 转型 Java 后端开发的学习实战工程。

## 常用命令

- `./mvnw spring-boot:run` - 启动应用
- `./mvnw clean install` - 构建项目
- `./mvnw test` - 运行所有测试
- `./mvnw test -Dtest=ClassName` - 运行指定测试类

## 代码规范

- **Alibaba P3C**: 必须严格遵循 [p3c_guidelines.md](./agent_docs/p3c_guidelines.md)
- **Java 版本**: JDK 21 (JDK 17 兼容)
- **技术栈**: Spring Boot 3.3.6, MyBatis-Plus, Lombok, MySQL/H2
- **Lombok**: 实体类强制使用 `@Data`/`@Builder`，禁止手写 Getter/Setter
- **注释**: 逻辑说明使用简体中文，标识符使用英文

## 工作流程

- 每次会话开始时，必读 `AGENTS.md` 和 `LEARNING-ROADMAP.md` 以确定进度
- 任何业务逻辑修改必须伴随对应的 JUnit 5 测试用例
- 优先生成现代化 Java API 代码（如 `java.time.*`, `Stream API`）

## 详细指南

- 学习路径: [LEARNING-ROADMAP.md](./LEARNING-ROADMAP.md)
- 技术映射: [NODE-JAVA-MAPPING.md](./NODE-JAVA-MAPPING.md)
- 详细规约: [agent_docs/p3c_guidelines.md](./agent_docs/p3c_guidelines.md)
