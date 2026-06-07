# B2C 移动端商城 — 系统设计文档

## 文档信息

| 项目 | 说明 |
|------|------|
| 项目名称 | B2C 移动端商城 |
| 版本号 | v1.0.0 |
| 文档类型 | 系统设计文档 |
| 最后更新 | 2026-05-30 |

---

## 目录

1. [前台界面设计](#1-前台界面设计)
2. [后台架构设计](#2-后台架构设计)
3. [数据库设计](#3-数据库设计)
4. [API 接口设计](#4-api-接口设计)

---

## 1. 前台界面设计

### 1.1 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | uni-app (Vue 3) | 3.x | 基于 Vue 3 Composition API 的跨端开发框架 |
| UI 库 | uView Plus | 3.3.49 | 专为 uni-app 打造的 Vue 3 组件库 |
| 状态管理 | Pinia | 2.1.7 | Vue 官方推荐的状态管理库 |
| 构建工具 | Vite (HBuilderX 内置) | 5.x | 极速开发服务器与构建工具 |
| CSS 预处理 | SCSS | — | HBuilderX 内置 sass 编译器 |

### 1.2 页面架构

```
b2c-mall-app/
├── pages/
│   ├── index/          ── 首页（轮播图 + 分类 + 商品网格）
│   ├── login/          ── 登录页
│   ├── register/       ── 注册页
│   ├── product/
│   │   ├── list/       ── 商品列表（按分类筛选）
│   │   ├── search/     ── 商品搜索（模糊匹配）
│   │   └── detail/     ── 商品详情（加入购物车）
│   ├── cart/           ── 购物车（管理 + 结算）
│   ├── order/
│   │   ├── confirm/    ── 确认订单（地址 + 提单）
│   │   ├── list/       ── 订单列表（Tab 状态筛选）
│   │   ├── detail/     ── 订单详情
│   │   └── pay-success/── 支付成功
│   ├── address/
│   │   ├── list/       ── 地址列表（CRUD）
│   │   └── edit/       ── 新增/编辑地址
│   └── user/           ── 个人中心
├── store/
│   ├── user.js         ── 用户 Store（登录态管理）
│   └── cart.js         ── 购物车 Store（状态管理）
├── services/
│   └── index.js        ── 全部 C 端 API 接口（22 个）
└── utils/
    └── request.js      ── HTTP 请求封装（拦截器）
```

### 1.3 用户交互流程

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  游客浏览  │ → │  用户注册  │ → │  用户登录  │ → │  浏览商品  │
│  商品首页  │   │   / 登录   │   │  (JWT)   │   │  首页/搜索  │
└──────────┘    └──────────┘    └──────────┘    └─────┬────┘
                                                      │
      ┌───────────────────────────────────────────────┘
      ▼
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  查看详情  │ → │ 加入购物车 │ → │  确认订单  │ → │  提交订单  │
│  商品详情  │   │  (Redis)  │   │  选地址    │   │  扣库存    │
└──────────┘    └──────────┘    └──────────┘    └─────┬────┘
                                                      │
      ┌───────────────────────────────────────────────┘
      ▼
┌──────────┐    ┌──────────┐    ┌──────────┐
│  模拟支付  │ → │  待收货    │ → │  确认收货  │
│  改状态    │   │           │   │  已完成    │
└──────────┘    └──────────┘    └──────────┘
```

### 1.4 TabBar 导航

| Tab | 图标 | 页面路径 | 说明 |
|-----|------|----------|------|
| 首页 | `home` / `home-active` | `/pages/index/index` | 商品浏览入口 |
| 购物车 | `cart` / `cart-active` | `/pages/cart/cart` | 购物车管理 |
| 订单 | `order` / `order-active` | `/pages/order/list` | 订单列表 |
| 我的 | `user` / `user-active` | `/pages/user/user` | 个人中心 |

### 1.5 组件设计

#### 1.5.1 页面组件清单

| 组件 | 路径 | 主要功能 |
|------|------|----------|
| 首页 | `pages/index/index.vue` | 搜索栏、轮播图 (u-swiper)、分类导航 (scroll-view)、商品网格、上拉加载 (u-loadmore) |
| 登录页 | `pages/login/login.vue` | 手机号/邮箱 + 密码登录，调用 userStore.loginAction() |
| 注册页 | `pages/register/register.vue` | 手机号 + 验证码 + 密码注册，60 秒倒计时 |
| 商品列表 | `pages/product/list.vue` | 按分类筛选 + 两列网格 + 触底分页 |
| 商品搜索 | `pages/product/search.vue` | 关键字模糊搜索 + 热门搜索词 |
| 商品详情 | `pages/product/detail.vue` | 图片轮播、价格/库存、加入购物车（需登录校验） |
| 购物车 | `pages/cart/cart.vue` | 全选/单选、数量加减、删除、底部结算按钮 |
| 确认订单 | `pages/order/confirm.vue` | 地址选择、商品清单、金额汇总、提交订单 |
| 订单列表 | `pages/order/list.vue` | Tab 状态筛选 + 支付/取消/确认收货操作 |
| 订单详情 | `pages/order/detail.vue` | 订单信息 + 地址快照 + 商品明细 |
| 支付成功 | `pages/order/pay-success.vue` | 支付成功提示 + 返回订单 |
| 地址列表 | `pages/address/list.vue` | 地址 CRUD + 默认地址 |
| 地址编辑 | `pages/address/edit.vue` | 省市区选择器 + 表单 |
| 个人中心 | `pages/user/user.vue` | 用户信息 + 订单入口 + 退出登录 |

#### 1.5.2 Pinia Store 组件

**User Store** (`store/user.js`)：

| 属性/方法 | 类型 | 说明 |
|-----------|------|------|
| `token` | state | JWT Token 字符串 |
| `userInfo` | state | 用户信息对象 |
| `isLogin` | getter | 是否已登录（!!token） |
| `loginAction(account, password)` | action | 调用登录 API → 保存 Token → 获取用户信息 |
| `fetchUserInfo()` | action | 从后端获取用户信息 |
| `logout()` | action | 清空 Token + 用户信息 + uni 缓存 |

**Cart Store** (`store/cart.js`)：

| 属性/方法 | 类型 | 说明 |
|-----------|------|------|
| `cartList` | state | 购物车商品列表 |
| `cartCount` | getter | 商品总数（含未选中） |
| `checkedItems` | getter | 已选中的商品数组 |
| `checkedCount` | getter | 已选中商品数量 |
| `isAllChecked` | getter | 是否全选 |
| `totalPrice` | getter | 已选商品总价（保留两位小数） |
| `fetchCartList()` | action | GET /api/cart 刷新列表 |
| `addAction(productId)` | action | 加入购物车 → 刷新列表 + toast |
| `updateQuantityAction(productId, qty)` | action | 修改数量 → 刷新列表 |
| `removeAction(productId)` | action | 删除商品 → 刷新列表 + toast |
| `toggleCheckAction(productId, checked)` | action | 切换选中状态 |

#### 1.5.3 请求层设计

**`utils/request.js`** — 基于 `uni.request` 的 HTTP 客户端：

| 特性 | 实现 |
|------|------|
| **BASE_URL** | 空字符串，走 Vite 代理转发到后端 |
| **JWT 自动携带** | 请求拦截器从 `uni.getStorageSync('token')` 读取 Token → 设置 `Authorization: Bearer xxx` |
| **401 处理** | 响应拦截器匹配 401 → 清空 Token → `uni.reLaunch` 到登录页 |
| **全局错误** | 400/403/500 统一弹 `uni.showToast` |
| **导出方法** | `get(url, params)` / `post(url, data)` / `put(url, data)` / `del(url)` |

### 1.6 UI 设计规范

| 设计元素 | 规范值 | 场景 |
|----------|--------|------|
| 主题色 | `#FF6034` | 导航栏、按钮渐变、高亮、选中态 |
| 价格色 | `#FF4444` | 所有价格标签 |
| 成功色 | `#07C160` | 支付成功、订单完成标签 |
| 警告色 | `#FF9500` | 待付款状态标签 |
| 背景色 | `#F5F5F5` | 页面全局背景 |
| 卡片背景 | `#FFFFFF` | 内容卡片 |
| 字体大小 | `20-36rpx` | 标签 20rpx → 正文 26-28rpx → 标题 30-36rpx |
| 圆角 | `12rpx` | 卡片圆角 |
| 按钮圆角 | `36-44rpx` | 全圆角胶囊形按钮 |
| 阴影 | `0 4rpx 20rpx rgba(0,0,0,0.06)` | 卡片/底部栏 |

### 1.7 响应式适配策略

- **基于 rpx 单位**：uni-app 的 rpx 是响应式像素单位，根据屏幕宽度动态计算（基准 750rpx = 屏幕宽度）
- **弹性容器**：使用 `display: flex` + `flex: 1` 实现自适应布局
- **百分比 + flex 网格**：商品列表采用 `calc(50% - 10rpx)` 两列自适应
- **safe-area 适配**：底部操作栏统一添加 `.safe-bottom` 类解决 iPhone 刘海屏
- **1rpx 边框**：使用 `border: 1rpx solid` 保证不同 DPR 屏幕上的细线效果

---

## 2. 后台架构设计

### 2.1 系统架构总览

```
┌──────────────────────────────────────┐
│           前端展示层                   │
│  ┌───────────┐  ┌──────────────────┐ │
│  │  C端 uni-app│  │  管理后台 Vue 3  │ │
│  │ (localhost: │  │  (localhost:     │ │
│  │   8081 H5)   │  │    8082 H5)      │ │
│  └──────┬──────┘  └────────┬─────────┘ │
└─────────┼──────────────────┼───────────┘
          │   HTTP REST API  │
          │   (JSON / JWT)   │
┌─────────▼──────────────────▼───────────┐
│              后端服务层                  │
│  ┌──────────────────────────────────┐  │
│  │    Spring Boot 3.2.5 (8080)      │  │
│  │  ┌────────────────────────────┐  │  │
│  │  │   表现层 (Controller)       │  │  │
│  │  │   User / Product / Cart    │  │  │
│  │  │   Order / Address / Admin  │  │  │
│  │  │   Common (Banner/Category) │  │  │
│  │  └────────────┬───────────────┘  │  │
│  │  ┌────────────▼───────────────┐  │  │
│  │  │   业务层 (Service)         │  │  │
│  │  │   核心业务逻辑 + 事务管理   │  │  │
│  │  │   乐观锁库存扣减            │  │  │
│  │  └────────────┬───────────────┘  │  │
│  │  ┌────────────▼───────────────┐  │  │
│  │  │   数据层 (Mapper)          │  │  │
│  │  │   MyBatis-Plus 3.5.6      │  │  │
│  │  │   雪花算法 ID 生成          │  │  │
│  │  └────────────┬───────────────┘  │  │
│  └───────────────┼──────────────────┘  │
└──────────────────┼─────────────────────┘
                   │
    ┌──────────────┼──────────────┐
    ▼              ▼              ▼
┌────────┐   ┌──────────┐  ┌──────────┐
│ MySQL  │   │  Redis   │  │ 本地磁盘  │
│  8.0   │   │   7.x    │  │ d:/uploads│
│ 持久层  │   │ 缓存/会话 │  │  图片存储  │
└────────┘   └──────────┘  └──────────┘
```

### 2.2 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 应用框架 |
| MyBatis-Plus | 3.5.6 | ORM 持久层 |
| MySQL | 8.0 | 关系型数据库 |
| Druid | 1.2.22 | 数据库连接池 + SQL 监控 |
| Redis | 7.x (Lettuce 6.3) | 缓存 + 购物车 + 验证码 |
| JWT (jjwt) | 0.12.5 | 认证 Token（RS256 签名） |
| Knife4j | 4.5.0 | API 接口文档 + 在线调试 |
| Hutool | 5.8.26 | 工具类库（BCrypt / 随机数等） |
| Java | 17 | 运行环境 |

### 2.3 项目分层架构

```
src/main/java/com/b2c/mall/
├── config/        ── 配置层（6个）
│   ├── WebMvcConfig.java       — CORS + 拦截器注册 + 静态资源映射
│   ├── MybatisPlusConfig.java  — 分页插件 + 乐观锁拦截器
│   ├── RedisConfig.java        — Redis 序列化（JSON）
│   └── Knife4jConfig.java      — API 文档
├── controller/    ── 控制器层（7个）
│   ├── user/                   — C端用户（注册/登录/信息）
│   ├── product/                — 商品浏览（列表/详情/搜索）
│   ├── cart/                   — 购物车管理（增删改查）
│   ├── order/                  — 订单流转（下单/支付/取消/收货）
│   ├── address/                — 收货地址 CRUD
│   ├── common/                 — 公共数据（轮播图/分类）
│   └── admin/                  — 后台管理（7个模块 + 上传 + 统计）
├── service/       ── 业务层（6 接口 + 6 实现）
│   ├── UserService             — 用户注册/登录（BCrypt + JWT）
│   ├── ProductService          — 商品查询（Cache-Aside 缓存）
│   ├── CartService             — 购物车（Redis Hash 存储）
│   ├── OrderService            — 订单（乐观锁 + 事务）
│   ├── AddressService          — 地址管理
│   └── BannerService           — 轮播图管理
├── mapper/        ── 数据层（7个 Mapper 接口）
│   ├── ProductMapper           — 含 @Update 自定义 Stock SQL
├── entity/        ── 实体层（7个）
├── dto/           ── 数据传输对象
│   ├── request/                — 入参 DTO（7个）
│   └── response/               — 出参 VO（5个）
├── interceptor/   ── 拦截器
│   └── JwtInterceptor          — JWT 验证 + 角色鉴权 + OPTIONS 放行
├── handler/       ── 处理器
│   └── MyMetaObjectHandler     — 自动填充 createTime/updateTime
├── enums/         ── 枚举
│   ├── OrderStatusEnum         — 订单状态枚举
│   └── RoleEnum                — 角色枚举
├── exception/     ── 异常处理
│   ├── BusinessException       — 业务异常
│   └── GlobalExceptionHandler  — @RestControllerAdvice 全局异常捕获
└── util/          ── 工具类
    ├── JwtUtil                 — JWT 生成/解析/验证
    └── RedisUtil               — Redis String/Hash 操作封装
```

### 2.4 中间件配置

#### 2.4.1 Druid 连接池

| 参数 | 值 | 说明 |
|------|-----|------|
| initial-size | 5 | 初始连接数 |
| min-idle | 5 | 最小空闲连接 |
| max-active | 20 | 最大活跃连接 |
| max-wait | 60000ms | 获取连接超时 |
| validation-query | SELECT 1 | 连接检验 SQL |
| 防火墙 | wall 过滤器 | 防 SQL 注入（禁止 DROP TABLE） |
| 监控页面 | /druid/* | 访问管理界面 |

#### 2.4.2 Redis (Lettuce)

| 配置项 | 值 |
|--------|-----|
| 客户端 | Lettuce（Spring Boot 2+ 默认） |
| 序列化 | Key=StringRedisSerializer, Value=Jackson2JsonRedisSerializer |
| 连接池 | max-active=8, max-idle=8, min-idle=0 |
| 超时 | connection=10000ms |

**Redis 使用场景**：

| 模块 | Key 格式 | 类型 | TTL | 说明 |
|------|----------|------|-----|------|
| 购物车 | `cart:{userId}` | Hash | 无（持久） | field=productId, value=CartItemVO JSON |
| 商品详情 | `product:detail:{id}` | String | 30 分钟 | 热点商品缓存（Cache-Aside） |
| 验证码 | `sms:code:{phone}` | String | 5 分钟 | 短信验证码 |
| Token 黑名单 | `token:black:{userId}` | String | 24 小时 | 登出时加入 |

### 2.5 安全策略

#### 2.5.1 认证流程

```
前端                               后端
 │                                  │
 │  POST /api/user/login            │
 │  { account, password }           │
 ├─────────────────────────────────>│
 │                                  ├─ WHERE phone/email = account
 │                                  ├─ BCrypt.checkpw(plain, hash)
 │                                  ├─ jwtUtil.generateToken(...)
 │                                  ├─ 返回 { token, userId, ... }
 │<─────────────────────────────────┤
 │  uni.setStorageSync('token',...) │
 │                                  │
 │  后续请求:                        │
 │  Authorization: Bearer {token}   │
 ├─────────────────────────────────>│
 │                                  ├─ JwtInterceptor.preHandle()
 │                                  │  ├─ 提取 Authorization 头
 │                                  │  ├─ 验证 Bearer 格式
 │                                  │  ├─ jwtUtil.validateToken()
 │                                  │  ├─ 角色权限校验(/admin/* → ADMIN)
 │                                  │  └─ 放行/返回 401
 │<─────────────────────────────────┤
```

#### 2.5.2 安全机制清单

| 层次 | 机制 | 实现 |
|------|------|------|
| 传输层 | 密码加密 | BCrypt 单向加密（Hutool BCrypt.hashpw） |
| 认证层 | JWT Token | 登录签发，24h 过期，HMAC-SHA256 签名 |
| 拦截层 | 路径鉴权 | JwtInterceptor，/api/** → USER, /admin/** → ADMIN |
| 跨域层 | CORS | WebMvcConfig.addCorsMappings()，允许所有来源 |
| 数据层 | SQL 注入防护 | MyBatis 预编译 + Druid 防火墙 |
| 并发层 | 超卖防护 | 乐观锁（t_product.version），下单时版本号校验 |
| 数据层 | 逻辑删除 | @TableLogic，所有表统一 deleted=0 过滤 |
| 数据层 | 密码痕迹 | 响应 DTO 不包含 password 字段 |

#### 2.5.3 拦截器白名单

以下路径**不拦截**（在 WebMvcConfig 中排除）：

| 路径 | 说明 |
|------|------|
| `/api/user/login` | 用户登录 |
| `/api/user/register` | 用户注册 |
| `/api/user/send-sms` | 发送短信验证码 |
| `/api/products/**` | 商品浏览（含搜索、详情） |
| `/api/banners` | 轮播图 |
| `/api/categories` | 商品分类 |
| `/admin/login` | 管理员登录 |

### 2.6 性能优化方案

#### 2.6.1 Redis 缓存策略（Cache-Aside）

```
读流程:                          写流程:
 GET /api/products/{id}          PUT /admin/products/{id}
        │                                │
        ▼                                ▼
 Redis.get("product:detail:{id}")    MySQL UPDATE product
        │                                │
   ┌────┴────┐                          ▼
   ▼         ▼                     Redis.DEL("product:detail:{id}")
  命中      未命中                        │
   │         │                           ▼
   │    MySQL SELECT                 下次读取时
   │         │                       Cache Miss → 重新加载
   │    Redis.SETEX(1800)
   │         │
   └────┬────┘
        ▼
   返回 ProductVO
```

#### 2.6.2 乐观锁防超卖

```sql
-- ProductMapper.deductStock() 自定义 SQL
UPDATE t_product 
SET stock = stock - #{quantity},
    version = version + 1
WHERE id = #{productId} 
  AND version = #{version} 
  AND stock >= #{quantity}
```

- 并发冲突时返回 `affected rows = 0`
- 应用层捕获 → 抛出 BusinessException → 事务回滚
- 前端提示"库存不足或系统繁忙，请稍后重试"

#### 2.6.3 购物车 Redis Hash 结构

```
Key: cart:1001
┌─────────────┬──────────────────────────────────┐
│ Field (HSET)│ Value (JSON)                     │
├─────────────┼──────────────────────────────────┤
│ "101"       │ {"productId":101,"name":"华为Mate 60 Pro",│
│             │  "price":6999,"quantity":2,"image":...}    │
│ "102"       │ {"productId":102,"name":"MacBook Pro",     │
│             │  "price":14999,"quantity":1,"image":...}   │
└─────────────┴──────────────────────────────────┘
```

优势：
- 单次 `HGETALL` 即可获取全部购物车数据
- `HSET` 原子操作更新单条商品数量，无并发问题
- 数据结构天然支持选中/取消选中（在 JSON Value 中维护 checked 字段）

#### 2.6.4 雪花算法主键

- `MyBatis-Plus @TableId(type = IdType.ASSIGN_ID)`
- 全局唯一递增 ID，无需数据库自增锁
- 分库分表友好

### 2.7 前端项目架构（管理后台）

#### 2.7.1 技术栈

| 技术 | 版本 |
|------|------|
| Vue 3 | 3.4.21 |
| Element Plus | 2.6.1 |
| Pinia | 2.1.7 |
| Vue Router | 4.3.0 |
| Axios | 1.6.7 |
| Vite | 5.2.8 |

#### 2.7.2 路由结构

| 路径 | 组件 | 说明 |
|------|------|------|
| `/login` | `Login.vue` | 管理员登录（独立页，无布局） |
| `/dashboard` | `Dashboard.vue` | 数据看板（3 指标卡 + 5 快捷入口） |
| `/products` | `ProductList.vue` | 商品管理（搜索/分页/增删改/上下架/图片上传） |
| `/categories` | `CategoryList.vue` | 分类管理（增删改） |
| `/banners` | `BannerList.vue` | 轮播图管理（增删改/图片上传） |
| `/orders` | `OrderList.vue` | 订单管理（状态筛选/详情/发货） |
| `/users` | `UserList.vue` | 用户管理（搜索/启用禁用） |

#### 2.7.3 路由守卫

```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')   // 无 Token → 跳登录
  } else if (to.path === '/login' && token) {
    next('/dashboard') // 已登录 → 跳看板
  } else {
    next()
  }
})
```

#### 2.7.4 Axios 拦截器

| 拦截器 | 功能 |
|--------|------|
| **请求拦截** | 从 localStorage 读取 admin_token → 设置 Authorization 头 |
| **响应拦截** | 401 → 清空 Token + 跳登录；403 → 权限不足提示；400 → 参数错误提示 |

---

## 3. 数据库设计

### 3.1 数据库信息

| 属性 | 值 |
|------|-----|
| 数据库名称 | `b2c_mall` |
| 字符集 | `utf8mb4` |
| 排序规则 | `utf8mb4_general_ci` |
| 存储引擎 | `InnoDB` |

### 3.2 表结构设计

#### 3.2.1 t_user — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, 雪花算法 | 主键 |
| phone | VARCHAR(20) | NOT NULL, UNIQUE | 手机号（用于登录） |
| email | VARCHAR(100) | NULL | 邮箱（可选） |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| nickname | VARCHAR(50) | NULL | 昵称 |
| avatar | VARCHAR(255) | NULL | 头像 URL |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | USER / ADMIN |
| status | TINYINT | NOT NULL, DEFAULT 1 | 1=启用, 0=禁用 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |

**索引**：PRIMARY (id), UNIQUE uk_phone (phone), INDEX idx_create_time (create_time)

#### 3.2.2 t_address — 收货地址表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| user_id | BIGINT | NOT NULL | 所属用户 ID |
| receiver_name | VARCHAR(50) | NOT NULL | 收货人 |
| receiver_phone | VARCHAR(20) | NOT NULL | 收货电话 |
| province | VARCHAR(50) | NOT NULL | 省份 |
| city | VARCHAR(50) | NOT NULL | 城市 |
| district | VARCHAR(50) | NOT NULL | 区/县 |
| detail | VARCHAR(255) | NOT NULL | 详细地址 |
| is_default | TINYINT | NOT NULL, DEFAULT 0 | 1=默认地址 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |

**索引**：PRIMARY (id), INDEX idx_user_id (user_id)

#### 3.2.3 t_product_category — 商品分类表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| name | VARCHAR(50) | NOT NULL | 分类名称 |
| sort | INT | NOT NULL, DEFAULT 0 | 排序权重 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 1=启用, 0=禁用 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |

**索引**：PRIMARY (id)

#### 3.2.4 t_product — 商品表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| category_id | BIGINT | NOT NULL | 所属分类 ID |
| name | VARCHAR(100) | NOT NULL | 商品名称 |
| price | DECIMAL(10,2) | NOT NULL | 价格（元） |
| stock | INT | NOT NULL, DEFAULT 0 | 库存数量 |
| version | INT | NOT NULL, DEFAULT 0 | **乐观锁版本号** |
| image | VARCHAR(500) | NULL | 图片 URL 列表（逗号分隔） |
| description | TEXT | NULL | 商品描述（富文本） |
| status | TINYINT | NOT NULL, DEFAULT 1 | 1=上架, 0=下架 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |

**索引**：PRIMARY (id), INDEX idx_category_id (category_id), INDEX idx_status (status)

**乐观锁说明**：
```sql
-- 库存扣减 SQL（ProductMapper.deductStock）
UPDATE t_product 
SET stock = stock - #{quantity}, version = version + 1
WHERE id = #{productId} AND version = #{version} AND stock >= #{quantity}
```

#### 3.2.5 t_banner — 轮播图表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| image_url | VARCHAR(255) | NOT NULL | 图片 URL |
| link_url | VARCHAR(255) | NULL | 跳转链接 |
| sort | INT | NOT NULL, DEFAULT 0 | 排序权重 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 1=启用, 0=禁用 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |

**索引**：PRIMARY (id)

#### 3.2.6 t_order — 订单表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| order_no | VARCHAR(32) | NOT NULL, UNIQUE | 订单编号 |
| user_id | BIGINT | NOT NULL | 下单用户 ID |
| address_json | JSON | NOT NULL | **收货地址快照** |
| total_amount | DECIMAL(10,2) | NOT NULL | 订单总金额 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'WAIT_PAY' | 订单状态 |
| pay_time | DATETIME | NULL | 支付时间 |
| delivery_time | DATETIME | NULL | 发货时间 |
| finish_time | DATETIME | NULL | 完成时间 |
| cancel_time | DATETIME | NULL | 取消时间 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |

**索引**：PRIMARY (id), UNIQUE uk_order_no (order_no), INDEX idx_user_id (user_id), INDEX idx_status (status), INDEX idx_create_time (create_time)

#### 3.2.7 t_order_item — 订单商品明细表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| order_id | BIGINT | NOT NULL | 所属订单 ID |
| product_id | BIGINT | NOT NULL | 商品原始 ID |
| product_name | VARCHAR(100) | NOT NULL | **商品名称快照** |
| product_image | VARCHAR(255) | NULL | **商品图片快照** |
| price | DECIMAL(10,2) | NOT NULL | **下单时单价快照** |
| quantity | INT | NOT NULL | 购买数量 |
| total_amount | DECIMAL(10,2) | NOT NULL | 小计 (price × quantity) |

**索引**：PRIMARY (id), INDEX idx_order_id (order_id)

### 3.3 实体关系图（ER）

```
┌──────────────┐         ┌──────────────────┐
│    t_user    │ 1    N  │    t_address     │
│──────────────│─────────│──────────────────│
│ id (PK)      │         │ id (PK)          │
│ phone (UK)   │         │ user_id (FK)     │
│ password     │         │ receiver_name    │
│ role         │         │ receiver_phone   │
│ status       │         │ province/city/... │
└──────┬───────┘         └──────────────────┘
       │ 1
       │
       │ N
┌──────▼───────┐         ┌──────────────────┐
│   t_order    │ 1    N  │  t_order_item    │
│──────────────│─────────│──────────────────│
│ id (PK)      │         │ id (PK)          │
│ user_id (FK) │         │ order_id (FK)    │
│ order_no (UK)│         │ product_id (FK)  │
│ address_json │         │ product_name     │
│ total_amount │         │ price / quantity │
│ status       │         │ total_amount     │
└──────────────┘         └──────────────────┘

┌──────────────────┐      ┌──────────────────┐
│  t_product_category   │ 1    N  │    t_product     │
│──────────────────│─────────│──────────────────│
│ id (PK)          │         │ id (PK)          │
│ name             │         │ category_id (FK) │
│ sort             │         │ name             │
│ status           │         │ price / stock    │
└──────────────────┘         │ version (乐观锁)  │
                              │ image            │
┌──────────────────┐         └──────────────────┘
│    t_banner      │
│──────────────────│
│ id (PK)          │
│ image_url        │
│ link_url         │
│ sort / status    │
└──────────────────┘
```

### 3.4 订单状态机

```
                      ┌──────────────────┐
                      │   用户取消订单     │
                      │  (仅 WAIT_PAY)   │
                      └────────┬─────────┘
                               ▼
┌──────────┐    ┌──────────┐   ┌──────────┐    ┌──────────┐    ┌──────────┐
│ WAIT_PAY  │ → │   PAID    │ → │ RECEIVING │ → │ COMPLETED │    │CANCELLED │
│  (待付款)  │   │  (已支付)  │   │  (待收货)  │   │  (已完成)  │    │ (已取消)  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
     │               │               │               │
     │ 用户支付       │ 管理员发货     │ 用户确认收货    │
     └───────────────┴───────────────┴───────────────┘
```

| 状态 | 枚举值 | 可流转到 | 触发操作 |
|------|--------|----------|----------|
| 待付款 | WAIT_PAY | PAID | 用户点击"去支付" |
| 待付款 | WAIT_PAY | CANCELLED | 用户点击"取消订单" |
| 已支付 | PAID | RECEIVING | 管理员填写物流单号发货 |
| 待收货 | RECEIVING | COMPLETED | 用户点击"确认收货" |

### 3.5 逻辑删除

所有 7 张表均使用 `deleted` 字段实现逻辑删除：

| 配置项 | 值 |
|--------|-----|
| 全局逻辑删除字段 | `deleted` |
| 未删除值 | `0` |
| 已删除值 | `1` |
| 查询自动过滤 | `WHERE deleted = 0`（MyBatis-Plus 自动追加） |

---

## 4. API 接口设计

### 4.1 接口规范

| 约定 | 说明 |
|------|------|
| 基础路径 | `/api` (C端) / `/admin` (管理端) |
| 请求格式 | `application/json` (除文件上传使用 `multipart/form-data`) |
| 响应格式 | `application/json` |
| 认证方式 | `Authorization: Bearer {token}` |
| 分页参数 | `page` (页码, 默认1), `pageSize` (每页条数, 默认10) |
| 分页响应 | `{ records: [], total: N, pages: N }` |
| 日期格式 | `yyyy-MM-dd HH:mm:ss` |
| 空值处理 | Jackson `non_null` — 响应不包含 null 字段 |

### 4.2 统一错误码

| HTTP 状态码 | 含义 | 触发条件 |
|-------------|------|----------|
| 200 | 成功 | 正常响应 |
| 201 | 创建成功 | POST 新增资源 |
| 400 | 参数错误 | 校验失败 / 业务逻辑错误 |
| 401 | 未认证 | Token 缺失 / 过期 / 无效 |
| 403 | 无权限 | 角色不匹配 |
| 404 | 资源不存在 | 查无记录 |
| 500 | 服务器错误 | 未捕获异常 |

### 4.3 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(500).body(Map.of("message", "服务器内部错误：" + e.getMessage()));
    }
}
```

### 4.4 C 端接口清单（22个）

#### 4.4.1 用户模块

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/user/send-sms` | 无 | 发送短信验证码 |
| POST | `/api/user/register` | 无 | 用户注册 |
| POST | `/api/user/login` | 无 | 手机号/邮箱 + 密码登录 |
| GET | `/api/user/info` | USER | 获取当前用户信息 |

**POST /api/user/send-sms**

```
Request:  { "phone": "15818561610" }
Response: { "code": 200 }
说明:     验证码 6 位随机数，存入 Redis key=sms:code:{phone}，TTL=5分钟
```

**POST /api/user/register**

```
Request:  { "phone": "15818561610", "smsCode": "790296", "password": "123456" }
Response: { "code": 201, "message": "注册成功" }
校验:     手机号唯一性 + 验证码正确性 + 密码 6-20 位
```

**POST /api/user/login**

```
Request:  { "account": "15818561610", "password": "123456" }
Response: { "token": "eyJhbG...", "userId": 2, "phone": "15818561610",
            "nickname": "用户", "role": "USER" }
说明:     account 含 @ → 邮箱查询，否则 → 手机号查询
```

**GET /api/user/info**

```
Headers:  Authorization: Bearer {token}
Response: { "userId": 2, "phone": "15818561610", "nickname": "用户", "role": "USER" }
```

#### 4.4.2 商品模块

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/products` | 无 | 商品分页列表（按分类筛选） |
| GET | `/api/products/{id}` | 无 | 商品详情（Redis 缓存） |
| GET | `/api/products/search` | 无 | 关键字模糊搜索 |

**GET /api/products**

```
Query:    ?page=1&pageSize=10&categoryId=1
Response: { "records": [...], "total": 100, "pages": 10 }
说明:     只返回 status=1（上架）且 deleted=0 的商品
```

#### 4.4.3 购物车模块

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/cart` | USER | 获取购物车列表 |
| POST | `/api/cart` | USER | 加入购物车（?productId=xxx） |
| PUT | `/api/cart/{productId}` | USER | 修改数量（?quantity=xxx） |
| DELETE | `/api/cart/{productId}` | USER | 删除商品 |
| PUT | `/api/cart/check/{productId}` | USER | 选中/取消选中 |

**GET /api/cart**

```
Response: [
  { "productId": 1, "name": "华为 Mate 60 Pro", "price": 6999,
    "quantity": 2, "image": "/uploads/xxx.png", "checked": true },
  ...
]
存储:     Redis Hash key=cart:{userId}
```

#### 4.4.4 订单模块

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/orders` | USER | 提交订单（乐观锁扣库存） |
| GET | `/api/orders` | USER | 订单列表（?page&pageSize&status） |
| GET | `/api/orders/{id}` | USER | 订单详情 |
| PUT | `/api/orders/{id}/pay` | USER | 模拟支付 |
| PUT | `/api/orders/{id}/cancel` | USER | 取消订单（回滚库存） |
| PUT | `/api/orders/{id}/confirm` | USER | 确认收货 |

**POST /api/orders**

```
Request:  { "addressId": 1, "remark": "" }
Response: { "id": 100, "orderNo": "20260530143021123456", "totalAmount": 6999, ... }
事务保证: 库存扣减 + 订单创建 + 明细创建 + 清空购物车选中项 → @Transactional
失败场景: 库存不足 → 400 "库存不足或系统繁忙，请稍后重试"
```

#### 4.4.5 收货地址模块

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/addresses` | USER | 地址列表 |
| POST | `/api/addresses` | USER | 新增地址 |
| PUT | `/api/addresses/{id}` | USER | 修改地址 |
| DELETE | `/api/addresses/{id}` | USER | 删除地址 |
| PUT | `/api/addresses/{id}/default` | USER | 设为默认 |

#### 4.4.6 公共模块

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/banners` | 无 | 轮播图列表（status=1, 按 sort 排序） |
| GET | `/api/categories` | 无 | 商品分类列表（status=1） |

### 4.5 管理端接口清单（21个）

#### 4.5.1 认证

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/admin/login` | 无 | 管理员登录（role=ADMIN 验证） |

#### 4.5.2 商品管理

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/admin/products` | ADMIN | 商品列表（含下架，分页搜索） |
| POST | `/admin/products` | ADMIN | 新增商品 |
| PUT | `/admin/products/{id}` | ADMIN | 修改商品 |
| DELETE | `/admin/products/{id}` | ADMIN | 删除商品（逻辑删除） |
| PUT | `/admin/products/{id}/status` | ADMIN | 上架/下架切换 |

#### 4.5.3 分类管理

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/admin/categories` | ADMIN | 分类列表 |
| POST | `/admin/categories` | ADMIN | 新增分类 |
| PUT | `/admin/categories/{id}` | ADMIN | 修改分类 |
| DELETE | `/admin/categories/{id}` | ADMIN | 删除分类 |

#### 4.5.4 轮播图管理

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/admin/banners` | ADMIN | 轮播图列表 |
| POST | `/admin/banners` | ADMIN | 新增轮播图 |
| PUT | `/admin/banners/{id}` | ADMIN | 修改轮播图 |
| DELETE | `/admin/banners/{id}` | ADMIN | 删除轮播图 |

#### 4.5.5 订单管理

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/admin/orders` | ADMIN | 订单列表（状态筛选） |
| GET | `/admin/orders/{id}` | ADMIN | 订单详情 |
| PUT | `/admin/orders/{id}/deliver` | ADMIN | 发货（填写物流单号 → PAID→RECEIVING） |

**PUT /admin/orders/{id}/deliver**

```
Request:  { "expressCompany": "顺丰速运", "expressNo": "SF1234567890" }
Response: { "code": 200 }
说明:     仅 PAID 状态订单可发货，发货后状态变为 RECEIVING
```

#### 4.5.6 用户管理

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/admin/users` | ADMIN | 用户列表（分页搜索） |
| PUT | `/admin/users/{id}/status` | ADMIN | 启用/禁用用户 |

#### 4.5.7 其他

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/admin/upload` | ADMIN | 图片上传（multipart/form-data） |
| GET | `/admin/statistics/today` | ADMIN | 今日统计（订单数/营业额） |

**POST /admin/upload**

```
Content-Type: multipart/form-data
FormData:     { "file": <图片二进制> }
Response:     { "url": "/uploads/uuid.png" }  (201 Created)
存储路径:     d:/uploads/uuid.png
访问URL:      http://localhost:8080/uploads/uuid.png
```

### 4.6 接口权限矩阵

| 前缀 | 角色要求 | 拦截器行为 |
|------|----------|------------|
| `/api/products/**` | 无 | 白名单，直接放行 |
| `/api/banners` | 无 | 白名单 |
| `/api/categories` | 无 | 白名单 |
| `/api/user/login` | 无 | 白名单 |
| `/api/user/register` | 无 | 白名单 |
| `/api/user/send-sms` | 无 | 白名单 |
| `/admin/login` | 无 | 白名单 |
| `/api/**` (其他) | USER | 需 Valid Token |
| `/admin/**` | ADMIN | 需 Token + role=ADMIN |

---

## 附录

### A. 项目端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| Spring Boot 后端 | 8080 | REST API + Swagger |
| C端 uni-app H5 | 8081 | Vite devServer |
| 管理后台 Vue 3 | 8082 | Vite devServer |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

### B. 启动顺序

```
1. MySQL 8.0 + Redis 7.x 服务就绪
2. 执行 init.sql 建表
3. 执行 fix_admin.sql 初始化管理员
4. 启动 Spring Boot (IDEA 运行 MallApplication)
5. 启动 C端 H5 (HBuilderX 运行)
6. 启动管理后台 (npm run dev)
```

### C. 关键技术决策记录

| 决策 | 方案 | 理由 |
|------|------|------|
| 主键策略 | 雪花算法 | 分布式唯一，无需自增锁 |
| 购物车存储 | Redis Hash | 高性能读写，无需写 DB |
| 超卖防护 | 乐观锁 | 冲突率低场景最优选择 |
| 删除策略 | 逻辑删除 | 数据可追溯，误删可恢复 |
| 订单地址 | JSON 快照 | 防止地址被删后订单数据缺失 |
| 商品缓存 | Cache-Aside | 经典缓存模式，避免脏数据 |
| 认证方案 | JWT | 无状态，适合分布式 |
| 密码加密 | BCrypt | 单向哈希，防彩虹表 |
| 图片存储 | 本地磁盘 | 开发阶段无需 OSS |
