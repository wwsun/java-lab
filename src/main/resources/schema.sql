-- 数据库初始化脚本

-- 创建用户表 (增强版: 支持权限角色)
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100),
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密文',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 书籍分类表 (逻辑外键关联)
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID (0为一级分类)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 书籍信息表 (逻辑外键关联)
CREATE TABLE IF NOT EXISTS `books` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) NOT NULL COMMENT '作者',
    `isbn` VARCHAR(20) UNIQUE NOT NULL COMMENT 'ISBN 编号',
    `category_id` BIGINT NOT NULL COMMENT '逻辑外键: 分类ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `description` TEXT COMMENT '书籍简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 借阅记录表 (逻辑外键关联)
CREATE TABLE IF NOT EXISTS `borrow_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '逻辑外键: 用户ID',
    `book_id` BIGINT NOT NULL COMMENT '逻辑外键: 书籍ID',
    `borrow_date` DATE NOT NULL COMMENT '借书日期',
    `return_date` DATE COMMENT '归还日期',
    `status` INT DEFAULT 0 COMMENT '状态: 0-借出, 1-已还',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 历史任务表 (保留供兼容性参考)
CREATE TABLE IF NOT EXISTS `tasks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `status` VARCHAR(20) DEFAULT 'PENDING',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建员工表 (MyBatis-Plus 实战对象 - 保留)
CREATE TABLE IF NOT EXISTS `employees` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `department` VARCHAR(100),
    `salary` DOUBLE,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
