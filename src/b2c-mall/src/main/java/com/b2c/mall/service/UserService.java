package com.b2c.mall.service; // Service 接口包

import com.b2c.mall.dto.request.LoginRequest; // 登录请求 DTO
import com.b2c.mall.dto.request.RegisterRequest; // 注册请求 DTO
import com.b2c.mall.dto.response.LoginResponse; // 登录响应 DTO
import com.b2c.mall.entity.User; // 用户实体

/**
 * 用户服务接口 — 定义 C 端用户相关的业务方法
 */
public interface UserService {

    /**
     * 用户注册
     * 流程：校验验证码 → 检查手机号唯一性 → BCrypt 加密密码 → 插入用户 → 返回
     *
     * @param request 注册请求参数（手机号、密码、验证码）
     */
    void register(RegisterRequest request); // 注册新用户

    /**
     * 用户登录
     * 流程：支持手机号或邮箱 → 查询用户 → 校验密码 → 生成 JWT Token → 返回登录信息
     *
     * @param request 登录请求参数（账号、密码）
     * @return LoginResponse 包含 JWT Token 和用户基本信息
     */
    LoginResponse login(LoginRequest request); // 用户登录并返回Token

    /**
     * 发送短信验证码
     * 流程：生成6位随机验证码 → 存入 Redis（5分钟过期）→ 通过阿里云短信发送到用户手机
     *
     * @param phone 用户手机号
     */
    void sendSmsCode(String phone); // 发送短信验证码

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID（从 JWT Token 中解析得到）
     * @return User 实体对象（不含密码）
     */
    User getUserById(Long userId); // 查询用户信息
}
