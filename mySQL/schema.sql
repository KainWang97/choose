CREATE DATABASE IF NOT EXISTS chooseMVP CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE chooseMVP;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reply_templates;
DROP TABLE IF EXISTS contact_messages;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS product_variants;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS verification_tokens;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role ENUM('MEMBER', 'ADMIN') NOT NULL DEFAULT 'MEMBER',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    password_set BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_is_deleted (is_deleted)
);

CREATE TABLE verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    type ENUM('EMAIL_VERIFY', 'PASSWORD_RESET', 'LOGIN_LINK') NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_type (user_id, type)
);

CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    color_images TEXT,
    is_listed BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_category_listed (category_id, is_listed),
    INDEX idx_name (name),
    INDEX idx_created_at (created_at)
);

CREATE TABLE product_variants (
    variant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku_code VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(20) NOT NULL,
    size VARCHAR(10) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_color_size (product_id, color, size),
    INDEX idx_stock (stock)
);

CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_variant (user_id, variant_id),
    INDEX idx_user (user_id),
    INDEX idx_created_at (created_at)
);

CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL DEFAULT 'BANK_TRANSFER',
    payment_note TEXT NULL,
    shipping_method VARCHAR(20) NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
);

CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    INDEX idx_order (order_id)
);

CREATE TABLE contact_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NULL,
    message TEXT NOT NULL,
    status ENUM('PENDING', 'REPLIED_TRACKING', 'CLOSED') NOT NULL DEFAULT 'PENDING',
    admin_reply TEXT NULL,
    admin_reply_by BIGINT NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    replied_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (admin_reply_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_case_number (case_number),
    INDEX idx_status_created (status, created_at),
    INDEX idx_email (email),
    INDEX idx_ip_created (ip_address, created_at)
);

