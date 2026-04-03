# 15-Runnable 与 Callable：任务定义的两大标准

在 Java 并发编程中，我们通过接口来抽象“任务”。最常用的两个标准是 `Runnable` 和 `Callable`。

## 1. Runnable：最简单的任务 (没有回报的劳动)

`Runnable` 用于那些“只管去做，不求结果”的场景，比如记录一条日志、更新一个不重要的缓存。

```java
Runnable task = () -> {
    System.out.println("正在执行异步后台任务...");
};
new Thread(task).start();
```

## 2. Callable：高级任务 (有反馈的思考)

如果你需要任务执行完后返回一个值（比如从数据库查出的用户信息），你需要 `Callable`。

```java
Callable<Integer> calculateTask = () -> {
    Thread.sleep(1000); // 模拟耗时计算
    return 42; 
};
```

## 3. 核心对比

| 维度 | `Runnable` | `Callable<V>` |
| --- | --- | --- |
| **对标 JS** | `new Promise((resolve) => { ... resolve(); })` | `async () => { return 42; }` |
| **返回结果** | 无 (void) | 有 (V) |
| **异常处理** | 内部强制 try-catch | 可以 throw 异常 |
| **获取方式** | 直接运行 | 配合 `Future` 对象获取结果 |

## 4. 如何拿到 Callable 的结果？ (Future 模式)

当你把 `Callable` 交给线程池时，它会立刻返回一个 **`Future`**。这就像一张“取货凭证”。

```java
ExecutorService executor = Executors.newFixedThreadPool(1);
Future<Integer> ticket = executor.submit(() -> 42);

// 在未来的某个时刻，通过凭证取货
Integer result = ticket.get(); // 这一步会阻塞，直到任务完成 (类似 JS 的 await)
```

---
**参考资料**：
- [Baeldung: Java Runnable vs Callable](https://www.baeldung.com/java-runnable-callable)
- [Oracle Java Docs: Callable Interface](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Callable.html)
