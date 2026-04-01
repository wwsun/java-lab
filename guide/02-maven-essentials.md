# 02-maven-essentials

对于从 Node.js/TypeScript 转型而来的全栈开发者，如果把 Maven 对标为 npm/yarn，其实你**不需要**背诵厚厚的 Maven 官方手册。在日常的微服务与现代 Java 开发中，掌握以下 4 个核心护城河机制，就已经能应对 95% 以上的工作了。

## 1. 结构与坐标：GAV (GroupID, ArtifactID, Version)

在 npm 中，包的唯一标识是包名和版本（例如 `react@18.0.0`，带域名的则是 `@types/node@20`）。
在 Maven 中，寻找和下载一个包的唯一坐标是 **GAV**：

*   **`groupId`**：组织或公司的反向域名，比如 `org.springframework.boot`。相当于 NPM 的域 (Scope)。
*   **`artifactId`**：具体的包/模块名，比如 `spring-boot-starter-web`。
*   **`version`**：版本号。

**🔥 避坑提示**：平时不要去手写这些标签。需要任何包，直接访问 [MvnRepository](https://mvnrepository.com/) 搜索并复制 XML 片段。

## 2. 依赖管理：传递依赖与 "Npm 幽灵依赖"

npm 有 `package-lock.json` 和 `npm dedupe` 来处理错综复杂的依赖树树枝。
Maven 默认**没有 lock 文件**，它依靠一种名为**“最短路径优先”**的传递性依赖解析机制。

### 查看依赖树
遇到“类找不到 (ClassNotFoundException)” 或 “方法找不到 (NoSuchMethodError)” 报错时，第一反应应是检查是不是引入了两个不同版本的同一个包。这等同于 `npm ls`：
```bash
mvn dependency:tree
```

### 排除冲突包 (Exclusions)
当你引入了库 A，库 A 内部包含了旧版的库 C；但你又想使用新版的库 C。这个时候需要手动排雷：
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>library-a</artifactId>
    <version>1.0.0</version>
    <!-- 排除掉 library-a 中自带的旧版库 C -->
    <exclusions>
        <exclusion>
            <groupId>com.old.lib</groupId>
            <artifactId>library-c</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## 3. 统一全局版本：`<dependencyManagement>`

在 monorepo 或大型 Spring Cloud 项目中，每个微服务都要声明依赖。如果每个模块各自写版本号，升级时会非常痛苦。
Maven 提供了 `<dependencyManagement>` 标签进行版本“统一锁定”。

**Node.js 类比**：它就像是在根辈分的 `Workspace` 里定义了一份 `peerDependencies` 图谱。
在父 `pom.xml` 中声明后，子模块再引依赖时**无需写版本号**，会自动继承父类的版本！

```xml
<!-- 父 pom.xml 中锁定版本 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>32.1.2-jre</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 4. 全局加速镜像：`settings.xml`

在 Node 世界里，如果你面临网络下载过慢的问题，你会执行：
`npm config set registry https://registry.npmmirror.com`

在 Java 世界中，中央仓库在国外。你需要修改 Maven 本地的 `settings.xml`（通常位于 `~/.m2/settings.xml` 或 Maven 安装目录下的 `conf/settings.xml`），配置阿里云镜像。

```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>central</mirrorOf>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

## 总结
作为后端架构方向的学习者，您只要知道：如何找依赖 (**GAV**)，如何看依赖冲突 (**`mvn dependency:tree`**)，如何排除依赖 (**`<exclusion>`**)，就已经毕业了。其余构建流程，交给 IDEA 面板和 CI/CD 流水线即可。