-- 回覆模板表
CREATE TABLE reply_templates (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 預設回覆模板
INSERT INTO reply_templates (name, content, created_by) VALUES
('一般問候', '親愛的 {name}，\n\n感謝您的來信詢問。\n\n{reply}\n\n如有任何其他問題，歡迎隨時與我們聯繫。\n\nChoose 客服團隊', 1),
('商品庫存', '親愛的 {name}，\n\n關於您詢問的商品庫存問題：\n\n{reply}\n\n感謝您的耐心等候！\n\nChoose 客服團隊', 1),
('退換貨說明', '親愛的 {name}，\n\n關於您的退換貨詢問：\n\n本店提供 7 天鑑賞期，商品未拆封、未使用可辦理退換貨。\n\n{reply}\n\n如需進一步協助，請隨時與我們聯繫。\n\nChoose 客服團隊', 1),
('運送時間', '親愛的 {name}，\n\n關於您詢問的運送時間：\n\n一般訂單約 3-5 個工作天送達，偏遠地區可能需要多 1-2 天。\n\n{reply}\n\n感謝您的耐心等候！\n\nChoose 客服團隊', 1);

INSERT INTO users (email, password, name, phone, role, created_at) VALUES
('kevin.lin@choose.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzGjQYQQ.YjL1XMa9O6', '林凱文', '0923456789', 'MEMBER', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('amy.chen@choose.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzGjQYQQ.YjL1XMa9O6', '陳怡君', '0934567890', 'MEMBER', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('david.huang@choose.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzGjQYQQ.YjL1XMa9O6', '黃大維', '0945678901', 'MEMBER', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('jenny.wu@choose.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzGjQYQQ.YjL1XMa9O6', '吳佳穎', '0956789012', 'MEMBER', DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO categories (name, description, created_at) VALUES
('Top', '上衣、T恤、襯衫、針織衫、毛衣', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('Bottom', '褲款、長褲、褲子、短褲、寬褲', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('Coat', '外套、風衣、夾克、大衣', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('Accessories', '配件、帽子、包包、飾品、眼鏡', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('Shoes', '鞋款、鞋子、靴子', DATE_SUB(NOW(), INTERVAL 60 DAY));

INSERT INTO products (category_id, name, description, price, image_url, color_images, is_listed, created_at) VALUES
-- Top (category_id = 1)
(1, '修身羊毛針織高領上衣', '修身版型，羊毛針織材質，高領設計保暖舒適，適合秋冬季節。', 580.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766813988/%E7%81%B0%E8%89%B2_%E4%BF%AE%E8%BA%AB%E7%BE%8A%E6%AF%9B%E9%87%9D%E7%B9%94%E9%AB%98%E9%A0%98%E4%B8%8A%E8%A1%A3_myhq.png', '{"莫蘭迪綠": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766813990/%E7%81%B0%E8%89%B2_%E4%BF%AE%E8%BA%AB%E7%BE%8A%E6%AF%9B%E9%87%9D%E7%B9%94%E9%AB%98%E9%A0%98%E4%B8%8A%E8%A1%A3_myhq_%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0.png"], "酒紅色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766813992/%E7%81%B0%E8%89%B2_%E4%BF%AE%E8%BA%AB%E7%BE%8A%E6%AF%9B%E9%87%9D%E7%B9%94%E9%AB%98%E9%A0%98%E4%B8%8A%E8%A1%A3_myhq_%E8%B1%AC%E8%82%9D%E7%B4%85.png"], "灰色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766813994/%E7%81%B0%E8%89%B2_%E4%BF%AE%E8%BA%AB%E7%BE%8A%E6%AF%9B%E9%87%9D%E7%B9%94%E9%AB%98%E9%A0%98%E4%B8%8A%E8%A1%A3_myhq_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766813997/%E7%81%B0%E8%89%B2_%E4%BF%AE%E8%BA%AB%E7%BE%8A%E6%AF%9B%E9%87%9D%E7%B9%94%E9%AB%98%E9%A0%98%E4%B8%8A%E8%A1%A3_myhq_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766813999/%E7%81%B0%E8%89%B2_%E4%BF%AE%E8%BA%AB%E7%BE%8A%E6%AF%9B%E9%87%9D%E7%B9%94%E9%AB%98%E9%A0%98%E4%B8%8A%E8%A1%A3_myhq_Detail.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, '雪尼爾寬鬆高領針織衫', '雪尼爾材質，寬鬆版型，高領設計保暖舒適，柔軟親膚。', 980.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816035/%E9%BB%91%E8%89%B2_%E9%9B%AA%E5%B0%BC%E7%88%BE%E5%AF%AC%E9%AC%86%E9%AB%98%E9%A0%98%E9%87%9D%E7%B9%94%E8%A1%AB_xlvu.png', '{"黑色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816042/%E9%BB%91%E8%89%B2_%E9%9B%AA%E5%B0%BC%E7%88%BE%E5%AF%AC%E9%AC%86%E9%AB%98%E9%A0%98%E9%87%9D%E7%B9%94%E8%A1%AB_xlvu_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816044/%E9%BB%91%E8%89%B2_%E9%9B%AA%E5%B0%BC%E7%88%BE%E5%AF%AC%E9%AC%86%E9%AB%98%E9%A0%98%E9%87%9D%E7%B9%94%E8%A1%AB_xlvu_Detail.png"], "莫蘭迪綠": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816037/%E9%BB%91%E8%89%B2_%E9%9B%AA%E5%B0%BC%E7%88%BE%E5%AF%AC%E9%AC%86%E9%AB%98%E9%A0%98%E9%87%9D%E7%B9%94%E8%A1%AB_xlvu_%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0.png"], "深橘色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816040/%E9%BB%91%E8%89%B2_%E9%9B%AA%E5%B0%BC%E7%88%BE%E5%AF%AC%E9%AC%86%E9%AB%98%E9%A0%98%E9%87%9D%E7%B9%94%E8%A1%AB_xlvu_%E6%B7%B1%E6%A9%98%E8%89%B2.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(1, '無縫喀什米爾針織POLO衫', '經典POLO領設計，結合傳統襯衫領與針織衫的柔軟感，搭配質感三扣門襟，輕鬆切換正式與休閒場合，展現簡約紳士風格。', 1280.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816334/%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0_%E7%84%A1%E7%B8%AB%E5%96%80%E4%BB%80%E7%B1%B3%E7%88%BE%E9%87%9D%E7%B9%94_POLO_%E8%A1%AB_o751.png', '{"莫蘭迪綠": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816340/%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0_%E7%84%A1%E7%B8%AB%E5%96%80%E4%BB%80%E7%B1%B3%E7%88%BE%E9%87%9D%E7%B9%94_POLO_%E8%A1%AB_o751_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816342/%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0_%E7%84%A1%E7%B8%AB%E5%96%80%E4%BB%80%E7%B1%B3%E7%88%BE%E9%87%9D%E7%B9%94_POLO_%E8%A1%AB_o751_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816344/%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0_%E7%84%A1%E7%B8%AB%E5%96%80%E4%BB%80%E7%B1%B3%E7%88%BE%E9%87%9D%E7%B9%94_POLO_%E8%A1%AB_o751_Detail.png"], "海軍藍": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816337/%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0_%E7%84%A1%E7%B8%AB%E5%96%80%E4%BB%80%E7%B1%B3%E7%88%BE%E9%87%9D%E7%B9%94_POLO_%E8%A1%AB_o751_%E6%B5%B7%E8%BB%8D%E8%97%8D.png"], "灰色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816338/%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0_%E7%84%A1%E7%B8%AB%E5%96%80%E4%BB%80%E7%B1%B3%E7%88%BE%E9%87%9D%E7%B9%94_POLO_%E8%A1%AB_o751_%E7%81%B0%E8%89%B2.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 18 DAY)),
-- Bottom (category_id = 2)
(2, '精紡羊毛寬褲', '經典寬版直筒剪裁，高腰設計搭配俐落的寬版褲管，能有效拉長腿部比例，營造出優雅且具氣場的都會風格。', 1380.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816529/%E6%B7%B1%E7%81%B0_%E7%B2%BE%E7%B4%A1%E7%BE%8A%E6%AF%9B%E5%AF%AC%E8%A4%B2_3efl.png', '{"深灰": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816534/%E6%B7%B1%E7%81%B0_%E7%B2%BE%E7%B4%A1%E7%BE%8A%E6%AF%9B%E5%AF%AC%E8%A4%B2_3efl_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816536/%E6%B7%B1%E7%81%B0_%E7%B2%BE%E7%B4%A1%E7%BE%8A%E6%AF%9B%E5%AF%AC%E8%A4%B2_3efl_Detail.png"], "黑色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766816531/%E6%B7%B1%E7%81%B0_%E7%B2%BE%E7%B4%A1%E7%BE%8A%E6%AF%9B%E5%AF%AC%E8%A4%B2_3efl_%E9%BB%91%E8%89%B2.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(2, '弧形剪裁錐形牛仔褲', '經典直筒剪裁，俐落的中高腰直筒版型，能有效修飾腿部線條，營造修長視覺效果，展現簡約大方的經典風格。', 1580.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817295/%E7%89%9B%E4%BB%94%E6%B7%B1%E8%97%8D%E8%89%B2_%E5%BC%A7%E5%BD%A2%E5%89%AA%E8%A3%81%E9%8C%90%E5%BD%A2%E7%89%9B%E4%BB%94%E8%A4%B2_62yu.png', '{"牛仔深藍色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817298/%E7%89%9B%E4%BB%94%E6%B7%B1%E8%97%8D%E8%89%B2_%E5%BC%A7%E5%BD%A2%E5%89%AA%E8%A3%81%E9%8C%90%E5%BD%A2%E7%89%9B%E4%BB%94%E8%A4%B2_62yu_Side.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(2, '羊毛法蘭絨長褲', '立體單褶設計，腰部下方的單褶細節增加臀部與大腿處活動空間，營造優雅的垂墜廓形，兼顧穿著舒適度與修身效果。', 1680.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817737/%E7%BE%8A%E6%AF%9B%E6%B3%95%E8%98%AD%E7%B5%A8%E9%95%B7%E8%A4%B2_r47y.png', '{"淺灰色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817741/%E7%BE%8A%E6%AF%9B%E6%B3%95%E8%98%AD%E7%B5%A8%E9%95%B7%E8%A4%B2_r47y_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817743/%E7%BE%8A%E6%AF%9B%E6%B3%95%E8%98%AD%E7%B5%A8%E9%95%B7%E8%A4%B2_r47y_Detail.png"], "深灰": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817739/%E6%B7%BA%E7%81%B0%E8%89%B2_%E7%BE%8A%E6%AF%9B%E6%B3%95%E8%98%AD%E7%B5%A8%E9%95%B7%E8%A4%B2_r47y_%E6%B7%B1%E7%81%B0.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 11 DAY)),
-- Coat (category_id = 3)
(3, '美麗諾羊毛西裝外套', '高級美麗諾羊毛材質，經典西裝剪裁，質感細膩，適合商務與正式場合。', 3980.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817290/%E6%B7%B1%E7%81%B0%E8%89%B2_%E7%BE%8E%E9%BA%97%E8%AB%BE%E6%B0%B4%E7%85%AE%E7%BE%8A%E6%AF%9B%E8%A5%BF%E8%A3%9D%E5%A4%96%E5%A5%97_bvn7.png', '{"深灰色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817292/%E6%B7%B1%E7%81%B0%E8%89%B2_%E7%BE%8E%E9%BA%97%E8%AB%BE%E6%B0%B4%E7%85%AE%E7%BE%8A%E6%AF%9B%E8%A5%BF%E8%A3%9D%E5%A4%96%E5%A5%97_bvn7_.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, '標準版型棉質拉鍊外套', '標準版型，優質棉質材質，拉鍊設計方便穿脫，休閒百搭款式。', 1680.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817300/%E6%B7%BA%E7%81%B0%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A3%89%E8%B3%AA%E6%8B%89%E9%8D%8A%E5%A4%96%E5%A5%97_bfqx.png', '{"淺灰色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817306/%E6%B7%BA%E7%81%B0%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A3%89%E8%B3%AA%E6%8B%89%E9%8D%8A%E5%A4%96%E5%A5%97_bfqx_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817309/%E6%B7%BA%E7%81%B0%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A3%89%E8%B3%AA%E6%8B%89%E9%8D%8A%E5%A4%96%E5%A5%97_bfqx_Detail.png"], "深灰": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817301/%E6%B7%BA%E7%81%B0%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A3%89%E8%B3%AA%E6%8B%89%E9%8D%8A%E5%A4%96%E5%A5%97_bfqx_%E6%B7%B1%E7%81%B0.png"], "黑色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817303/%E6%B7%BA%E7%81%B0%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A3%89%E8%B3%AA%E6%8B%89%E9%8D%8A%E5%A4%96%E5%A5%97_bfqx_%E9%BB%91%E8%89%B2.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 13 DAY)),
-- Accessories (category_id = 4)
(4, 'CITY皮革郵差包', '經典郵差包設計，優質皮革材質，簡約都會風格，適合日常通勤與休閒使用。', 2680.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817021/%E9%BB%91%E8%98%AD%E8%8A%B1_CITY_%E7%9A%AE%E9%9D%A9%E9%83%B5%E5%B7%AE%E5%8C%85_ehie.png', '{"黑蘭花": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817022/%E9%BB%91%E8%98%AD%E8%8A%B1_CITY_%E7%9A%AE%E9%9D%A9%E9%83%B5%E5%B7%AE%E5%8C%85_ehie_Detail.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(4, '針織肩背包', '極簡美學設計，採用細緻的針織羅紋紋理，展現低調且具質感的層次感，簡約造型能輕鬆融入各種日常穿搭風格。', 1280.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817730/%E6%B7%BA%E7%81%B0%E8%89%B2%E8%88%87%E9%BB%91%E8%89%B2%E7%9B%B8%E9%96%93_%E6%95%B4%E9%AB%94%E5%81%8F%E6%B7%B1%E8%89%B2%E7%B3%BB_Knit_Shoulder_Bag_gn9u.png', '{"灰黑相間": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817732/%E6%B7%BA%E7%81%B0%E8%89%B2%E8%88%87%E9%BB%91%E8%89%B2%E7%9B%B8%E9%96%93_%E6%95%B4%E9%AB%94%E5%81%8F%E6%B7%B1%E8%89%B2%E7%B3%BB_Knit_Shoulder_Bag_gn9u_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766817735/%E6%B7%BA%E7%81%B0%E8%89%B2%E8%88%87%E9%BB%91%E8%89%B2%E7%9B%B8%E9%96%93_%E6%95%B4%E9%AB%94%E5%81%8F%E6%B7%B1%E8%89%B2%E7%B3%BB_Knit_Shoulder_Bag_gn9u_Detail.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, '無框時尚眼鏡', '簡約半框設計，經典的半框造型結合流暢線條，展現知性且專業的個人氣質，能完美修飾臉型，適合各類商務與休閒場合。', 880.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819355/%E9%BB%91%E8%89%B2_%E7%84%A1%E6%A1%86%E6%99%82%E5%B0%9A%E7%9C%BC%E9%8F%A1_dyn3.png', '{"黑色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819357/%E9%BB%91%E8%89%B2_%E7%84%A1%E6%A1%86%E6%99%82%E5%B0%9A%E7%9C%BC%E9%8F%A1_dyn3_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819359/%E9%BB%91%E8%89%B2_%E7%84%A1%E6%A1%86%E6%99%82%E5%B0%9A%E7%9C%BC%E9%8F%A1_dyn3_Edit.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 10 DAY)),
-- 新增 Bottom (category_id = 2)
(2, '標準版型桶形燈芯絨長褲', '質感燈芯絨面料，採用細條紋燈芯絨織法，觸感溫潤紮實，特殊的立體紋理展現復古優雅的視覺層次，更提供絕佳的秋冬保暖性。', 1480.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819334/%E7%B1%B3%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A1%B6%E5%BD%A2%E7%87%88%E8%8A%AF%E7%B5%A8%E9%95%B7%E8%A4%B2_8c6t.png', '{"米色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819340/%E7%B1%B3%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A1%B6%E5%BD%A2%E7%87%88%E8%8A%AF%E7%B5%A8%E9%95%B7%E8%A4%B2_8c6t_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819343/%E7%B1%B3%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A1%B6%E5%BD%A2%E7%87%88%E8%8A%AF%E7%B5%A8%E9%95%B7%E8%A4%B2_8c6t_Detail.png"], "深灰色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819336/%E7%B1%B3%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A1%B6%E5%BD%A2%E7%87%88%E8%8A%AF%E7%B5%A8%E9%95%B7%E8%A4%B2_8c6t_%E6%B7%B1%E7%81%B0%E8%89%B2.png"], "白色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819339/%E7%B1%B3%E8%89%B2_%E6%A8%99%E6%BA%96%E7%89%88%E5%9E%8B%E6%A1%B6%E5%BD%A2%E7%87%88%E8%8A%AF%E7%B5%A8%E9%95%B7%E8%A4%B2_8c6t_%E7%99%BD%E8%89%B2.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 9 DAY)),
-- 新增 Coat (category_id = 3)
(3, 'GORE-TEX WINDSTOPPER 羽絨外套', '極致保暖與戰術機能設計，靈感源自經典的Level 7極地防寒系統，採用立體充絨工藝，提供卓越的蓬鬆度與保溫效能，確保在極低溫環境下依然能鎖住體溫。', 5980.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819345/%E9%BB%91%E8%89%B2_GORE_TEX_WINDSTOPPER_%E7%BE%BD%E7%B5%A8%E5%A4%96%E5%A5%97_LEVEL7_jy3j.png', '{"黑色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819348/%E9%BB%91%E8%89%B2_GORE_TEX_WINDSTOPPER_%E7%BE%BD%E7%B5%A8%E5%A4%96%E5%A5%97_LEVEL7_jy3j_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819350/%E9%BB%91%E8%89%B2_GORE_TEX_WINDSTOPPER_%E7%BE%BD%E7%B5%A8%E5%A4%96%E5%A5%97_LEVEL7_jy3j_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819353/%E9%BB%91%E8%89%B2_GORE_TEX_WINDSTOPPER_%E7%BE%BD%E7%B5%A8%E5%A4%96%E5%A5%97_LEVEL7_jy3j_Detail.png"], "淺灰": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819347/%E9%BB%91%E8%89%B2_GORE_TEX_WINDSTOPPER_%E7%BE%BD%E7%B5%A8%E5%A4%96%E5%A5%97_LEVEL7_jy3j_%E6%B7%BA%E7%81%B0.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, '復古工裝長版大衣', '結合復古工裝元素與現代剪裁的經典長版大衣，經典丹寧與燈芯絨撞色設計，採用深色水洗丹寧面料，搭配質感的卡其色燈芯絨領片，展現濃郁的復古工藝美學。', 4580.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819361/%E9%BB%91%E8%89%B2_%E8%A1%BF%E5%88%87_%E6%9B%BF__oexo.png', '{"黑色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819367/%E9%BB%91%E8%89%B2_%E8%A1%BF%E5%88%87_%E6%9B%BF__oexo_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819370/%E9%BB%91%E8%89%B2_%E8%A1%BF%E5%88%87_%E6%9B%BF__oexo_Detail.png"], "深橘色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766819365/%E9%BB%91%E8%89%B2_%E8%A1%BF%E5%88%87_%E6%9B%BF__oexo_%E6%B7%B1%E6%A9%98%E8%89%B2.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY)),
-- 新增 Bottom (category_id = 2)
(2, '直筒棉質長褲', '經典修身直筒剪裁，專為亞洲身型設計，線條流暢俐落，能有效修飾腿型，展現簡約而不失品味的職人風格。', 1680.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856719/mole_drak_green_COTTON_STRAIGHT_LEG_TROUSERS_pfrc.png', '{"鼴鼠灰褐": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856725/mole_drak_green_COTTON_STRAIGHT_LEG_TROUSERS_pfrc_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856727/mole_drak_green_COTTON_STRAIGHT_LEG_TROUSERS_pfrc_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856730/mole_drak_green_COTTON_STRAIGHT_LEG_TROUSERS_pfrc_Detail.png"], "深棕色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856722/mole_drak_green_COTTON_STRAIGHT_LEG_TROUSERS_pfrc_dark_brown.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY)),
-- 新增 Coat (category_id = 3)
(3, '圍巾絎縫內襯夾克', '融合經典絎縫工藝與現代設計語言，內襯採用精緻絎縫棉面料，提供輕盈保暖效果，搭配可拆卸圍巾設計，兼具機能性與時尚感。', 3980.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856732/mole_drak_green_SCARF_DETAIL_QUILTED_LINER_JACKET_jehr.png', '{"莫蘭迪綠": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856746/mole_drak_green_SCARF_DETAIL_QUILTED_LINER_JACKET_jehr_%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0.png"], "淺橘色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766856748/mole_drak_green_SCARF_DETAIL_QUILTED_LINER_JACKET_jehr_%E6%B7%BA%E6%A9%98%E8%89%B2.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- 新增 Accessories (category_id = 4)
(4, '格紋羊駝毛圍巾', '經典英倫格紋設計，採用沉穩的酒紅與炭灰撞色大格紋，風格簡約大方，展現低調奢華的視覺張力。羊駝毛混紡材質，輕盈保暖，觸感柔軟細緻。', 1280.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858617/Burgundy_Checked_CHECKED_ALPACA_BLEND_SCARF_zaas.png', '{"酒紅格紋": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858619/Burgundy_Checked_CHECKED_ALPACA_BLEND_SCARF_zaas_Detail.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, '雕塑托特包', '結合極簡主義與現代幾何美學的托特包，採用獨特的俐落線條與幾何造型，完美平衡現代感與簡約美學。麂皮材質，展現低調奢華的氣息。', 2680.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858625/Burgundy_Brown_SCULPTED_TOTE_BAG_SUEDE_fnbz.png', '{"酒紅棕": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858627/Burgundy_Brown_SCULPTED_TOTE_BAG_SUEDE_fnbz_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858630/Burgundy_Brown_SCULPTED_TOTE_BAG_SUEDE_fnbz_Detail.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY)),
-- 新增 Coat (category_id = 3)
(3, '雙面羊毛青果領大衣', '優雅青果領設計，獨特的深棕色青果領與海軍藍衣身形成鮮明的色彩對比，為簡約的設計注入高級層次感，展現低調而奢華的紳士魅力。', 6980.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858636/Dark_Brown_DOUBLE_FACED_WOOL_SHAWL_COLLAR_COAT_0nyq.png', '{"深棕海軍藍": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858640/Dark_Brown_DOUBLE_FACED_WOOL_SHAWL_COLLAR_COAT_0nyq_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858643/Dark_Brown_DOUBLE_FACED_WOOL_SHAWL_COLLAR_COAT_0nyq_Detail.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- 新增 Accessories (category_id = 4)
(4, '麂皮斜背包', '簡約俐落的長方形輪廓設計，展現現代摩登氣息，無論日常通勤或休閒約會皆能完美適配。高品質麂皮材質，觸感細膩柔軟。', 1880.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858648/Chocolate_TROVE_CROSSBODY_BAG_SUEDE_ewv0.png', '{"巧克力色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858649/Chocolate_TROVE_CROSSBODY_BAG_SUEDE_ewv0_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766858652/Chocolate_TROVE_CROSSBODY_BAG_SUEDE_ewv0_Detail.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- 新增 Top (category_id = 1)
(1, '寬鬆棉質T恤', '採用時下流行的 Oversize 廓形與落肩設計，不僅修飾身型，更能輕鬆打造隨興且具層次感的潮流街頭風格。優質棉質面料，柔軟透氣。', 680.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860665/OVERSIZED_COTTON_T_SHIRT_a2ir.png', '{"白色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860675/OVERSIZED_COTTON_T_SHIRT_a2ir_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860677/OVERSIZED_COTTON_T_SHIRT_a2ir_Detail.png"], "莫蘭迪綠": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860668/OVERSIZED_COTTON_T_SHIRT_a2ir_%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0.png"], "春天黃": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860670/OVERSIZED_COTTON_T_SHIRT_a2ir_%E6%98%A5%E5%A4%A9%E9%BB%83.png"], "月亮橘": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860672/OVERSIZED_COTTON_T_SHIRT_a2ir_%E6%9C%88%E4%BA%AE%E6%A9%98.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, '寬鬆棉質羅紋長袖T恤', '採用獨特的華夫格面料，細密的立體紋理賦予衣物豐富的觸感與視覺層次，兼具透氣性與保暖度。寬鬆版型，舒適百搭。', 880.00, 'https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860679/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt.png', '{"黑色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860689/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt_Side.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860691/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt_Back.png", "https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860693/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt_Detail.png"], "莫蘭迪綠": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860681/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt_%E8%8E%AB%E8%98%AD%E8%BF%AA%E7%B6%A0.png"], "秋天褐色": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860683/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt_%E7%A7%8B%E5%A4%A9%E8%A4%90%E8%89%B2.png"], "冬天灰白": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860685/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt_%E5%86%AC%E5%A4%A9%E7%81%B0%E7%99%BD.png"], "淺酒紅": ["https://res.cloudinary.com/dtowrsbhe/image/upload/v1766860688/%E5%AF%AC%E9%AC%86%E6%A3%89%E8%B3%AA%E7%BE%85%E7%B4%8B%E9%95%B7%E8%A2%96T%E6%81%A4_3o0bt_%E6%B7%BA%E9%85%92%E7%B4%85.png"]}', TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO product_variants (product_id, sku_code, color, size, stock, created_at) VALUES
-- 商品1: 修身羊毛針織高領上衣 (莫蘭迪綠、酒紅色、灰色)
(1, 'P1-MLD-S-001', '莫蘭迪綠', 'S', 15, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-MLD-M-002', '莫蘭迪綠', 'M', 20, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-MLD-L-003', '莫蘭迪綠', 'L', 18, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-ZGH-S-004', '酒紅色', 'S', 12, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-ZGH-M-005', '酒紅色', 'M', 15, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-ZGH-L-006', '酒紅色', 'L', 10, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-HUI-S-007', '灰色', 'S', 18, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-HUI-M-008', '灰色', 'M', 22, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'P1-HUI-L-009', '灰色', 'L', 15, DATE_SUB(NOW(), INTERVAL 20 DAY)),
-- 商品2: 雪尼爾寬鬆高領針織衫 (黑色、莫蘭迪綠、深橘色)
(2, 'P2-HEI-S-001', '黑色', 'S', 12, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-HEI-M-002', '黑色', 'M', 18, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-HEI-L-003', '黑色', 'L', 15, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-MLD-S-004', '莫蘭迪綠', 'S', 10, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-MLD-M-005', '莫蘭迪綠', 'M', 15, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-MLD-L-006', '莫蘭迪綠', 'L', 12, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-SJS-S-007', '深橘色', 'S', 8, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-SJS-M-008', '深橘色', 'M', 12, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'P2-SJS-L-009', '深橘色', 'L', 10, DATE_SUB(NOW(), INTERVAL 19 DAY)),
-- 商品3: 無縫喀什米爾針織POLO衫 (莫蘭迪綠、海軍藍、灰色)
(3, 'P3-MLD-S-001', '莫蘭迪綠', 'S', 10, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-MLD-M-002', '莫蘭迪綠', 'M', 15, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-MLD-L-003', '莫蘭迪綠', 'L', 12, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-HJL-S-004', '海軍藍', 'S', 8, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-HJL-M-005', '海軍藍', 'M', 12, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-HJL-L-006', '海軍藍', 'L', 10, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-HUI-S-007', '灰色', 'S', 10, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-HUI-M-008', '灰色', 'M', 14, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'P3-HUI-L-009', '灰色', 'L', 11, DATE_SUB(NOW(), INTERVAL 18 DAY)),
-- 商品4: 精紡羊毛寬褲 (深灰、黑色)
(4, 'P4-SHU-S-001', '深灰', 'S', 10, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(4, 'P4-SHU-M-002', '深灰', 'M', 15, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(4, 'P4-SHU-L-003', '深灰', 'L', 12, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(4, 'P4-HEI-S-004', '黑色', 'S', 12, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(4, 'P4-HEI-M-005', '黑色', 'M', 18, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(4, 'P4-HEI-L-006', '黑色', 'L', 14, DATE_SUB(NOW(), INTERVAL 17 DAY)),
-- 商品5: 弧形剪裁錐形牛仔褲 (牛仔深藍色)
(5, 'P5-NZS-28-001', '牛仔深藍色', '28', 8, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(5, 'P5-NZS-30-002', '牛仔深藍色', '30', 12, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(5, 'P5-NZS-32-003', '牛仔深藍色', '32', 15, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(5, 'P5-NZS-34-004', '牛仔深藍色', '34', 10, DATE_SUB(NOW(), INTERVAL 14 DAY)),
-- 商品6: 羊毛法蘭絨長褲 (淺灰色、深灰)
(6, 'P6-QHU-S-001', '淺灰色', 'S', 10, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(6, 'P6-QHU-M-002', '淺灰色', 'M', 15, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(6, 'P6-QHU-L-003', '淺灰色', 'L', 12, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(6, 'P6-SHU-S-004', '深灰', 'S', 8, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(6, 'P6-SHU-M-005', '深灰', 'M', 12, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(6, 'P6-SHU-L-006', '深灰', 'L', 10, DATE_SUB(NOW(), INTERVAL 11 DAY)),
-- 商品7: 美麗諾羊毛西裝外套 (深灰色)
(7, 'P7-SHU-S-001', '深灰色', 'S', 5, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(7, 'P7-SHU-M-002', '深灰色', 'M', 8, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(7, 'P7-SHU-L-003', '深灰色', 'L', 6, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(7, 'P7-SHU-XL-004', '深灰色', 'XL', 4, DATE_SUB(NOW(), INTERVAL 15 DAY)),
-- 商品8: 標準版型棉質拉鍊外套 (淺灰色、深灰、黑色)
(8, 'P8-QHU-S-001', '淺灰色', 'S', 10, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-QHU-M-002', '淺灰色', 'M', 15, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-QHU-L-003', '淺灰色', 'L', 12, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-SHU-S-004', '深灰', 'S', 8, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-SHU-M-005', '深灰', 'M', 12, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-SHU-L-006', '深灰', 'L', 10, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-HEI-S-007', '黑色', 'S', 12, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-HEI-M-008', '黑色', 'M', 18, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 'P8-HEI-L-009', '黑色', 'L', 14, DATE_SUB(NOW(), INTERVAL 13 DAY)),
-- 商品9: CITY皮革郵差包 (黑蘭花) - 配件用 F 尺寸
(9, 'P9-HLH-F-001', '黑蘭花', 'F', 20, DATE_SUB(NOW(), INTERVAL 16 DAY)),
-- 商品10: 針織肩背包 (灰黑相間) - 配件用 F 尺寸
(10, 'P10-HHX-F-001', '灰黑相間', 'F', 25, DATE_SUB(NOW(), INTERVAL 12 DAY)),
-- 商品11: 無框時尚眼鏡 (黑色) - 配件用 F 尺寸
(11, 'P11-HEI-F-001', '黑色', 'F', 30, DATE_SUB(NOW(), INTERVAL 10 DAY)),
-- 商品12: 標準版型桶形燈芯絨長褲 (米色、深灰色、白色)
(12, 'P12-MIS-S-001', '米色', 'S', 10, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-MIS-M-002', '米色', 'M', 15, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-MIS-L-003', '米色', 'L', 12, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-SHU-S-004', '深灰色', 'S', 8, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-SHU-M-005', '深灰色', 'M', 12, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-SHU-L-006', '深灰色', 'L', 10, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-BAI-S-007', '白色', 'S', 10, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-BAI-M-008', '白色', 'M', 14, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 'P12-BAI-L-009', '白色', 'L', 11, DATE_SUB(NOW(), INTERVAL 9 DAY)),
-- 商品13: GORE-TEX WINDSTOPPER 羽絨外套 (黑色、淺灰)
(13, 'P13-HEI-S-001', '黑色', 'S', 5, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 'P13-HEI-M-002', '黑色', 'M', 8, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 'P13-HEI-L-003', '黑色', 'L', 6, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 'P13-HEI-XL-004', '黑色', 'XL', 4, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 'P13-QHU-S-005', '淺灰', 'S', 4, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 'P13-QHU-M-006', '淺灰', 'M', 6, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 'P13-QHU-L-007', '淺灰', 'L', 5, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 'P13-QHU-XL-008', '淺灰', 'XL', 3, DATE_SUB(NOW(), INTERVAL 8 DAY)),
-- 商品14: 復古工裝長版大衣 (黑色、深橘色)
(14, 'P14-HEI-S-001', '黑色', 'S', 4, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'P14-HEI-M-002', '黑色', 'M', 6, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'P14-HEI-L-003', '黑色', 'L', 5, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'P14-HEI-XL-004', '黑色', 'XL', 3, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'P14-SJS-S-005', '深橘色', 'S', 3, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'P14-SJS-M-006', '深橘色', 'M', 5, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'P14-SJS-L-007', '深橘色', 'L', 4, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'P14-SJS-XL-008', '深橘色', 'XL', 2, DATE_SUB(NOW(), INTERVAL 7 DAY)),
-- 商品15: 直筒棉質長褲 (鼴鼠灰褐、深棕色)
(15, 'P15-MOL-S-001', '鼴鼠灰褐', 'S', 5, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 'P15-MOL-M-002', '鼴鼠灰褐', 'M', 7, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 'P15-MOL-L-003', '鼴鼠灰褐', 'L', 6, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 'P15-MOL-XL-004', '鼴鼠灰褐', 'XL', 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 'P15-DBR-S-005', '深棕色', 'S', 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 'P15-DBR-M-006', '深棕色', 'M', 6, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 'P15-DBR-L-007', '深棕色', 'L', 5, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 'P15-DBR-XL-008', '深棕色', 'XL', 3, DATE_SUB(NOW(), INTERVAL 6 DAY)),
-- 商品16: 圍巾細節絎縫內襯夾克 (莫蘭迪綠、淺橘色)
(16, 'P16-MLD-S-001', '莫蘭迪綠', 'S', 3, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 'P16-MLD-M-006', '莫蘭迪綠', 'M', 5, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 'P16-MLD-L-007', '莫蘭迪綠', 'L', 4, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 'P16-MLD-XL-008', '莫蘭迪綠', 'XL', 2, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 'P16-QJS-S-009', '淺橘色', 'S', 3, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 'P16-QJS-M-010', '淺橘色', 'M', 5, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 'P16-QJS-L-011', '淺橘色', 'L', 4, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 'P16-QJS-XL-012', '淺橘色', 'XL', 2, DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- 商品17: 格紋羊駝毛圍巾 (酒紅格紋) - ONE SIZE
(17, 'P17-BGC-OS-001', '酒紅格紋', 'ONE SIZE', 15, DATE_SUB(NOW(), INTERVAL 4 DAY)),
-- 商品18: 雕塑托特包 (酒紅棕) - ONE SIZE
(18, 'P18-BRB-OS-001', '酒紅棕', 'ONE SIZE', 10, DATE_SUB(NOW(), INTERVAL 4 DAY)),
-- 商品19: 雙面羊毛青果領大衣 (深棕海軍藍)
(19, 'P19-DBN-S-001', '深棕海軍藍', 'S', 3, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(19, 'P19-DBN-M-002', '深棕海軍藍', 'M', 5, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(19, 'P19-DBN-L-003', '深棕海軍藍', 'L', 4, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(19, 'P19-DBN-XL-004', '深棕海軍藍', 'XL', 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- 商品20: 麂皮斜背包 (巧克力色) - ONE SIZE
(20, 'P20-CHO-OS-001', '巧克力色', 'ONE SIZE', 12, DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- 商品21: 寬鬆棉質T恤 (白色、莫蘭迪綠、春天黃、月亮橘)
(21, 'P21-WHI-S-001', '白色', 'S', 10, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-WHI-M-002', '白色', 'M', 15, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-WHI-L-003', '白色', 'L', 12, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-WHI-XL-004', '白色', 'XL', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-MLD-S-005', '莫蘭迪綠', 'S', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-MLD-M-006', '莫蘭迪綠', 'M', 12, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-MLD-L-007', '莫蘭迪綠', 'L', 10, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-MLD-XL-008', '莫蘭迪綠', 'XL', 6, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-CTH-S-009', '春天黃', 'S', 6, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-CTH-M-010', '春天黃', 'M', 10, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-CTH-L-011', '春天黃', 'L', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-CTH-XL-012', '春天黃', 'XL', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-YLJ-S-013', '月亮橘', 'S', 6, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-YLJ-M-014', '月亮橘', 'M', 10, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-YLJ-L-015', '月亮橘', 'L', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'P21-YLJ-XL-016', '月亮橘', 'XL', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- 商品22: 寬鬆棉質羅紋長袖T恤 (黑色、莫蘭迪綠、秋天褐色、冬天灰白、淺酒紅)
(22, 'P22-BLK-S-001', '黑色', 'S', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-BLK-M-002', '黑色', 'M', 12, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-BLK-L-003', '黑色', 'L', 10, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-BLK-XL-004', '黑色', 'XL', 6, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-MLD-S-005', '莫蘭迪綠', 'S', 6, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-MLD-M-006', '莫蘭迪綠', 'M', 10, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-MLD-L-007', '莫蘭迪綠', 'L', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-MLD-XL-008', '莫蘭迪綠', 'XL', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QTS-S-009', '秋天褐色', 'S', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QTS-M-010', '秋天褐色', 'M', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QTS-L-011', '秋天褐色', 'L', 7, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QTS-XL-012', '秋天褐色', 'XL', 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-DTH-S-013', '冬天灰白', 'S', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-DTH-M-014', '冬天灰白', 'M', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-DTH-L-015', '冬天灰白', 'L', 7, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-DTH-XL-016', '冬天灰白', 'XL', 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QJH-S-017', '淺酒紅', 'S', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QJH-M-018', '淺酒紅', 'M', 8, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QJH-L-019', '淺酒紅', 'L', 7, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'P22-QJH-XL-020', '淺酒紅', 'XL', 4, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO cart_items (user_id, variant_id, quantity, created_at) VALUES
-- user 2: 修身羊毛針織高領上衣-莫蘭迪綠-M, 雪尼爾寬鬆高領針織衫-黑色-L, 精紡羊毛寬褲-深灰-M
(2, 2, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 12, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 29, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- user 3: 修身羊毛針織高領上衣-酒紅色-M, 無縫喀什米爾針織POLO衫-海軍藍-M, 美麗諾羊毛西裝外套-深灰色-M
(3, 5, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 23, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 45, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- user 4: 修身羊毛針織高領上衣-灰色-L, 雪尼爾寬鬆高領針織衫-莫蘭迪綠-M, 標準版型棉質拉鍊外套-淺灰色-L
(4, 9, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 14, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 50, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO orders (user_id, total_amount, status, payment_method, payment_note, shipping_method, recipient_name, recipient_phone, shipping_address, created_at) VALUES
(2, 1560.00, 'PAID', 'BANK_TRANSFER', '已轉帳，後五碼：12345', 'MAIL', '林凱文', '0923456789', '台北市信義區信義路五段7號', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 1380.00, 'SHIPPED', 'BANK_TRANSFER', NULL, 'STORE', '林凱文', '0923456789', '7-11 信義門市', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 580.00, 'COMPLETED', 'BANK_TRANSFER', NULL, 'MAIL', '林凱文', '0923456789', '台北市大安區敦化南路二段216號', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 2560.00, 'PENDING', 'BANK_TRANSFER', NULL, 'MAIL', '陳怡君', '0934567890', '新北市板橋區文化路一段188巷', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 980.00, 'COMPLETED', 'BANK_TRANSFER', '已轉帳', 'STORE', '陳怡君', '0934567890', '全家 板橋門市', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4, 1960.00, 'PAID', 'BANK_TRANSFER', '已轉帳，後五碼：67890', 'MAIL', '黃大維', '0945678901', '台中市西屯區台灣大道三段99號', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, 3980.00, 'SHIPPED', 'BANK_TRANSFER', NULL, 'STORE', '黃大維', '0945678901', 'OK超商 西屯門市', DATE_SUB(NOW(), INTERVAL 7 DAY));

INSERT INTO order_items (order_id, variant_id, price, quantity) VALUES
-- 訂單1: 修身羊毛針織高領上衣-莫蘭迪綠-M + 雪尼爾寬鬆高領針織衫-黑色-M
(1, 2, 580.00, 1), (1, 11, 980.00, 1),
-- 訂單2: 精紡羊毛寬褲-深灰-M
(2, 29, 1380.00, 1),
-- 訂單3: 修身羊毛針織高領上衣-灰色-S
(3, 7, 580.00, 1),
-- 訂單4: 無縫喀什米爾針織POLO衫-莫蘭迪綠-M + 無縫喀什米爾針織POLO衫-海軍藍-L
(4, 20, 1280.00, 1), (4, 24, 1280.00, 1),
-- 訂單5: 雪尼爾寬鬆高領針織衫-深橘色-M
(5, 17, 980.00, 1),
-- 訂單6: 修身羊毛針織高領上衣-酒紅色-L + 精紡羊毛寬褲-黑色-M
(6, 6, 580.00, 1), (6, 32, 1380.00, 1),
-- 訂單7: 美麗諾羊毛西裝外套-深灰色-L
(7, 46, 3980.00, 1);

INSERT INTO contact_messages (case_number, user_id, name, email, subject, message, status, admin_reply, admin_reply_by, ip_address, created_at, replied_at) VALUES
('CS-0001', 2, '林凱文', 'kevin.lin@choose.com', '商品到貨詢問', '請問商品何時會到貨？', 'CLOSED', '您好，商品預計3-5個工作天內到貨，届時將以簡訊通知您，感謝您的耐心等候！', 1, '192.168.1.100', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
('CS-0002', 3, '陳怡君', 'amy.chen@choose.com', '退換貨政策', '可以退換貨嗎？', 'REPLIED_TRACKING', '您好，本店提供7天鑑賞期，商品未拆封可辦理退換貨。請問您需要退換哪件商品呢？', 1, '192.168.1.101', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
('CS-0003', 4, '黃大維', 'david.huang@choose.com', '商品好評', '商品品質很好，謝謝！', 'CLOSED', '感謝您的肯定！期待您再次光臨 Choose！', 1, '192.168.1.102', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('CS-0004', NULL, '周雅婷', 'tina.chou@choose.com', '顏色詢問', '想詢問是否有其他顏色？', 'PENDING', NULL, NULL, '192.168.1.103', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),
('CS-0005', NULL, '楊志明', 'ming.yang@choose.com', '尺寸選擇', '商品尺寸如何選擇？', 'PENDING', NULL, NULL, '192.168.1.104', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),
('CS-0006', NULL, '許雅雯', 'wendy.hsu@choose.com', '門市詢問', '請問有實體店面嗎？', 'PENDING', NULL, NULL, '192.168.1.105', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL);

SELECT 'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'products', COUNT(*) FROM products
UNION ALL SELECT 'product_variants', COUNT(*) FROM product_variants
UNION ALL SELECT 'cart_items', COUNT(*) FROM cart_items
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL SELECT 'contact_messages', COUNT(*) FROM contact_messages;

SET FOREIGN_KEY_CHECKS = 1;
