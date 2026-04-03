# 10-Java 异常处理： Checked vs Unchecked Exception

在 Java 中，异常处理是一门艺术。它通过一套严谨的语法，强制开发者去思考：“如果这里出错了，系统该怎么办？”

## 1. 核心分类：不再只有一种 Error

| 类别 | 所属基类 | 核心哲学 |
| --- | --- | --- |
| **受检异常 (Checked Exception)** | `Exception` | **“大概率会发生，你必须处理”**。如：网络断了、硬盘满了。编译器会强制你处理。 |
| **运行时异常 (Unchecked Exception)** | **`RuntimeException`** | **“这是你的代码写得烂 (Bug)”**。如：空指针、除以 0、索引越界。编译器不强制你。 |
| **错误 (Error)** | `Error` | **“别挣扎了，重启吧”**。如：内存溢出 (OOM)。应用层不建议捕获。 |

## 2. 语法结构对比 (Node.js vs Java)

### Node.js (运行时捕捉)
```javascript
try {
  doSomething();
} catch (e) {
  console.error(e.message);
} finally {
  cleanUp();
}
```

### Java (多了声明契约)
```java
// 如果是 Checked Exception，必须在方法名后用 throws 声明“甩锅”
public void readFile() throws IOException {
    try {
        // ...业务逻辑
    } catch (IOException e) {
        // ...处理逻辑
        throw e; // 继续向上抛出
    } finally {
        // ...无论如何都会执行 (由于资源释放)
    }
}
```

## 3. 一个重要的现代特性：Try-with-resources

在 Java 7 以后，如果你处理的是文件、网络连接等需要关闭的资源，千万别在 `finally` 里手动 `close()`。使用自动资源管理：

```java
try (var file = new FileInputStream("test.txt")) {
    // 处理文件逻辑
} catch (IOException e) {
    // 自动调用 file.close()
}
```

## 4. 最佳实践 (面向指挥 AI 的开发者)

### (1) 不要直接捕获 `Exception` (Specific Catch)
这就像在 JS 中写 `catch(e) { }` 一样，会吞掉所有错误（包括编译报错、空指针等）。
*   **❌ Bad**：`catch (Exception e) { ... }` 过于笼统。
*   **✅ Good**：针对性捕获 `FileNotFoundException`, `IOException`, `SQLException`。这样你能为不同错误提供不同的策略（如重试或提示）。

### (2) 自定义业务异常 (Custom Business Exception)
为特定的业务逻辑定义自己的异常类（如 `UserNotFoundException`），这有助于 AI 帮你自动梳理异常流，也让代码自带“文档”属性。
```java
// 示例：定义一个业务异常
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("ID 为 " + userId + " 的用户不存在");
    }
}
```

### (3) 早抛出，晚捕获 (Throw Early, Catch Late)
*   **早抛出**：在最底层（如 Service）发现输入不合法或业务逻辑不通时，立即抛出具体异常，防止错误扩散。
*   **晚捕获**：底层的 `Service` 不要随便吃掉异常。它应该一直向上传导到 **展示层 (如 Controller)**，由最外层统一决定给用户返回什么 HTTP 状态码 (如 404, 403, 500)。

```java
// Controller 层统筹捕获示例
@PostMapping("/register")
public Result register(User user) {
    try {
        userService.register(user); // 这里面可能会抛出 UserExistException
        return Result.ok();
    } catch (UserExistException e) {
        return Result.fail(409, e.getMessage()); // 统筹决定返回 409
    }
}
```

## 5. 核心异常清单 (Must-Know Exceptions)

作为 Java 开发者，建议肌肉记忆以下异常，它们涵盖了 90% 的业务开发场景。

### (1) 非受检异常 (Unchecked - 通常是代码逻辑问题)

| 异常名称 | 触发场景 | Node.js 对标 |
| --- | --- | --- |
| **`NullPointerException` (NPE)** | 调用了 `null` 对象的方法或属性。Java 届的“头号杀手”。 | `TypeError: Cannot read property 'x' of undefined` |
| **`IllegalArgumentException`** | 方法接收到了非法的参数。 | 参数校验失败抛出的 Error |
| **`NumberFormatException`** | 尝试将格式错误的字符串转换为数字（如 `Integer.parseInt("abc")`）。 | `NaN` 相关的逻辑错误 |
| **`IndexOutOfBoundsException`** | 数组或集合索引越界。 | `arr[10]` (JS 返回 undefined, Java 抛异常) |
| **`ConcurrentModificationException`** | **重要！** 在使用 `for-each` 遍历集合的同时尝试使用 `list.remove()`。 | 集合并发修改冲突 |
| **`ClassCastException`** | 强制类型转换失败。 | 类型转换逻辑错误 |

### (2) 受检异常 (Checked - 通常是外部环境问题)

| 异常名称 | 触发场景 | 应对建议 |
| --- | --- | --- |
| **`IOException`** | 读写文件、网络通信等所有 I/O 操作的基类异常。 | 检查路径、权限、网络连通性 |
| **`FileNotFoundException`** | 尝试打开的文件在物理磁盘上不存在。 | `IOException` 的子类，建议单独处理路径逻辑 |
| **`SQLException`** | 数据库查询语法错误、连接超时或驱动问题。 | 检查 SQL 语句、数据库连接配置 |
| **`InterruptedException`** | 线程在等待、睡眠或占用时被中断。 | 多线程协作时的标准响应 |

### (3) 系统错误 (Error - 灾难性问题)

| 错误名称 | 触发场景 | 应对建议 |
| --- | --- | --- |
| **`OutOfMemoryError` (OOM)** | JVM 堆内存耗尽，无法分配新对象。 | 调大 `-Xmx` 参数或排查内存泄漏 |
| **`StackOverflowError`** | 递归调用过深，导致线程栈溢出。 | 检查递归出口逻辑 |
| **`NoClassDefFoundError`** | 编译时类存在，但运行时找不到类文件（通常是依赖冲突）。 | 检查 Maven 依赖或运行时 Classpath |

---

---
**参考资料**：
- [Baeldung: Exception Handling in Java](https://www.baeldung.com/java-exceptions)
- [Oracle Docs: Exceptions Tutorial](https://docs.oracle.com/javase/tutorial/essential/exceptions/index.html)
