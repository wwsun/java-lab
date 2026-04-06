# 38 - 实战演练：书籍管理系统后端实现

本指南将带你从零开始构建一个具备 **JWT 安全认证**、**MyBatis-Plus 持久化**及**两层分类逻辑**的后端系统。这是你从基础语法进入企业级 Web 开发的关键一步。

## 1. 业务背景与需求说明

### 背景

作为 Node.js 开发者，你已经习惯了使用 `express-jwt` 或 `Passport`。在 Java 世界中，我们将通过手动集成 `jjwt` 库并利用 Spring Boot 的拦截机制，实现一个类似架构的书籍管理系统。

### 核心需求

1.  **用户中心**：支持用户注册与登录，登录后颁发具有 24 小时有效期的 JWT。
2.  **分类管理**：支持两层分类（如“计算机”下有“后端开发”）。
3.  **书籍目录**：
    - 分页查询书籍列表。
    - 支持按书名模糊搜索和按分类过滤。
    - 管理员（ADMIN 角色）可执行增删改操作。
4.  **借阅记录**：普通用户（USER 角色）可以申请借阅，系统需自动扣减库存。

---

## 2. 数据库设计方案 (DDL)

我们将遵循阿里巴巴《Java 开发手册》规范，**不使用物理外键**，所有数据关系通过业务代码维护。

```sql
-- 1. 书籍分类表
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID (0为一级分类)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='书籍分类表';

-- 2. 书籍信息表
CREATE TABLE IF NOT EXISTS `books` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) NOT NULL COMMENT '作者',
    `isbn` VARCHAR(20) UNIQUE NOT NULL COMMENT 'ISBN 编号',
    `category_id` BIGINT NOT NULL COMMENT '逻辑外键: 分类ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `description` TEXT COMMENT '书籍简介',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='书籍信息表';

-- 3. 用户表 (包含 RBAC 角色)
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) UNIQUE NOT NULL,
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密文',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户信息表';

-- 4. 借阅记录表
CREATE TABLE IF NOT EXISTS `borrow_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '逻辑外键: 用户ID',
    `book_id` BIGINT NOT NULL COMMENT '逻辑外键: 书籍ID',
    `borrow_date` DATE NOT NULL COMMENT '借书日期',
    `return_date` DATE COMMENT '归还日期',
    `status` INT DEFAULT 0 COMMENT '状态: 0-借出, 1-已还',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='借阅历史表';
```

---

## 3. 任务执行计划

1.  **基础设施配置**：
    - 在 `pom.xml` 中引入 `jjwt-api`, `jjwt-impl`, `jjwt-jackson`。
    - 在 `application.yml` 中配置数据库连接。
2.  **Security 层实现**：
    - 创建 `JwtUtils.java` 负责生成/解析令牌。
    - (可选) 编写 `JwtInterceptor` 拦截受保护的 API。
3.  **持久层 (MyBatis-Plus)**：
    - 通过 AI 指令或手动创建 `Book`, `User`, `Category` 实体类（使用 Lombok）。
    - 编写对应的 `Mapper` 接口。
4.  **业务层 (Service)**：
    - `UserService.login()`: 校验 BCrypt 密码并生成 JWT。
    - `BookService.borrowBook()`: 开启事务，减库存并插入借阅记录。
5.  **接口层 (Controller)**：
    - 实现统一返回格式 `Result<T>`。

---

## 4. 验证方案与实操指令

### 第一步：数据库初始化

将上述 DDL 复制并运行。你可以使用 MySQL Client 或 IDEA 内置的 Database 面板。

### 第二步：API 功能验证 (Curl/Postman)

#### 1. 登录并获取 JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "password123"}'
```

> **预期结果**：返回 `code: 200`，`data` 中包含以 `eyJ...` 开头的长字符串。

#### 2. 未授权访问测试（故意不带 Token）

```bash
curl -i -X POST http://localhost:8080/api/books \
     -H "Content-Type: application/json" \
     -d '{"title":"Java核心技术","price":99.0}'
```

> **预期结果**：HTTP 状态码 `401 Unauthorized` 或业务 Code `401`。

#### 3. 模拟借阅书籍 (带 Token)

```bash
curl -X POST http://localhost:8080/api/books/1/borrow \
     -H "Authorization: Bearer <你的TOKEN>" \
     -H "Content-Type: application/json"
```

> **预期结果**：返回“借阅成功”。

---

## 5. 常见 Q&A

- **Q: 为什么要用 BCrypt 而不是普通的 MD5?**
  - A: BCrypt 强迫破解者进行极其缓慢的哈希运算，并自带“盐值”。即便两个用户密码都是 `123456`，存储在数据库里的密文也完全不同。
- **Q: 逻辑外键发生数据不一致怎么办？**
  - A: 既然移除了数据库强约束，你就必须在 Service 层编写代码检查。例如：删除分类前，先查询该分类下是否有存量书籍。

## 扩展阅读

1. [阿里巴巴 Java 开发手册 (黄山版)](https://github.com/alibaba/p3c) - 重点阅读“数据库设计”章节。
2. [Spring Security Password Encoder 详解](https://www.baeldung.com/spring-security-registration-password-encoding-bcrypt)
