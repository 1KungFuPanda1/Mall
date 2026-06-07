# B2C 移动端商城

基于 Spring Boot 3 + uni-app 的移动端 B2C 电商系统，包含 C 端移动应用和后台管理系统。

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.2.5 / Java 17 | RESTful API |
| ORM | MyBatis-Plus 3.5.6 | 通用 CRUD + 分页 + 乐观锁 |
| 数据库 | MySQL 8.0 + Druid 连接池 | 关系型数据存储 |
| 缓存 | Redis 7.x (Lettuce) | 商品缓存 + 购物车 + 验证码 |
| 认证 | JWT (jjwt 0.12) | Token 签发与校验 |
| 接口文档 | Knife4j 4.5 | Swagger 增强版，在线调试 |
| C 端前端 | uni-app + uView Plus 3.x | 移动端跨端开发 |
| 管理后台 | Vue 3 + Element Plus + Pinia | 后台管理系统 |
| 构建 | Maven / Vite | 依赖管理 & 打包 |

## 项目结构

```
├── src/b2c-mall/          # Java 后端 (Spring Boot)
│   ├── controller/        #   控制器层 (C端/API + 管理后台)
│   ├── service/           #   业务逻辑层
│   ├── mapper/            #   MyBatis-Plus Mapper
│   ├── entity/            #   数据库实体
│   └── dto/               #   请求/响应 DTO
├── web/b2c-mall-app/      # 移动端 C 端 (uni-app)
│   ├── pages/             #   页面 (首页/商品/购物车/订单/用户)
│   ├── store/             #   Pinia 状态管理
│   └── services/          #   API 封装
├── admin/                 # 管理后台 (Vue 3 + Element Plus)
│   ├── src/views/         #   页面 (看板/商品/分类/订单/用户)
│   └── src/api/           #   Axios 请求封装
└── docs/                  # 设计文档
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x+
- Node.js 18+

### 1. 启动后端

```bash
cd src/b2c-mall

# 修改 application-dev.yml 中的数据库、Redis、阿里云短信配置
# 执行 src/main/resources/db/init.sql 建表

mvn spring-boot:run
# 启动后访问: http://localhost:8080/doc.html (接口文档)
# Druid 监控: http://localhost:8080/druid/
```

### 2. 启动管理后台

```bash
cd admin

npm install
npm run dev
# 访问: http://localhost:8082
```

### 3. 启动移动端

```bash
cd web/b2c-mall-app

npm install
npm run dev:h5
# 访问: http://localhost:8081
```

前端开发服务器已配置代理，自动将 `/api`、`/admin`、`/uploads` 请求转发到后端 `8080` 端口。

## 核心功能

**C 端（移动端）**
- 用户注册/登录（手机号 + 短信验证码）
- 商品浏览（分类筛选、搜索）
- 购物车（Redis 存储）
- 订单管理（下单、支付、取消、确认收货）
- 收货地址管理

**管理后台**
- 数据看板（今日订单/营业额统计）
- 商品管理（上下架、CRUD）
- 分类管理
- 订单管理（发货处理）
- 用户管理（启用/禁用）
- 轮播图管理
- 文件上传

## 设计亮点

- **乐观锁库存扣减** — 高并发下单安全，`WHERE version = ? AND stock >= ?`
- **购物车存 Redis** — 无需建表，Hash 结构查询高效
- **商品缓存 Cache-Aside** — 读优先查 Redis，写后删除缓存
- **订单地址快照** — 下单时序列化地址 JSON，防止后续变更影响追溯
- **雪花算法主键** — 分布式唯一 ID
- **全表逻辑删除** — MyBatis-Plus `@TableLogic`
