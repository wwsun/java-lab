-- 初始化分类数据
INSERT IGNORE INTO categories (name, parent_id) VALUES ('计算机科学', 0);
INSERT IGNORE INTO categories (name, parent_id) VALUES ('文学艺术', 0);
INSERT IGNORE INTO categories (name, parent_id) VALUES ('Java 编程', 1);
INSERT IGNORE INTO categories (name, parent_id) VALUES ('Python 编程', 1);

-- 初始化管理员用户 (密码: password123)
-- BCrypt 密文: $2a$10$UMxy0Qkhq/L2s3DnDVce0uXTx9dDhVGziUlh.mN/DeINfaDiSp8JC
INSERT IGNORE INTO users (username, email, password, role) VALUES 
('admin', 'admin@example.com', '$2a$10$UMxy0Qkhq/L2s3DnDVce0uXTx9dDhVGziUlh.mN/DeINfaDiSp8JC', 'ADMIN'),
('user1', 'user1@example.com', '$2a$10$UMxy0Qkhq/L2s3DnDVce0uXTx9dDhVGziUlh.mN/DeINfaDiSp8JC', 'USER');

-- 初始化书籍数据
INSERT IGNORE INTO books (title, author, isbn, category_id, price, stock, description) VALUES 
('Effective Java', 'Joshua Bloch', '978-0134685991', 3, 59.99, 10, 'Java 进阶必读经典'),
('Spring Boot 实战', 'Craig Walls', '978-1617292545', 3, 45.00, 5, 'Spring Boot 入门首选');
