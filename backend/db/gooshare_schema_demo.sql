-- GooShare 最小可运行数据库结构与演示数据
-- 在已创建的 gooshare 数据库中执行本文件
USE gooshare;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS coupon_order, coupon_item_rel, coupon, orders, comment, item_like, item,
  seller, brand, category_item, category, user;

CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  phone VARCHAR(20) NOT NULL UNIQUE,
  avatar VARCHAR(500),
  role BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE seller (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  avatar VARCHAR(500),
  location VARCHAR(128),
  UNIQUE KEY uk_seller_user (user_id),
  CONSTRAINT fk_seller_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL UNIQUE,
  icon VARCHAR(255),
  sort INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  item_name VARCHAR(64) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_category_item (category_id, item_name),
  CONSTRAINT fk_category_item_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE brand (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE item (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  price INT NOT NULL,
  stock BIGINT NOT NULL DEFAULT 1,
  image_url VARCHAR(500),
  description TEXT,
  like_count INT NOT NULL DEFAULT 0,
  collect_count INT NOT NULL DEFAULT 0,
  browser_count INT NOT NULL DEFAULT 0,
  brand_id BIGINT,
  category_id BIGINT,
  category_item_id BIGINT,
  seller_id INT,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_item_search (status, category_id, category_item_id, brand_id),
  CONSTRAINT fk_item_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
  CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_item_category_item FOREIGN KEY (category_item_id) REFERENCES category_item(id),
  CONSTRAINT fk_item_seller FOREIGN KEY (seller_id) REFERENCES seller(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE item_like (
  user_id BIGINT NOT NULL,
  item_id INT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, item_id),
  CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_like_item FOREIGN KEY (item_id) REFERENCES item(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_id INT NOT NULL,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(64),
  user_avatar VARCHAR(500),
  content VARCHAR(1000) NOT NULL,
  is_seller BOOLEAN NOT NULL DEFAULT FALSE,
  parent_id BIGINT NOT NULL DEFAULT 0,
  reply_user_id BIGINT,
  reply_user_name VARCHAR(64),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_comment_item_parent (item_id, parent_id),
  CONSTRAINT fk_comment_item FOREIGN KEY (item_id) REFERENCES item(id),
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
  id VARCHAR(40) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  item_id INT NOT NULL,
  seller_id INT,
  price INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  pay_time DATETIME,
  finish_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE coupon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  coupon_type BIGINT NOT NULL DEFAULT 1,
  value BIGINT NOT NULL DEFAULT 0,
  min_threshold BIGINT NOT NULL DEFAULT 0,
  total_stock BIGINT NOT NULL DEFAULT 0,
  remain_stock BIGINT NOT NULL DEFAULT 0,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE coupon_item_rel (
  coupon_id BIGINT NOT NULL,
  item_id INT NOT NULL,
  PRIMARY KEY (coupon_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE coupon_order (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  coupon_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 测试用户：密码为 123456；验证码登录可使用手机号 13800138000
INSERT INTO user (id, username, password, phone, avatar, role) VALUES
  (1, 'demo', '123456', '13800138000', 'https://picsum.photos/seed/demo/120', 0),
  (2, 'seller1', '123456', '13900139000', 'https://picsum.photos/seed/seller/120', 0);

INSERT INTO seller (id, user_id, username, nickname, avatar, location) VALUES
  (1, 2, 'seller1', '校园卖家', 'https://picsum.photos/seed/seller/120', '大学城');

INSERT INTO category (id, name, icon, sort) VALUES
  (1, '数码电子', 'icon-digital', 1),
  (2, '学习办公', 'icon-study', 2),
  (3, '生活用品', 'icon-life', 3);

INSERT INTO category_item (id, category_id, item_name, sort) VALUES
  (1, 1, '手机', 1), (2, 1, '耳机', 2), (3, 1, '笔记本电脑', 3),
  (4, 2, '教材', 1), (5, 2, '办公用品', 2), (6, 3, '家具', 1);

INSERT INTO brand (id, name) VALUES (1, '苹果'), (2, '小米'), (3, '联想');

INSERT INTO item (id, title, price, stock, image_url, description, brand_id, category_id, category_item_id, seller_id, status) VALUES
  (1, '九成新 iPhone 13', 2899, 1, 'https://picsum.photos/seed/iphone13/600/400', '个人自用，功能正常，支持当面验机。', 1, 1, 1, 1, 1),
  (2, '小米蓝牙降噪耳机', 199, 5, 'https://picsum.photos/seed/earphone/600/400', '降噪效果良好，配件齐全。', 2, 1, 2, 1, 1),
  (3, '大学英语教材一套', 80, 3, 'https://picsum.photos/seed/books/600/400', '适合大一课程，少量笔记。', NULL, 2, 4, 1, 1),
  (4, '联想轻薄笔记本', 2399, 1, 'https://picsum.photos/seed/laptop/600/400', '适合学习办公，电池状态良好。', 3, 1, 3, 1, 1),
  (5, '宿舍收纳置物架', 35, 8, 'https://picsum.photos/seed/storage/600/400', '宿舍闲置，结实耐用。', NULL, 3, 6, 1, 1);

INSERT INTO comment (item_id, user_id, user_name, user_avatar, content, is_seller) VALUES
  (1, 1, 'demo', 'https://picsum.photos/seed/demo/120', '请问可以当面验机吗？', FALSE),
  (1, 2, 'seller1', 'https://picsum.photos/seed/seller/120', '可以，校内面交。', TRUE);

SET FOREIGN_KEY_CHECKS = 1;
