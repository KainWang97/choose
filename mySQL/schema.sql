-- ============================================
-- 資料庫建置與測試資料腳本
-- 專案: chooseMVP
-- ============================================

-- 1. 建立資料庫 (若不存在則建立)
CREATE DATABASE IF NOT EXISTS chooseMVP CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE chooseMVP;

-- 暫時關閉外鍵檢查，避免建表或插資料時因順序問題報錯
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一部分：清理舊表 (若需重置時使用，預設啟用)
-- ============================================
DROP TABLE IF EXISTS contact_messages;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS product_variants;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- ============================================
-- 第二部分：建立資料表結構 (CREATE TABLES)
-- ============================================

-- 1. 使用者表 (Users)
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '使用者ID',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '帳號/信箱',
    password VARCHAR(255) COMMENT '加密後的密碼 (去識別化後為NULL)',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '電話',
    role ENUM('MEMBER', 'ADMIN') NOT NULL DEFAULT 'MEMBER' COMMENT '權限角色',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已刪除帳號',
    deleted_at TIMESTAMP NULL COMMENT '帳號刪除時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '註冊時間',
    INDEX idx_is_deleted (is_deleted)
) COMMENT='使用者資料表';

-- 2. 商品分類表 (Categories)
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分類名稱',
    description VARCHAR(200) COMMENT '分類描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間'
) COMMENT='商品分類表';

-- 3. 商品主檔 (Products)
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '關聯分類',
    name VARCHAR(100) NOT NULL COMMENT '商品名稱',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品單價',
    image_url VARCHAR(255) COMMENT '商品主圖URL',
    is_listed BOOLEAN DEFAULT TRUE COMMENT '是否上架',
    is_featured BOOLEAN DEFAULT FALSE COMMENT '是否為新品上架',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_category_listed (category_id, is_listed),
    INDEX idx_name (name),
    INDEX idx_created_at (created_at)
) COMMENT='商品主檔';

-- 4. 商品規格變體 (Product Variants / SKUs)
CREATE TABLE product_variants (
    variant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '關聯商品主檔',
    sku_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'SKU編碼 (ex: SHIRT-W-S)',
    color VARCHAR(20) NOT NULL COMMENT '顏色',
    size VARCHAR(10) NOT NULL COMMENT '尺寸',
    stock INT NOT NULL DEFAULT 0 COMMENT '庫存數量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_color_size (product_id, color, size),
    INDEX idx_stock (stock)
) COMMENT='商品規格庫存表';

-- 5. 購物車 (Cart Items)
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '會員ID',
    variant_id BIGINT NOT NULL COMMENT '商品規格ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '數量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入購物車時間',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_variant (user_id, variant_id),
    INDEX idx_user (user_id),
    INDEX idx_created_at (created_at)
) COMMENT='購物車明細';

-- 6. 訂單主檔 (Orders)
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '下單會員',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '訂單總金額',
    status ENUM('PENDING', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '訂單狀態',
    payment_method VARCHAR(20) NOT NULL DEFAULT 'BANK_TRANSFER' COMMENT '付款方式',
    payment_note TEXT NULL COMMENT '訂單備注',
    shipping_method VARCHAR(20) NOT NULL COMMENT '運送方式 (STORE, MAIL)',
    recipient_name VARCHAR(50) NOT NULL COMMENT '收件人姓名',
    recipient_phone VARCHAR(20) NOT NULL COMMENT '收件人電話',
    shipping_address VARCHAR(255) NOT NULL COMMENT '地址或超商門市資訊',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下單時間',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) COMMENT='訂單主檔';

-- 7. 訂單明細 (Order Items)
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL COMMENT '購買當下的單價',
    quantity INT NOT NULL COMMENT '購買數量',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    INDEX idx_order (order_id)
) COMMENT='訂單商品明細';

-- 8. 聯絡訊息表 (Contact Messages)
CREATE TABLE contact_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL COMMENT '會員ID (若為已登入會員送出)',
    name VARCHAR(50) NOT NULL COMMENT '填寫人姓名',
    email VARCHAR(100) NOT NULL COMMENT '聯絡信箱',
    message TEXT NOT NULL COMMENT '訊息內容',
    status ENUM('UNREAD', 'READ', 'REPLIED') NOT NULL DEFAULT 'UNREAD' COMMENT '處理狀態',
    ip_address VARCHAR(45) COMMENT 'IP位址 (用於防濫發)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '送出時間',
    replied_at TIMESTAMP NULL COMMENT '回覆時間',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_status_created (status, created_at),
    INDEX idx_email (email),
    INDEX idx_ip_created (ip_address, created_at)
) COMMENT='聯絡訊息表';

