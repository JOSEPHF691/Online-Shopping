-- ============================================================
-- CS黑市商城 - 数据库初始化脚本
-- 数据库: product | 用户: shop | 密码: shop123
-- 使用方法: mysql -h 127.0.0.1 -u shop -pshop123 product < init.sql
-- ============================================================

-- ============================================================
-- 第一部分: 原有表结构 (从Entity反推, 与MyBatis-Plus实体对应)
-- ============================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50),
    role INT DEFAULT 0 COMMENT '0=Customer, 1=Sales, 2=Admin',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    email VARCHAR(100),
    region VARCHAR(50) DEFAULT '未知',
    last_login_ip VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 商品表
CREATE TABLE IF NOT EXISTS `product` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    status INT DEFAULT 1 COMMENT '1=上架, 0=下架',
    image VARCHAR(255),
    stock INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    count INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    count INT DEFAULT 1,
    total_amount DECIMAL(10,2),
    status INT DEFAULT 1 COMMENT '1=待付款, 2=已支付, 3=已发货',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 第二部分: 新增表 (满足PDF 3.2大数据采集要求)
-- ============================================================

-- 5. 商品分类表 (Sales角色管理商品目录)
CREATE TABLE IF NOT EXISTS `category` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 用户浏览日志表 (PDF要求: 记录浏览行为 - 商品类别、停留时长)
CREATE TABLE IF NOT EXISTS `browse_log` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT,
    category VARCHAR(100),
    duration_seconds INT DEFAULT 0 COMMENT '停留时长(秒)',
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_browse_user (user_id),
    INDEX idx_browse_product (product_id),
    INDEX idx_browse_category (category),
    INDEX idx_browse_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 用户登录日志表 (PDF要求: 记录登录时间、IP地址)
CREATE TABLE IF NOT EXISTS `login_log` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    ip VARCHAR(50),
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    role INT COMMENT '登录时的角色',
    INDEX idx_login_user (user_id),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 操作日志表 (PDF要求: 记录Sales/Admin操作时间、内容、IP、账号)
CREATE TABLE IF NOT EXISTS `operation_log` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    operation VARCHAR(500) COMMENT '操作内容描述',
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_op_operator (operator_id),
    INDEX idx_op_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 第三部分: 种子数据 (CS2武器皮肤主题)
-- ============================================================

-- 默认账号
-- role: 0=Customer, 1=Sales, 2=Admin
-- 管理员: admin / admin123
INSERT INTO `user` (username, password, name, role, email, region) VALUES
('admin',    'admin123',  '系统管理员',   2, 'admin@csshop.com',   '上海'),
('seller1',  'seller123', '销售员-小王',  1, 'seller1@csshop.com', '北京'),
('seller2',  'seller123', '销售员-小李',  1, 'seller2@csshop.com', '深圳'),
('player1',  '123456',    'CS玩家-小明',  0, 'player1@test.com',   '广东'),
('player2',  '123456',    'CS玩家-小红',  0, 'player2@test.com',   '北京'),
('player3',  '123456',    'CS玩家-大刘',  0, 'player3@test.com',   '四川'),
('player4',  '123456',    'CS玩家-阿强',  0, 'player4@test.com',   '上海');

-- 商品分类
INSERT INTO `category` (name) VALUES
('手枪'),
('步枪'),
('狙击枪'),
('冲锋枪'),
('装备');

-- CS2 商品数据
INSERT INTO `product` (name, category, price, description, status, image, stock) VALUES
('格洛克18型 | 水灵',            '手枪',   150.00,  'Glock-18 | Water Elemental — 久经沙场，水蓝涂装',              1, 'glock.png',     50),
('沙漠之鹰 | 深红之网',          '手枪',   320.00,  'Desert Eagle | Crimson Web — 略有磨损，红色蛛网纹路',            1, 'deagle.png',    30),
('USP-S | 击杀确认',             '手枪',   280.00,  'USP-S | Kill Confirmed — 崭新出厂，弹孔计数涂装',                1, 'usps.png',      35),
('AK-47 | 红线',                 '步枪',   890.00,  'AK-47 | Redline — 久经沙场，黑红碳纤维经典',                     1, 'ak47.png',      25),
('M4A4 | 喧嚣杀戮',              '步枪',   1200.00, 'M4A4 | Howl — 略有磨损，传说级红色咆哮狼头',                     1, 'm4a4.png',      10),
('M4A1-S | 金蛇缠绕',            '步枪',   650.00,  'M4A1-S | Golden Coil — 崭新出厂，金色蛇纹消音器',               1, 'm4a1s.png',     20),
('AWP | 巨龙传说',               '狙击枪', 2500.00, 'AWP | Dragon Lore — 久经沙场，骑士巨龙金色浮雕',                1, 'awp_dlore.png', 5),
('AWP | 二西莫夫',               '狙击枪', 1800.00, 'AWP | Asiimov — 略有磨损，科幻白橙几何涂装',                     1, 'awp_asii.png',  8),
('SSG 08 | 血溅运动',            '狙击枪', 420.00,  'SSG 08 | Blood in the Water — 崭新出厂，鲨鱼血红色涂装',         1, 'scout.png',     15),
('MP9 | 玫瑰铁锈',               '冲锋枪', 80.00,   'MP9 | Rose Iron — 崭新出厂，铁锈玫瑰花纹',                       1, 'mp9.png',       60),
('MAC-10 | 霓虹革命',            '冲锋枪', 200.00,  'MAC-10 | Neon Revolution — 略有磨损，赛博霓虹涂鸦',              1, 'mac10.png',     40),
('P90 | 二西莫夫',               '冲锋枪', 350.00,  'P90 | Asiimov — 略有磨损，白橙科幻风格',                          1, 'p90.png',       30),
('蝴蝶刀 | 渐变大理石',          '装备',   3500.00, '★ Butterfly Knife | Marble Fade — 崭新出厂，红蓝渐变刀身',    1, 'butterfly.png', 3),
('爪子刀 | 多普勒',              '装备',   2800.00, '★ Karambit | Doppler — 崭新出厂，紫粉相位多普勒',              1, 'karambit.png',  2),
('运动手套 | 潘多拉之盒',        '装备',   4200.00, '★ Sport Gloves | Pandora Box — 久经沙场，稀有紫色',          1, 'gloves.png',    1),
('M9刺刀 | 深红之网',            '装备',   1800.00, '★ M9 Bayonet | Crimson Web — 略有磨损，血色蛛网刀身',          1, 'm9.png',        4),
('AK-47 | 火蛇',                 '步枪',   3200.00, 'AK-47 | Fire Serpent — 久经沙场，经典火焰蛇纹',                 1, 'ak47_fs.png',   3),
('AWP | 王子',                   '狙击枪', 4500.00, 'AWP | Prince — 崭新出厂，稀有收藏品级',                          1, 'awp_prince.png',1);
