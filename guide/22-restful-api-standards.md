# 22-SpringBoot RESTful API 设计与规范指引

本指南总结了 Java 后端开发中关于 REST 接口设计的核心工业规范，结合本项目中的 `EmployeeController` 实践。

---

## 1. 核心设计原则：资源导向 (ROA)

REST 的本质是**操作资源**。在 Spring Boot 中，我们通过特定的注解来实现这种映射。

### 📍 URL 命名规范
- **使用名词复数**：`/api/employees` (✅) 而非 `/api/getEmployees` (❌)。
- **全小写/连字符**：推荐使用 `/api/it-employees` 而非蛇形命名。

---

## 2. HTTP 谓词的语义映射

我们通过 `EmployeeController` 验证了以下标准映射：

| 方法 | 语义 | 幂等性 | 本项目实现 |
| :--- | :--- | :--- | :--- |
| **GET** | 获取资源 | 是 | `getAllEmployees()`, `getEmployeeById()` |
| **POST** | 创建新资源 | 否 | `createEmployee()` |
| **PUT** | 更新/替换资源 | 是 | `updateEmployee()` |
| **DELETE** | 删除资源 | 是 | `deleteEmployee()` |

---

## 3. HTTP 状态码的设置方式：三种姿势

在 Spring Boot 中，设置状态码主要有三种方式，按灵活度排序如下：

### 3.1 姿势一：利用 `ResponseEntity` (推荐)
这是最灵活、最显式的方式，允许在返回数据的同时动态指定状态码和 Header。

```java
// 示例：创建成功返回 201
@PostMapping
public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
    Employee saved = employeeService.createEmployee(employee);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

// 示例：查询成功返回 200 (默认)
return ResponseEntity.ok(data);
```

### 3.2 姿势二：使用 `@ResponseStatus` 注解
适用于逻辑简单、意图固定的场景。直接打在方法上，Spring 会自动包装。

```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT) // 👈 强制返回 204
public void deleteEmployee(@PathVariable String id) {
    employeeService.deleteEmployee(id);
}
```

### 3.3 姿势三：异常触发
正如第 4 章所示，当抛出特定异常时，由异常处理器统一重定向到对应的状态码。

---

## 4. 全局异常处理实战：从 AOP 到 JSON

在 Node.js 中，我们习惯通过 `app.use((err, req, res, next) => ...)` 来捕捉错误。Spring Boot 则使用了 **`@RestControllerAdvice`**。

### 📍 设置指南：手把手教你配置

#### 第一步：定义自定义异常
创建一个继承自 `RuntimeException` 的类，用于业务逻辑报错。
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
```

#### 第二步：编写全局处理器
创建一个类并加上 `@RestControllerAdvice` 注解。

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 针对特定异常返回 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
```

### ⚡ 运行指引：如何演示异常拦截？
1. **启动应用**。
2. **故意制造错误**：请求一个不存在的资源，如 `curl -i http://localhost:8080/api/employees/abc`。
3. **观察结果**：
   - 虽然 Service 层抛出的是 Java 的 `throw new Exception()`。
   - 但你收到的却是规范的 **JSON** 响应，状态码精准为 **404**。
   - 这证明了异常在到达用户前，被“大管家”拦截并进行了“精装修”。

---

> [!IMPORTANT]
> **最佳实践总结**：
> 始终优先返回 `ResponseEntity<T>` 或通过 `ExceptionHandler` 统一转换逻辑。这能确保你的 API 无论在成功还是失败时，返回的结构都是可预测的，这对前端开发者极其友好。

**关联代码**：
- [EmployeeController.java](../src/main/java/com/javalabs/controller/EmployeeController.java)
- [GlobalExceptionHandler.java](../src/main/java/com/javalabs/exception/GlobalExceptionHandler.java)
