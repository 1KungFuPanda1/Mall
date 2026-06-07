# 移动端 B2C 商城 — 系统设计文档

---

## 1. 架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层 (Frontend)                         │
│  ┌──────────────────────┐    ┌──────────────────────────────┐  │
│  │   uni-app 移动端      │    │   Vue 3 + Element Plus 后台  │  │
│  │   (uView Plus)       │    │   (Axios + Vue Router)       │  │
│  └─────────┬────────────┘    └──────────────┬───────────────┘  │
└────────────┼────────────────────────────────┼──────────────────┘
             │          HTTP/RESTful           │
             ▼                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       网关/安全层                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  JWT 拦截器 (AuthInterceptor)                              │  │
│  │  · 校验 Token 有效性  · 解析用户ID和角色  · 角色权限校验      │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     业务层 (Spring Boot)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │ 用户模块  │ │ 商品模块  │ │ 订单模块  │ │ 通用模块  │          │
│  │ User     │ │ Product  │ │ Order    │ │ Common   │          │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘          │
│       │            │            │            │                 │
│       ▼            ▼            ▼            ▼                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   @ControllerAdvice (全局异常处理)          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
│   MySQL 8.0     │ │  Redis 7.x   │ │   本地磁盘        │
│   (Druid连接池)  │ │  (缓存+购物车)│ │   (图片存储)      │
│   MyBatis-Plus  │ │              │ │                  │
└─────────────────┘ └──────────────┘ └──────────────────┘
```

---

## 2. 技术栈明细

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | Spring Boot | 3.x | 基础框架 |
| ORM | MyBatis-Plus | 3.5.x | 数据库操作 + 分页 |
| 数据库 | MySQL | 8.0 | 关系型数据存储 |
| 连接池 | Druid | 1.2.x | 数据库连接池 + SQL 监控 |
| 缓存 | Redis | 7.x | 商品缓存 + 购物车 + 验证码 |
| 认证 | JWT (jjwt) | 0.12.x | Token 签发与校验 |
| 加密 | BCrypt | Spring Security | 密码加密 |
| 短信 | 阿里云 SMS SDK | 最新版 | 注册验证码短信 |
| 文件 | Spring MultipartFile | - | 图片上传 |
| 接口文档 | Knife4j | 4.x | Swagger 增强版 |
| 构建 | Maven | 3.9+ | 依赖管理 & 打包 |
| 前端 C端 | uni-app + uView Plus | 最新 | 移动端跨端开发 |
| 前端 后台 | Vue 3 + Element Plus | 3.x | 管理后台 |
| 工具 | IntelliJ IDEA 2024.2.2 | - | 开发 IDE |
| 调试 | ApiPost | - | 接口调试 |

---

## 3. 项目工程结构

```
b2c-mall/
├── pom.xml
├── src/main/java/com/b2c/mall/
│   ├── MallApplication.java                 # 启动类
│   ├── controller/                          # 控制器层
│   │   ├── user/
│   │   │   ├── UserController.java          # C端用户接口
│   │   │   └── AddressController.java       # 收货地址接口
│   │   ├── product/
│   │   │   └── ProductController.java       # 商品浏览接口
│   │   ├── cart/
│   │   │   └── CartController.java          # 购物车接口
│   │   ├── order/
│   │   │   └── OrderController.java         # 订单接口
│   │   ├── admin/
│   │   │   ├── AdminAuthController.java     # 管理员登录
│   │   │   ├── AdminProductController.java  # 商品管理
│   │   │   ├── AdminCategoryController.java # 分类管理
│   │   │   ├── AdminOrderController.java    # 订单管理
│   │   │   └── AdminUserController.java     # 用户管理
│   │   └── common/
│   │       ├── UploadController.java        # 文件上传
│   │       └── BannerController.java        # 轮播图
│   ├── service/                             # 业务逻辑层
│   │   ├── impl/                            # 实现类
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   ├── CartService.java
│   │   ├── OrderService.java
│   │   ├── AddressService.java
│   │   ├── CategoryService.java
│   │   ├── BannerService.java
│   │   └── SmsService.java                 # 阿里云短信
│   ├── mapper/                              # MyBatis-Plus Mapper
│   │   ├── UserMapper.java
│   │   ├── ProductMapper.java
│   │   ├── OrderMapper.java
│   │   ├── OrderItemMapper.java
│   │   ├── AddressMapper.java
│   │   ├── CategoryMapper.java
│   │   └── BannerMapper.java
│   ├── entity/                              # 实体类
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Address.java
│   │   ├── Category.java
│   │   └── Banner.java
│   ├── dto/                                 # 请求/响应 DTO
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── PlaceOrderRequest.java
│   │   │   └── ...
│   │   └── response/
│   │       ├── LoginResponse.java
│   │       ├── ProductVO.java
│   │       └── ...
│   ├── config/                              # 配置类
│   │   ├── WebMvcConfig.java               # 拦截器注册 + 静态资源映射
│   │   ├── RedisConfig.java                # Redis 序列化配置
│   │   ├── DruidConfig.java                # Druid 监控配置
│   │   └── Knife4jConfig.java              # 接口文档配置
│   ├── interceptor/
│   │   └── JwtInterceptor.java             # JWT 拦截器
│   ├── exception/
│   │   ├── BusinessException.java           # 业务异常
│   │   └── GlobalExceptionHandler.java     # 全局异常处理
│   ├── util/
│   │   ├── JwtUtil.java                    # JWT 工具类
│   │   └── RedisUtil.java                  # Redis 工具类
│   └── enums/
│       ├── OrderStatusEnum.java            # 订单状态枚举
│       └── RoleEnum.java                   # 用户角色枚举
├── src/main/resources/
│   ├── application.yml                     # 主配置
│   ├── application-dev.yml                 # 开发环境
│   └── db/
│       └── init.sql                        # 建表 DDL
└── uploads/                                # 图片上传目录
```

---

## 4. 数据库设计

### 4.1 ER 图（实体关系）

```
┌──────────────┐       ┌──────────────────┐
│   t_user     │       │   t_address      │
│──────────────│       │──────────────────│
│ id (PK)      │──┐    │ id (PK)          │
│ phone        │  │    │ user_id (FK) ────│──┐  ← 一个用户多个地址
│ email        │  │    │ receiver_name    │  │
│ password     │  │    │ receiver_phone   │  │
│ nickname     │  │    │ province         │  │
│ avatar       │  │    │ city             │  │
│ role         │  │    │ district         │  │
│ status       │  │    │ detail           │  │
│ deleted      │  │    │ is_default       │  │
└──────┬───────┘  │    └──────────────────┘  │
       │          └──────────────────────────┘
       │
       │          ┌──────────────────┐
       │          │   t_order        │
       ├──────────│──────────────────│
       │          │ id (PK)          │
       │          │ user_id (FK) ────│──┐  ← 一个用户多个订单
       │          │ order_no (UK)    │  │
       │          │ address_json     │  │
       │          │ total_amount     │  │
       │          │ status           │  │
       │          │ pay_time         │  │
       │          │ delivery_time    │  │
       │          │ finish_time      │  │
       │          │ deleted          │  │
       │          └────────┬─────────┘  │
       │                   │            │
       │                   │ 1:N        │
       │                   ▼            │
       │          ┌──────────────────┐  │
       │          │  t_order_item    │  │
       │          │──────────────────│  │
       │          │ id (PK)          │  │
       │          │ order_id (FK) ───│──┘
       │          │ product_id       │
       │          │ product_name     │
       │          │ product_image    │
       │          │ price            │
       │          │ quantity         │
       │          └──────────────────┘
       │
       │          ┌──────────────────┐
       │          │ t_product        │
       │          │──────────────────│
       │          │ id (PK)          │
       │          │ category_id (FK)─│──┐  ← 属于某个分类
       │          │ name             │  │
       │          │ price            │  │
       │          │ stock            │  │
       │          │ version          │  │  ← 乐观锁字段
       │          │ image            │  │
       │          │ description      │  │
       │          │ status           │  │
       │          │ deleted          │  │
       │          └──────────────────┘  │
       │                                │
       │          ┌──────────────────┐  │
       │          │t_product_category│  │
       │          │──────────────────│  │
       │          │ id (PK) ◄────────│──┘
       │          │ name             │
       │          │ sort             │
       │          │ status           │
       │          │ deleted          │
       │          └──────────────────┘
       │
       │          ┌──────────────────┐
       │          │   t_banner       │
       │          │──────────────────│
       │          │ id (PK)          │
       │          │ image_url        │
       │          │ link_url         │
       │          │ sort             │
       │          │ status           │
       │          └──────────────────┘
       │
       └────────── 注意：购物车数据 → 只存 Redis Hash，不建 MySQL 表
                  验证码数据 → 只存 Redis String，不建 MySQL 表
