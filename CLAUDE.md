# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

移动端 B2C 商城系统，包含三个子项目：

| 子项目 | 路径 | 技术栈 | 端口 |
|--------|------|--------|------|
| Java 后端 | `src/b2c-mall/` | Spring Boot 3.2.5, MyBatis-Plus 3.5.6, Redis, MySQL, JWT | 8080 |
| 管理后台 | `admin/` | Vue 3 + Element Plus + Pinia + Vue Router + Vite | 8082 |
| 移动端 C 端 | `web/b2c-mall-app/` | uni-app + uView Plus + Pinia (Vite 构建) | 8081 |

完整的设计文档见 `移动端B2C商城-系统设计文档.md`，需求文档见 `移动端B2C商城-需求分析文档.md`。

## 常用命令

### Java 后端

```bash
cd src/b2c-mall

# 编译 & 运行 (需要私有配置 application-dev.yml 中的 MySQL/Redis/阿里云短信 配置)
mvn spring-boot:run

# 只编译不运行
mvn clean compile

# 打包为可执行 JAR
mvn clean package -DskipTests
```

- Java 17 必须
- 应用启动后，Knife4j 接口文档: `http://localhost:8080/doc.html`
- Druid 监控页: `http://localhost:8080/druid/`

### 管理后台

```bash
cd admin

npm install       # 安装依赖
npm run dev       # 开发模式 (端口 8082)
npm run build     # 生产构建
```

- Dev server 已配置代理：`/api`、`/admin`、`/uploads` → `http://127.0.0.1:8080`

### 移动端

```bash
cd web/b2c-mall-app

npm install       # 安装依赖
npm run dev:h5    # H5 开发模式 (端口 8081)
npm run build:h5  # H5 构建
```

- Dev server 已配置代理：`/api`、`/admin`、`/uploads` → `http://127.0.0.1:8080`

## 架构要点

### 后端分层结构

```
controller/     → 请求参数校验、调用 Service、返回结果（分 C 端 /api 和后台 /admin 两组路径）
service/        → 业务逻辑（接口 + impl 实现类）
mapper/         → MyBatis-Plus BaseMapper，对应数据表
entity/         → 数据库实体（@TableLogic 逻辑删除、雪花算法主键）
dto/request/    → 前端请求体
dto/response/   → 前端响应 VO（使用 Lombok @Builder）
config/         → Spring 配置（WebMvc、Redis、MyBatis-Plus、Knife4j）
interceptor/    → JwtInterceptor 认证拦截
exception/      → BusinessException + GlobalExceptionHandler
util/           → JwtUtil、RedisUtil
enums/          → OrderStatusEnum、RoleEnum
```

### 关键设计决策

- **包结构按技术分层**，不是按领域。新增功能在对应层中添加文件。
- **购物车不建 MySQL 表**，完全存储在 Redis Hash：`cart:{userId}` → `{productId: JSON}`
- **商品库存扣减用乐观锁**：`WHERE version = ? AND stock >= ?`，冲突时抛 BusinessException 回滚事务
- **缓存策略**：读商品先查 Redis → 未命中查 MySQL 并回写 Redis（TTL 30min）；写商品后直接删除缓存
- **订单状态机**：WAIT_PAY → PAY → RECEIVING → COMPLETED；WAIT_PAY 时可取消 → CANCELLED（库存回滚）
- **主键**：雪花算法（MyBatis-Plus ASSIGN_ID），所有表统一
- **删除**：全表逻辑删除（`deleted` 字段 + `@TableLogic`）
- **地址快照**：下单时将收货地址序列化为 JSON 存入订单表，防止后续地址变更影响订单查询
- **API 响应**：RESTful 标准 HTTP 状态码，直接返回数据对象或 `{"message": "..."}` 错误信息
- **权限**：JwtInterceptor 根据请求路径前缀判断 — `/admin/**` 要求 ADMIN 角色，`/api/**` 要求 USER 角色

### 配置文件层级

- `application.yml` — 通用配置（端口、MyBatis-Plus 全局设置、Jackson、文件上传）
- `application-dev.yml` — 开发环境（数据库连接、Redis、JWT密钥、阿里云短信、文件上传路径）— **包含敏感信息，不提交**

### 前端架构差异

- **管理后台**：标准 SPA，Vue Router (hash 模式) + Element Plus，axios 请求封装自动处理 Snowflake ID 精度丢失（大数字转字符串）
- **移动端**：uni-app pages 模式，使用 `pages.json` 配置路由和 tabbar，uView Plus 组件库（easycom 自动导入），uni.scss 全局样式变量
