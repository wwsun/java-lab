# Guide Index

`guide/` 已完成按学习阶段重构，不再继续使用单一平铺目录 + 全局连续编号的方式。

## 新的整理规则

新的学习资料目录采用两层结构：

1. 第一层按学习阶段分目录
2. 第二层在各阶段目录内重新编号

当前规划如下：

- `01-java-foundation/`
  Java 基础语法、工具链、集合、异常、时间 API、并发入门
- `02-spring-boot-and-persistence/`
  Spring Boot、REST、数据库、MyBatis-Plus、测试
- `03-web-security-and-infra/`
  Security、Redis、缓存、日志、Docker、Nginx、部署
- `04-project-delivery/`
  业务实战、需求建模、DDL、项目交付

## 当前状态

- Phase 1 已完成：`01-java-foundation/`
- Phase 2 已完成：`02-spring-boot-and-persistence/`
- Phase 3 已完成：`03-web-security-and-infra/`
- Phase 4 已完成：`04-project-delivery/`
- 根目录补充专题已完成归并，目前 `guide/` 根目录只保留总索引文件

## Phase 1 迁移映射

| 旧路径 | 新路径 |
| --- | --- |
| `guide/00-java-basic.md` | `guide/01-java-foundation/01-java-101.md` |
| `guide/07-intellij-productivity-tips.md` | `guide/01-java-foundation/02-intellij-productivity.md` |
| `guide/01-maven-vs-npm-guide.md` | `guide/01-java-foundation/03-maven-vs-npm.md` |
| `guide/02-maven-essentials.md` | `guide/01-java-foundation/04-maven-essentials.md` |
| `guide/12-java-var-usage.md` | `guide/01-java-foundation/05-var-usage.md` |
| `guide/04-linting-and-quality.md` | `guide/01-java-foundation/06-code-quality-and-linting.md` |
| `guide/03-stream-api-mapping.md` | `guide/01-java-foundation/07-stream-api-mapping.md` |
| `guide/06-intellij-debugging-tips.md` | `guide/01-java-foundation/08-intellij-debugging.md` |
| `guide/05-generics-and-erasure.md` | `guide/01-java-foundation/09-generics-and-erasure.md` |
| `guide/08-collections-mapping.md` | `guide/01-java-foundation/10-collections-mapping.md` |
| `guide/14-java-lambda-essentials.md` | `guide/01-java-foundation/11-lambda-essentials.md` |
| `guide/10-exception-handling.md` | `guide/01-java-foundation/12-exception-handling.md` |
| `guide/11-java-time-api.md` | `guide/01-java-foundation/13-java-time-api.md` |
| `guide/13-concurrency-models.md` | `guide/01-java-foundation/14-concurrency-models.md` |
| `guide/15-runnable-vs-callable.md` | `guide/01-java-foundation/15-runnable-vs-callable.md` |
| `guide/16-concurrent-hashmap-guide.md` | `guide/01-java-foundation/16-concurrent-hashmap.md` |
| `guide/17-threadpool-tuning-guide.md` | `guide/01-java-foundation/17-threadpool-tuning.md` |
| `guide/18-multithreading-debugging.md` | `guide/01-java-foundation/18-multithreading-debugging.md` |
| `guide/19-virtual-threads-intro.md` | `guide/01-java-foundation/19-virtual-threads-intro.md` |
| `guide/09-java-project-structure.md` | `guide/01-java-foundation/20-project-structure.md` |

## 当前整理结果

本轮整理完成了两件事：

1. 主线学习资料全部按阶段落位
2. 原先散落在根目录的 `Lombok`、`DTO`、`事务`、`MySQL/Redis 配置`、`PostgreSQL 选型` 等补充专题，已并入第 2 周目录

## 下一步

后续如继续迭代，建议优先处理：

1. 统一第 3 周部分 frontmatter 文档的标题风格
2. 为每个阶段补一页“阶段总览图”
3. 按 meetingroom 项目推进第 4 周交付型资料