```

### 4.2 表结构 DDL

```sql
-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE t_user (
    id          BIGINT        NOT NULL COMMENT '主键ID(雪花算法)',
    phone       VARCHAR(20)   NOT NULL COMMENT '手机号',
    email       VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    password    VARCHAR(255)  NOT NULL COMMENT '密码(BCrypt)',
    nickname    VARCHAR(50)   DEFAULT NULL COMMENT '昵称',
    avatar      VARCHAR(255)  DEFAULT NULL COMMENT '头像URL',
    role        VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT '角色: USER=普通用户, ADMIN=管理员',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 收货地址表
-- ============================================
CREATE TABLE t_address (
    id             BIGINT       NOT NULL COMMENT '主键ID(雪花算法)',
    user_id        BIGINT       NOT NULL COMMENT '用户ID',
    receiver_name  VARCHAR(50)  NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20)  NOT NULL COMMENT '收货人手机号',
    province       VARCHAR(50)  NOT NULL COMMENT '省',
    city           VARCHAR(50)  NOT NULL COMMENT '市',
    district       VARCHAR(50)  NOT NULL COMMENT '区',
    detail         VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认: 1=是, 0=否',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ============================================
-- 3. 商品分类表
-- ============================================
CREATE TABLE t_product_category (
    id          BIGINT       NOT NULL COMMENT '主键ID(雪花算法)',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序(越小越靠前)',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ============================================
-- 4. 商品表
-- ============================================
CREATE TABLE t_product (
    id          BIGINT         NOT NULL COMMENT '主键ID(雪花算法)',
    category_id BIGINT         NOT NULL COMMENT '分类ID',
    name        VARCHAR(100)   NOT NULL COMMENT '商品名称',
    price       DECIMAL(10,2)  NOT NULL COMMENT '价格',
    stock       INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    version     INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    image       VARCHAR(500)   DEFAULT NULL COMMENT '商品图片(多张逗号分隔)',
    description TEXT           DEFAULT NULL COMMENT '商品描述',
    status      TINYINT        NOT NULL DEFAULT 1 COMMENT '状态: 1=上架, 0=下架',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================
-- 5. 轮播图表
-- ============================================
CREATE TABLE t_banner (
    id          BIGINT       NOT NULL COMMENT '主键ID(雪花算法)',
    image_url   VARCHAR(255) NOT NULL COMMENT '图片URL',
    link_url    VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序(越小越靠前)',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- ============================================
-- 6. 订单表
-- ============================================
CREATE TABLE t_order (
    id            BIGINT         NOT NULL COMMENT '主键ID(雪花算法)',
    order_no      VARCHAR(32)    NOT NULL COMMENT '订单编号(时间戳+随机数)',
    user_id       BIGINT         NOT NULL COMMENT '用户ID',
    address_json  JSON           NOT NULL COMMENT '收货地址快照(JSON)',
    total_amount  DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    status        VARCHAR(20)    NOT NULL DEFAULT 'WAIT_PAY' COMMENT '订单状态: WAIT_PAY/PAID/DELIVERING/RECEIVING/COMPLETED/CANCELLED',
    pay_time      DATETIME       DEFAULT NULL COMMENT '支付时间',
    delivery_time DATETIME       DEFAULT NULL COMMENT '发货时间',
    finish_time   DATETIME       DEFAULT NULL COMMENT '完成时间',
    cancel_time   DATETIME       DEFAULT NULL COMMENT '取消时间',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 7. 订单商品明细表
-- ============================================
CREATE TABLE t_order_item (
    id            BIGINT         NOT NULL COMMENT '主键ID(雪花算法)',
    order_id      BIGINT         NOT NULL COMMENT '订单ID',
    product_id    BIGINT         NOT NULL COMMENT '商品ID',
    product_name  VARCHAR(100)   NOT NULL COMMENT '商品名称(快照)',
    product_image VARCHAR(255)   DEFAULT NULL COMMENT '商品图片(快照)',
    price         DECIMAL(10,2)  NOT NULL COMMENT '购买时单价',
    quantity      INT            NOT NULL COMMENT '购买数量',
    total_amount  DECIMAL(10,2)  NOT NULL COMMENT '小计(price * quantity)',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';
```

### 4.3 Redis 数据结构设计

| Key | 类型 | 说明 | 示例 |
|-----|------|------|------|
| `sms_code:{phone}` | String | 短信验证码 | `"123456"`，TTL = 300s |
| `email_code:{email}` | String | 邮箱验证码 | `"654321"`，TTL = 300s |
| `token:user:{userId}` | String | 登录 Token | `"eyJhbG..."`，TTL = 86400s |
| `product:hot:list` | String(JSON) | 首页热门商品缓存 | `"[{...},{...}]"`，TTL = 1800s |
| `product:detail:{id}` | String(JSON) | 商品详情缓存 | `"{...}"`，TTL = 1800s |
| `cart:{userId}` | Hash | 购物车 | field=productId, value=`{"name":"xxx","price":99,"quantity":2,"image":"/xxx.jpg"}` |

---

## 5. 接口设计（API 文档概要）

### 5.1 接口规范

| 规范项 | 约定 |
|--------|------|
| HTTP 状态码 | 200 成功 / 201 创建成功 / 400 参数错误 / 401 未登录 / 403 无权限 / 404 不存在 / 409 冲突 / 500 服务错误 |
| 响应体 | 直接返回数据对象或错误信息 |
| 分页请求 | `?page=1&pageSize=10` |
| 分页响应 | `{ "total": 100, "records": [...] }` |
| 认证方式 | Header: `Authorization: Bearer <token>` |

### 5.2 C端接口清单（uni-app 调用）

#### 用户模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/user/register` | 用户注册 | 否 |
| POST | `/api/user/send-sms` | 发送短信验证码 | 否 |
| POST | `/api/user/login` | 用户登录 | 否 |
| GET | `/api/user/info` | 获取当前用户信息 | 是 |
| PUT | `/api/user/profile` | 修改个人资料 | 是 |

#### 收货地址

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/addresses` | 地址列表 | 是 |
| POST | `/api/addresses` | 新增地址 | 是 |
| PUT | `/api/addresses/{id}` | 修改地址 | 是 |
| DELETE | `/api/addresses/{id}` | 删除地址 | 是 |
| PUT | `/api/addresses/{id}/default` | 设为默认 | 是 |

#### 商品浏览

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/products` | 商品列表（分页+分类筛选） | 否 |
| GET | `/api/products/{id}` | 商品详情 | 否 |
| GET | `/api/products/search` | 商品搜索（?keyword=xxx） | 否 |

#### 购物车

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/cart` | 获取购物车列表 | 是 |
| POST | `/api/cart` | 添加商品到购物车 | 是 |
| PUT | `/api/cart/{productId}` | 修改购物车商品数量 | 是 |
| DELETE | `/api/cart/{productId}` | 删除购物车商品 | 是 |
| PUT | `/api/cart/check` | 选中/取消选中商品 | 是 |

#### 订单

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/orders` | 提交订单（下单） | 是 |
| GET | `/api/orders` | 订单列表（?status=xxx） | 是 |
| GET | `/api/orders/{id}` | 订单详情 | 是 |
| PUT | `/api/orders/{id}/pay` | 模拟支付 | 是 |
| PUT | `/api/orders/{id}/cancel` | 取消订单 | 是 |
| PUT | `/api/orders/{id}/confirm` | 确认收货 | 是 |

#### 公共接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/banners` | 轮播图列表 | 否 |
| GET | `/api/categories` | 商品分类列表 | 否 |

### 5.3 后台管理接口清单（Vue 管理端调用）

#### 管理员认证

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/admin/login` | 管理员登录 | 否 |

#### 分类管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/admin/categories` | 分类列表 | 管理员 |
| POST | `/admin/categories` | 新增分类 | 管理员 |
| PUT | `/admin/categories/{id}` | 修改分类 | 管理员 |
| DELETE | `/admin/categories/{id}` | 删除分类 | 管理员 |

#### 商品管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/admin/products` | 商品列表（分页） | 管理员 |
| POST | `/admin/products` | 新增商品 | 管理员 |
| PUT | `/admin/products/{id}` | 修改商品 | 管理员 |
| DELETE | `/admin/products/{id}` | 删除商品 | 管理员 |
| PUT | `/admin/products/{id}/status` | 上下架 | 管理员 |

#### 订单管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/admin/orders` | 订单列表（分页+状态筛选） | 管理员 |
| GET | `/admin/orders/{id}` | 订单详情 | 管理员 |
| PUT | `/admin/orders/{id}/deliver` | 发货 | 管理员 |

#### 用户管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/admin/users` | 用户列表（分页） | 管理员 |
| PUT | `/admin/users/{id}/status` | 启用/禁用 | 管理员 |

#### 轮播图管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/admin/banners` | 轮播图列表 | 管理员 |
| POST | `/admin/banners` | 新增轮播图 | 管理员 |
| PUT | `/admin/banners/{id}` | 修改轮播图 | 管理员 |
| DELETE | `/admin/banners/{id}` | 删除轮播图 | 管理员 |

#### 数据统计

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/admin/statistics/today` | 今日订单数+营业额 | 管理员 |
| GET | `/admin/statistics/weekly` | 近7日订单趋势 | 管理员 |

#### 文件上传

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/admin/upload` | 图片上传 | 管理员 |

---

## 6. 关键业务设计详解

### 6.1 乐观锁库存扣减

```
SQL 语句:
UPDATE t_product
SET stock = stock - #{quantity},
    version = version + 1
WHERE id = #{productId}
  AND version = #{currentVersion}
  AND stock >= #{quantity}

MyBatis-Plus 实现:
product.setStock(product.getStock() - quantity);
int rows = productMapper.update(product, 
    new LambdaUpdateWrapper<Product>()
        .eq(Product::getId, productId)
        .eq(Product::getVersion, currentVersion)
        .ge(Product::getStock, quantity));
if (rows == 0) throw new BusinessException("下单失败，库存不足或冲突，请重试");
```

### 6.2 购物车 Redis Hash 设计

```
Key:    cart:{userId}
Field:  {productId}
Value:  {"productId":1,"name":"商品名","image":"/xxx.jpg","price":99.00,"quantity":2,"checked":true}

操作流程:
· 加入购物车 → HSET cart:1001 101 '{"productId":101,"name":"xxx",...}'
· 修改数量   → 读取 → 修改 quantity → HSET 覆盖
· 删除商品   → HDEL cart:1001 101
· 获取全部   → HGETALL cart:1001
· 下单后     → HDEL 选中的商品（事务中与订单创建一起）
```

### 6.3 商品缓存一致性策略

```
┌─────────┐
│ 读请求   │
└────┬────┘
     ▼
  查 Redis ──→ 命中 → 返回缓存数据
     │
     │ 未命中
     ▼
  查 MySQL ──→ 写入 Redis (TTL 30min) → 返回
     
┌─────────┐
│ 写请求   │  (管理员更新商品)
└────┬────┘
     ▼
  更新 MySQL
     │
     ▼
  删除 Redis 缓存  (等下次读取时重新加载)
```

### 6.4 订单状态机

```
                            ┌────────────────┐
                            │    用户取消     │
                            │  (仅此状态可取消) │
                            └───────┬────────┘
                                    ▼
WAIT_PAY ──支付──▶ PAID ──发货──▶ RECEIVING ──确认收货──▶ COMPLETED
 待付款            已支付           待收货                      已完成
                                    
* 取消后状态变为 CANCELLED，库存回滚
```

---

## 7. 设计决策汇总表

| 编号 | 决策项 | 最终方案 |
|------|--------|---------|
| D01 | 构建工具 | Maven |
| D02 | 包结构 | 按技术分层 (controller/service/mapper/entity/config) |
| D03 | 数据库连接池 | Druid |
| D04 | 图片存储 | 本地磁盘 + 虚拟路径映射 |
| D05 | 管理后台 UI | Element Plus |
| D06 | 移动端 UI | uView Plus |
| D07 | 接口文档 | Knife4j (Swagger 增强) |
| D08 | 表命名 | t_ 前缀 |
| D09 | 主键策略 | 雪花算法 (MyBatis-Plus ASSIGN_ID) |
| D10 | 订单号生成 | 时间戳 + 随机数 |
| D11 | 商品规格 | 不做多 SKU |
| D12 | 删除策略 | 逻辑删除 (@TableLogic) |
| D13 | 轮播图 | 单独 t_banner 表 |
| D14 | HTTP 响应格式 | RESTful 标准 HTTP 状态码 |
| D15 | 分页参数 | page + pageSize |
| D16 | 分页响应 | total + records |
| D17 | API 路径风格 | RESTful |
| D18 | 接口权限 | C端用户 vs 管理员 |
| D19 | 注册验证码 | 阿里云真实短信 |
| D20 | 库存扣减 | 下单时扣减 + 乐观锁 |
| D21 | 购物车存储 | Redis Hash |

---

> 📌 **下一步**：系统设计确认后，进入「编码开发阶段」。你可以让我：
> - **A**：生成 Spring Boot 项目脚手架代码（pom.xml + application.yml + 启动类 + 公共组件）
> - **B**：按照模块逐步生成代码（先用户模块 → 商品模块 → 订单模块...）
> - **C**：先把简历更新了（加入这个新项目）
