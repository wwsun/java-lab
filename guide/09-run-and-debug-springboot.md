# Spring Boot 启动与调试：从 npm 到 IDEA

对于 Node.js 开发者，你会发现 IDEA 的启动和调试功能比 VS Code 配合插件要强大且集成度更高。

## 1. 运行 (Run) vs 调试 (Debug)
- **Run (绿色箭头)**：直接运行。
- **Debug (小虫子图标)**：启动断点调试。在 Java 开发中，**强烈建议优先选择 Debug 模式**，因为它支持"热加载" (Hot Swap)，当你修改方法内部逻辑时，IDEA 可以直接把新代码"替换"进正在运行的应用中。

## 2. 观察控制台日志
启动后，底部会弹出 **Run** 窗口。重点关注最后几行：
- `Tomcat initialized with port 8080 (http)`：说明应用监听在 8080 端口。
- `Started JavaLabsApplication in ... seconds`：说明启动成功。

## 3. 验证接入 Redis
应用启动后，你可以通过浏览器或 `curl` 测试我们刚才集成的 Redis 功能：

- **设置 Key**：访问 `http://localhost:8080/api/redis/string/set?key=claudecode&value=is_awesome`
- **读取 Key**：访问 `http://localhost:8080/api/redis/string/get?key=claudecode`

## 4. 常见问题：端口占用
如果你看到 `Address already in use` 错误，说明 8080 端口已被占用。
- **解决方案**：在 `application.yml` 中修改 `server.port: 8081`，或者杀掉占用端口的进程。
