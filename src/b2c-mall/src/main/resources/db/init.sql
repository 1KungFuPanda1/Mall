-- ============================================
-- B2C 移动端商城 - 数据库初始化脚本
-- 数据库名称：b2c_mall
-- 字符集：utf8mb4（支持 emoji 等四字节字符）
-- 存储引擎：InnoDB（支持事务、行级锁、外键）
-- ============================================

-- 创建数据库（如果不存在），指定字符集和排序规则
CREATE DATABASE IF NOT EXISTS b2c_mall
    DEFAULT CHARACTER SET utf8mb4 -- 字符集：utf8mb4 支持完整的 Unicode 字符
    DEFAULT COLLATE utf8mb4_general_ci; -- 排序规则：不区分大小写的通用排序

-- 切换到目标数据库
USE b2c_mall;

-- ============================================
-- 1. 用户表（t_user）
-- 存储 C 端注册用户和管理员的基本信息
-- 主键使用雪花算法生成的 BIGINT，手机号为唯一约束
-- ============================================
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT        NOT NULL COMMENT '主键ID(雪花算法生成，MyBatis-Plus ASSIGN_ID)',
    phone       VARCHAR(20)   NOT NULL COMMENT '手机号（唯一，用于登录和短信验证）',
    email       VARCHAR(100)  DEFAULT NULL COMMENT '邮箱（可选，用于邮箱验证码注册）',
    password    VARCHAR(255)  NOT NULL COMMENT '密码（BCrypt 加密存储，不可逆）',
    nickname    VARCHAR(50)   DEFAULT NULL COMMENT '用户昵称（显示名称）',
    avatar      VARCHAR(255)  DEFAULT NULL COMMENT '用户头像图片 URL',
    role        VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT '用户角色: USER=普通用户, ADMIN=管理员',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '账户状态: 1=启用, 0=禁用',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间（记录变更时自动更新）',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0=未删除, 1=已删除',
    PRIMARY KEY (id), -- 主键索引
    UNIQUE KEY uk_phone (phone), -- 手机号唯一索引，防止重复注册
    KEY idx_create_time (create_time) -- 创建时间索引，用于按时间查询用户列表
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（包含普通用户和管理员）';

