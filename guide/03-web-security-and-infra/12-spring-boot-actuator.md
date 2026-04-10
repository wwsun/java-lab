---
title: "应用监控端点：Spring Boot Actuator"
description: "给你的 Java 应用装上监控雷达，实时查看运行状态、JVM 指标与配置信息"
---

# 12 - 应用监控端点：Spring Boot Actuator

## 核心心智映射

在 Node.js 生产环境下，为了知道服务是否还“活着”，你可能会自己写一个 `/health` 接口返回 `{ status: 'ok' }`。为了监控 CPU 和内存，你可能会用 `pm2 monit` 或者安装 `prom-client` 对接 Prometheus。

在 Spring Boot 中，你完全不需要自己写这些。**Actuator** 是官方提供的“监视器”，只要引入一个依赖，它就会自动为你暴露出一系列生产级别的端点（Endpoints）。

| 功能 | 类比 (Node.js) | Actuator 端点 |
| :--- | :--- | :--- |
| **健康检查** | 手写的 `/health` 路由 | `/actuator/health` |
| **JVM 状态** | `process.memoryUsage()` | `/actuator/metrics/jvm.memory.used` |
| **配置查看** | `console.log(process.env)` | `/actuator/env` |
| **Bean 列表** | (无直接对应，类似容器对象列表) | `/actuator/beans` |
| **线程堆栈** | `core.createReport()` | `/actuator/threaddump` |

---

## 快速接入

### 1. 引入依赖 (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. 核心配置 (`application.yml`)

默认情况下，出于安全考虑，Actuator 只会暴露 `/health`。如果你想查看更多信息（比如在开发环境），可以如下配置：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*" # 暴露所有端点（生产环境慎用！）
  endpoint:
    health:
      show-details: always # 在健康检查中显示详细信息（如磁盘空间、数据库连接）
```

---

## 常用端点解读

1. **`/actuator/health`**:
   - 如果数据库连接断了，或者磁盘空间不足，它的状态会从 `UP` 变成 `DOWN`。这通常被 K8s 或 Nginx 用来判断是否要踢出该节点。
2. **`/actuator/info`**:
   - 展示应用的自定义信息。你可以在 `yml` 中通过 `info.app.name=meetingroom` 来定义。
3. **`/actuator/metrics`**:
   - 极其强大的数据源。它可以告诉你 HTTP 请求的平均耗时、JVM 垃圾回收次数、线程池活跃度等。通常配合 **Micrometer** 和 **Prometheus** 使用。
4. **`/actuator/loggers`**:
   - **黑科技**：你可以通过这个端点在**程序不重启**的情况下，实时修改某个类的日志级别。比如生产环境突然出 Bug 查不出来，你可以发一个 POST 请求把该类的日志从 `INFO` 改成 `DEBUG`，排查完再改回去。

---

## 最佳实践与反模式

1. **[重要] 安全防护**：
   - **反模式**：在公网环境下直接暴露 `/actuator/*` 且没有任何权限验证。这会导致你的环境变量（包含数据库密码、密钥）被一览无余。
   - **对策**：
     - 利用 Spring Security 对 `/actuator/**` 路径进行角色限制（如只有 `ADMIN` 能看）。
     - 或者将 Actuator 端口与业务端口隔离（`management.server.port=8081`），内网环境下开放 8081，外网开放 8080。
2. **配合图形化界面**：
   - 虽然 Actuator 返回的是 JSON，但大家通常会配合 **Spring Boot Admin**。它是一个可视化看板，能让你直接在网页上点点点来查看所有微服务的状态、柱状图，甚至直接在线看日志文件。

---

## 扩展阅读
1. [Spring Boot Actuator 官方参考指南](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
2. [Micrometer 指标采集](https://micrometer.io/) (对标 Node.js 的 Prometheus 指标库)
3. [Spring Boot Admin 项目主页](https://github.com/codecentric/spring-boot-admin)
