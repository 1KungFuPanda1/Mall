-- ============================================
-- B2C商城 — 管理员账号修复脚本
-- 问题: 前端传 account='admin'，但原账号 phone='13800000000' 无法匹配
-- 解决: 将管理员账号的 phone 改为 'admin'，同时更新正确的 BCrypt 哈希
-- 
-- 登录账号: admin
-- 登录密码: admin123
-- ============================================

-- 方式1: 如果已存在 phone='admin' 的记录，则更新
UPDATE t_user 
SET password = '$2a$10$06hIRHoMem65H/B6Lc7g6udoI1hngaIwGBcMl..3F9ePX9rsZxQ1a',
    nickname = '系统管理员',
    role = 'ADMIN',
    status = 1
WHERE phone = 'admin';

-- 方式2: 如果存在旧的管理员账号(phone=13800000000)，则将其 phone 改为 'admin'
UPDATE t_user 
SET phone = 'admin',
    password = '$2a$10$06hIRHoMem65H/B6Lc7g6udoI1hngaIwGBcMl..3F9ePX9rsZxQ1a',
    nickname = '系统管理员',
    role = 'ADMIN',
    status = 1
WHERE phone = '13800000000' AND role = 'ADMIN';

-- 方式3: 如果完全没有管理员账号，则插入
INSERT INTO t_user (id, phone, password, nickname, role, status)
SELECT 1, 'admin', '$2a$10$06hIRHoMem65H/B6Lc7g6udoI1hngaIwGBcMl..3F9ePX9rsZxQ1a', '系统管理员', 'ADMIN', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE role = 'ADMIN');