-- ============================================
-- 2. 收货地址表（t_address）
-- 每个用户可以有多个收货地址，其中最多一个设为默认
-- user_id 与 t_user 存在逻辑外键关系
-- ============================================
CREATE TABLE IF NOT EXISTS t_address (
    id             BIGINT       NOT NULL COMMENT '主键ID(雪花算法)',
    user_id        BIGINT       NOT NULL COMMENT '所属用户ID（逻辑关联 t_user.id）',
    receiver_name  VARCHAR(50)  NOT NULL COMMENT '收货人真实姓名',
    receiver_phone VARCHAR(20)  NOT NULL COMMENT '收货人手机号码',
    province       VARCHAR(50)  NOT NULL COMMENT '省份（如：广东省）',
    city           VARCHAR(50)  NOT NULL COMMENT '城市（如：深圳市）',
    district       VARCHAR(50)  NOT NULL COMMENT '区/县（如：南山区）',
    detail         VARCHAR(255) NOT NULL COMMENT '详细地址（街道、门牌号等）',
    is_default     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认地址: 1=默认, 0=非默认（同一用户只能有一个默认）',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '地址创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '地址更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id), -- 主键索引
    KEY idx_user_id (user_id) -- 用户ID索引，加速某用户所有地址的查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ============================================
-- 3. 商品分类表（t_product_category）
-- 用于管理商品的分类体系，如"手机数码"、"服装鞋帽"等
-- 支持排序和启停控制
-- ============================================
CREATE TABLE IF NOT EXISTS t_product_category (
    id          BIGINT       NOT NULL COMMENT '主键ID(雪花算法)',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称（如"手机数码"）',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序权重（数值越小越靠前显示）',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用（前端可见）, 0=禁用（前端隐藏）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分类创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '分类更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id) -- 主键索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ============================================
-- 4. 商品表（t_product）
-- 存储所有商品信息，包含乐观锁版本号字段用于防超卖
-- 商品图片以逗号分隔的 URL 字符串存储（支持多图）
-- ============================================
CREATE TABLE IF NOT EXISTS t_product (
    id          BIGINT         NOT NULL COMMENT '主键ID(雪花算法)',
    category_id BIGINT         NOT NULL COMMENT '所属分类ID（逻辑关联 t_product_category.id）',
    name        VARCHAR(100)   NOT NULL COMMENT '商品名称',
    price       DECIMAL(10,2)  NOT NULL COMMENT '商品价格（元，保留两位小数）',
    stock       INT            NOT NULL DEFAULT 0 COMMENT '当前库存数量（用于库存扣减和售罄判断）',
    version     INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（每次库存变更时 +1，用于防超卖并发控制）',
    image       VARCHAR(500)   DEFAULT NULL COMMENT '商品图片URL列表（多张图片以逗号分隔）',
    description TEXT           DEFAULT NULL COMMENT '商品图文详情描述（富文本）',
    status      TINYINT        NOT NULL DEFAULT 1 COMMENT '上下架状态: 1=上架（前端可见）, 0=下架（前端不可见）',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商品创建时间',
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '商品最后更新时间',
    deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id), -- 主键索引
    KEY idx_category_id (category_id), -- 分类ID索引，加速按分类筛选商品
    KEY idx_status (status) -- 上架状态索引，加速只查上架商品的查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- ============================================
-- 5. 轮播图表（t_banner）
-- 用于管理首页顶部的轮播 Banner 广告
-- 支持排序权重和跳转链接
-- ============================================
CREATE TABLE IF NOT EXISTS t_banner (
    id          BIGINT       NOT NULL COMMENT '主键ID(雪花算法)',
    image_url   VARCHAR(255) NOT NULL COMMENT '轮播图图片URL地址',
    link_url    VARCHAR(255) DEFAULT NULL COMMENT '点击轮播图后的跳转链接（可为空，表示不跳转）',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序权重（数值越小越靠前显示）',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用（前端展示）, 0=禁用（前端不展示）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '轮播图创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '轮播图更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id) -- 主键索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图管理表';

-- ============================================
-- 6. 订单表（t_order）
-- 存储每一笔购物订单的主信息
-- address_json 保存下单时的收货地址快照（防止地址被删除后订单数据缺失）
-- order_no 为唯一订单编号，用于用户查询
-- ============================================
CREATE TABLE IF NOT EXISTS t_order (
    id            BIGINT         NOT NULL COMMENT '主键ID(雪花算法)',
    order_no      VARCHAR(32)    NOT NULL COMMENT '订单编号（格式: 年月日时分秒 + 6位随机数，如 20260530143021123456）',
    user_id       BIGINT         NOT NULL COMMENT '下单用户ID（逻辑关联 t_user.id）',
    address_json  JSON           NOT NULL COMMENT '收货地址完整快照（JSON格式存储，避免地址被删后订单数据不完整）',
    total_amount  DECIMAL(10,2)  NOT NULL COMMENT '订单总金额（元，为所有商品 price*quantity 之和）',
    status        VARCHAR(20)    NOT NULL DEFAULT 'WAIT_PAY' COMMENT '订单状态: WAIT_PAY=待付款, PAID=已支付, RECEIVING=待收货, COMPLETED=已完成, CANCELLED=已取消',
    pay_time      DATETIME       DEFAULT NULL COMMENT '支付完成时间（用户点击支付时记录）',
    delivery_time DATETIME       DEFAULT NULL COMMENT '发货时间（管理员操作发货时记录）',
    finish_time   DATETIME       DEFAULT NULL COMMENT '订单完成时间（用户确认收货时记录）',
    cancel_time   DATETIME       DEFAULT NULL COMMENT '订单取消时间（用户取消订单时记录）',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间（即下单时间）',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间',
    deleted       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id), -- 主键索引
    UNIQUE KEY uk_order_no (order_no), -- 订单编号唯一索引，防止重复订单号
    KEY idx_user_id (user_id), -- 用户ID索引，加速某用户所有订单查询
    KEY idx_status (status), -- 订单状态索引，加速按状态筛选订单
    KEY idx_create_time (create_time) -- 创建时间索引，用于按时间排序和统计查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- ============================================
-- 7. 订单商品明细表（t_order_item）
-- 存储订单中每个商品的具体信息（商品快照 + 购买数量 + 成交价）
-- 与 t_order 为多对一关系（一个订单包含多个商品明细）
-- ============================================
CREATE TABLE IF NOT EXISTS t_order_item (
    id            BIGINT         NOT NULL COMMENT '主键ID(雪花算法)',
    order_id      BIGINT         NOT NULL COMMENT '所属订单ID（逻辑关联 t_order.id）',
    product_id    BIGINT         NOT NULL COMMENT '商品原始ID（可用于追溯，即使商品已下架）',
    product_name  VARCHAR(100)   NOT NULL COMMENT '下单时商品名称快照（防止商品名被修改后历史订单显示错误）',
    product_image VARCHAR(255)   DEFAULT NULL COMMENT '下单时商品主图快照URL',
    price         DECIMAL(10,2)  NOT NULL COMMENT '下单时商品单价（快照，防止价格调整影响历史订单）',
    quantity      INT            NOT NULL COMMENT '购买数量',
    total_amount  DECIMAL(10,2)  NOT NULL COMMENT '小计金额（计算公式: price * quantity）',
    PRIMARY KEY (id), -- 主键索引
    KEY idx_order_id (order_id) -- 订单ID索引，加速查询某订单下的所有商品明细
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表（快照存储）';

-- ============================================
-- 插入初始化数据
-- ============================================

-- 预置管理员账号（登录账号: admin, 密码: admin123, BCrypt 加密后存储）
INSERT INTO t_user (id, phone, password, nickname, role, status)
VALUES (1, 'admin', '$2a$10$06hIRHoMem65H/B6Lc7g6udoI1hngaIwGBcMl..3F9ePX9rsZxQ1a', '系统管理员', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE phone = phone;

-- 预置商品分类示例数据
INSERT INTO t_product_category (id, name, sort, status) VALUES
(1, '手机数码', 1, 1),
(2, '电脑办公', 2, 1),
(3, '服装鞋帽', 3, 1),
(4, '食品生鲜', 4, 1),
(5, '家用电器', 5, 1)
ON DUPLICATE KEY UPDATE name = name;

-- 预置示例商品数据
INSERT INTO t_product (id, category_id, name, price, stock, version, description, status) VALUES
(1001, 1, '华为 Mate 60 Pro', 6999.00, 100, 0, '<p>旗舰手机，搭载麒麟芯片</p>', 1),
(1002, 1, 'iPhone 15 Pro Max', 9999.00, 80, 0, '<p>苹果最新旗舰，A17 Pro 芯片</p>', 1),
(1003, 2, 'MacBook Pro 14英寸', 14999.00, 50, 0, '<p>M3 Pro 芯片，18GB 内存</p>', 1),
(1004, 2, 'ThinkPad X1 Carbon', 9999.00, 30, 0, '<p>商务轻薄本，i7 处理器</p>', 1),
(1005, 3, '纯棉 T 恤 白色', 99.00, 500, 0, '<p>100% 新疆长绒棉，亲肤透气</p>', 1),
(1006, 3, '休闲运动鞋', 399.00, 200, 0, '<p>轻便舒适，适合日常穿着</p>', 1)
ON DUPLICATE KEY UPDATE name = name;
