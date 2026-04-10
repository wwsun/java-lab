# 03 - Spring Boot RESTful API 设计与规范指引

## 核心心智映射 (Core Mental Mapping)

REST 的本质是**操作资源**。在 Spring Boot 中，我们通过特定的注解将 HTTP 方法精确映射到业务资源上。

| 场景 | Node.js (Express / Nest) | Java (Spring Boot) | 心智映射 |
| :--- | :--- | :--- | :--- |
| **获取资源** | `router.get()` / `@Get()` | **`@GetMapping`** | 幂等读取 |
| **创建资源** | `router.post()` / `@Post()` | **`@PostMapping`** | 非幂等写入 (201 Created) |
| **更新资源** | `router.put()` / `@Put()` | **`@PutMapping`** | 整体覆盖 (幂等) |
| **删除资源** | `router.delete()` / `@Delete()` | **`@DeleteMapping`** | 物理/逻辑删除 (204 No Content) |
| **请求参数** | `req.params` / `@Param()` | **`@PathVariable`** | 路径中的资源 ID |
| **查询参数** | `req.query` / `@Query()` | **`@RequestParam`** | 过滤、分页参数 |

---

## 概念解释 (Conceptual Explanation)

### 1. 资源导向 (ROA)
-   **URL 命名**: 应该是名词复数。例如 `/api/users` 而不是 `/api/getUser`。
-   **无状态性**: 每个请求都应该包含处理该请求所需的全部信息，服务器不保存会话上下文（对标 JWT 模式）。

### 2. 幂等性 (Idempotency)
-   无论执行多少次，结果都相同的方法（如 GET, PUT, DELETE）。这对分布式系统中的重试机制至关重要。

---

## 关键语法和 API 介绍 (Key Syntax and API Introduction)

### ResponseEntity：状态码的指挥棒
在 Java 中，如果你想返回非 200 的状态码（如 201 或 404），必须使用 `ResponseEntity`。
```java
@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) {
    User savedUser = userService.save(user);
    // 返回 201 Created 状态码
    return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
}
```

---

## 典型用法 (Typical Usage)

### 常用状态码准则
-   **200 OK**: 常规查询成功。
-   **201 Created**: POST 创建资源成功。
-   **204 No Content**: DELETE 成功，且无需返回内容。
-   **400 Bad Request**: 参数非法（如校验失败）。
-   **404 Not Found**: 资源不存在。

---

## 配套的代码示例解读 (Code Example Walkthrough)

观察 `EmployeeController.java` 中的设计：
```java
@GetMapping("/{id}")
public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
    Employee employee = employeeService.getById(id);
    return ResponseEntity.ok(employee); // 默认为 200
}
```
通过 `@PathVariable`，我们将 URL 中的 ID 直接映射到了方法参数上。配合 `ResponseEntity` 的链式调用，代码兼具了可读性与 HTTP 规范性。

---

## AI 辅助开发实战建议 (AI-assisted Development Suggestions)

设计 RESTful 接口时，层级关系容易混乱。

> **最佳实践 Prompt**:
> "我需要为一个『博客系统』设计 API。一个文章（Post）下有多个评论（Comment）。
> 1. 请帮我设计符合 REST 规范的 URL 路径，以获取特定文章下的所有评论。
> 2. 请生成对应的 Spring Boot Controller 骨架，并使用 `ResponseEntity` 返回规范的状态码。
> 3. 请说明在更新评论时，应该选择 `PUT` 还是 `PATCH`，并提供代码示例。"

---

## 2-3 条扩展阅读 (Extended Readings)

1. [Microsoft REST API Guidelines](https://github.com/microsoft/api-guidelines) - 工业级参考标准。
2. [Baeldung: ResponseEntity in Spring](https://www.baeldung.com/spring-response-entity) - 详细讲解状态码与 Header 的控制技巧。