-- ============================================
-- 第三部分：插入測試資料 (INSERT DATA)
-- ============================================

-- 1. 使用者 (Users)
INSERT INTO users (email, password, name, phone, role, created_at) VALUES
('admin@choose.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理員', '0912345678', 'ADMIN', NOW()),
('user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '張三', '0923456789', 'MEMBER', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('user2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李四', '0934567890', 'MEMBER', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('user3@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王五', '0945678901', 'MEMBER', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('user4@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '趙六', '0956789012', 'MEMBER', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 2. 商品分類 (Categories)
INSERT INTO categories (name, description, created_at) VALUES
('上衣', '各式上衣商品，包含T恤、襯衫、針織衫等', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('褲類', '各式褲款商品，包含長褲、短褲、寬褲等', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('外套', '各式外套商品，包含風衣、夾克、大衣等', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('配件', '各式配件商品，包含帽子、包包、飾品等', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('鞋類', '各式鞋款商品', DATE_SUB(NOW(), INTERVAL 60 DAY));

-- 3. 商品主檔 (Products)
INSERT INTO products (category_id, name, description, price, image_url, is_listed, created_at) VALUES
(1, '白色基本款T恤', '五分袖、寬鬆剪裁、落肩設計，營造休閒感。百搭的經典款式，適合日常穿搭。', 580.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765764556/T.png', TRUE, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(1, '灰色刺繡字母襯衫', '精緻刺繡字母設計，質感面料，適合商務休閒場合。', 1280.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765126114/kx35gnwkfiabfvkupriw.png', TRUE, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(1, '條紋針織衫', '柔軟針織材質，條紋設計增添時尚感，春秋季節必備單品。', 1580.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765764560/Beige.png', TRUE, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(2, '黑色長寬褲', '經典黑色寬版長褲，舒適百搭，適合日常穿搭，版型修身不顯胖。', 1280.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1764695664/ofz5xacuymgz87sbvqbw.png', TRUE, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(2, '卡其色直筒褲', '經典卡其色，直筒版型修飾腿型，適合各種場合穿著。', 1180.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765764563/Asset_3.png', TRUE, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2, '深藍色牛仔褲', '經典牛仔褲款，彈性材質，舒適耐穿，百搭必備。', 980.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765764566/Asset_4.png', TRUE, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(3, '黑色風衣外套', '防風防潑水材質，簡約設計，適合春秋季節穿著。', 2580.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765124753/gpcji2ea4d0wfhbyzg05.png', TRUE, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(3, '米色針織外套', '柔軟針織材質，寬鬆版型，舒適保暖，適合室內外穿著。', 1880.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765764563/Asset_3.png', TRUE, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(4, '帆布托特包', '大容量設計，堅固耐用，適合日常通勤使用。', 680.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765765059/Asset_1.png', TRUE, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, '棒球帽', '經典棒球帽設計，可調節帽圍，防曬遮陽必備。', 480.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1765765062/Asset_2.png', TRUE, DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 4. 商品規格 (Product Variants)
INSERT INTO product_variants (product_id, sku_code, color, size, stock, created_at) VALUES
(1, 'TSHIRT-WHT-S', '白色', 'S', 20, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(1, 'TSHIRT-WHT-M', '白色', 'M', 25, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(1, 'TSHIRT-WHT-L', '白色', 'L', 18, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(1, 'TSHIRT-WHT-XL', '白色', 'XL', 12, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(2, 'SHIRT-GRY-S', '灰色', 'S', 15, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2, 'SHIRT-GRY-M', '灰色', 'M', 20, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2, 'SHIRT-GRY-L', '灰色', 'L', 18, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2, 'SHIRT-GRY-XL', '灰色', 'XL', 10, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(3, 'KNIT-STR-S', '條紋', 'S', 12, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(3, 'KNIT-STR-M', '條紋', 'M', 15, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(3, 'KNIT-STR-L', '條紋', 'L', 10, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(4, 'PANT-BLK-S', '黑色', 'S', 15, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(4, 'PANT-BLK-M', '黑色', 'M', 20, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(4, 'PANT-BLK-L', '黑色', 'L', 18, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(4, 'PANT-BLK-XL', '黑色', 'XL', 10, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(5, 'PANT-KHK-S', '卡其色', 'S', 12, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(5, 'PANT-KHK-M', '卡其色', 'M', 18, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(5, 'PANT-KHK-L', '卡其色', 'L', 15, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(5, 'PANT-KHK-XL', '卡其色', 'XL', 8, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(6, 'JEAN-BLU-28', '深藍色', '28', 10, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(6, 'JEAN-BLU-30', '深藍色', '30', 15, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(6, 'JEAN-BLU-32', '深藍色', '32', 20, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(6, 'JEAN-BLU-34', '深藍色', '34', 12, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(7, 'COAT-BLK-S', '黑色', 'S', 8, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(7, 'COAT-BLK-M', '黑色', 'M', 10, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(7, 'COAT-BLK-L', '黑色', 'L', 8, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(7, 'COAT-BLK-XL', '黑色', 'XL', 5, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(8, 'CARD-BEI-S', '米色', 'S', 10, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(8, 'CARD-BEI-M', '米色', 'M', 12, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(8, 'CARD-BEI-L', '米色', 'L', 10, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(9, 'BAG-TOTE-ONE', '米色', 'ONE', 30, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(10, 'CAP-BASE-ONE', '黑色', 'ONE', 25, DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 5. 購物車 (Cart Items)
INSERT INTO cart_items (user_id, variant_id, quantity, created_at) VALUES
(2, 1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 5, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 13, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 2, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 21, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 29, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 6, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 14, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 31, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 6. 訂單主檔 (Orders)
INSERT INTO orders (user_id, total_amount, status, payment_method, payment_note, shipping_method, recipient_name, recipient_phone, shipping_address, created_at) VALUES
(2, 2560.00, 'PAID', 'BANK_TRANSFER', '已轉帳，後五碼：12345', 'MAIL', '張三', '0923456789', '台北市信義區信義路五段7號', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 1280.00, 'SHIPPED', 'BANK_TRANSFER', NULL, 'STORE', '張三', '0923456789', '7-11 信義門市', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 580.00, 'COMPLETED', 'BANK_TRANSFER', NULL, 'MAIL', '張三', '0923456789', '台北市大安區敦化南路二段216號', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 2540.00, 'PENDING', 'BANK_TRANSFER', NULL, 'MAIL', '李四', '0934567890', '新北市板橋區文化路一段188巷', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 980.00, 'COMPLETED', 'BANK_TRANSFER', '已轉帳', 'STORE', '李四', '0934567890', '全家 板橋門市', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4, 2560.00, 'PAID', 'BANK_TRANSFER', '已轉帳，後五碼：67890', 'MAIL', '王五', '0945678901', '台中市西屯區台灣大道三段99號', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, 1880.00, 'SHIPPED', 'BANK_TRANSFER', NULL, 'STORE', '王五', '0945678901', 'OK超商 西屯門市', DATE_SUB(NOW(), INTERVAL 7 DAY));

-- 7. 訂單明細 (Order Items)
INSERT INTO order_items (order_id, variant_id, price, quantity) VALUES
(1, 5, 1280.00, 1), (1, 13, 1280.00, 1),
(2, 14, 1280.00, 1),
(3, 1, 580.00, 1),
(4, 2, 580.00, 1), (4, 21, 980.00, 2),
(5, 22, 980.00, 1),
(6, 6, 1280.00, 1), (6, 15, 1280.00, 1),
(7, 29, 1880.00, 1);

-- 8. 聯絡訊息 (Contact Messages)
INSERT INTO contact_messages (user_id, name, email, message, status, ip_address, created_at, replied_at) VALUES
(2, '張三', 'user1@example.com', '請問商品何時會到貨？', 'REPLIED', '192.168.1.100', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, '李四', 'user2@example.com', '可以退換貨嗎？', 'READ', '192.168.1.101', DATE_SUB(NOW(), INTERVAL 15 DAY), NULL),
(4, '王五', 'user3@example.com', '商品品質很好，謝謝！', 'REPLIED', '192.168.1.102', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
(NULL, '陳七', 'guest1@example.com', '想詢問是否有其他顏色？', 'UNREAD', '192.168.1.103', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),
(NULL, '林八', 'guest2@example.com', '商品尺寸如何選擇？', 'READ', '192.168.1.104', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),
(NULL, '黃九', 'guest3@example.com', '請問有實體店面嗎？', 'UNREAD', '192.168.1.105', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL);

-- ============================================
-- 第四部分：資料驗證 (VALIDATION)
-- ============================================
SELECT 'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'products', COUNT(*) FROM products
UNION ALL SELECT 'product_variants', COUNT(*) FROM product_variants
UNION ALL SELECT 'cart_items', COUNT(*) FROM cart_items
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL SELECT 'contact_messages', COUNT(*) FROM contact_messages;

-- 恢復外鍵檢查
SET FOREIGN_KEY_CHECKS = 1;
