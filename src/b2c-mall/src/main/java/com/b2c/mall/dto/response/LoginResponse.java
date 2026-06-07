package com.b2c.mall.dto.response; // 响应 DTO 包

import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Builder; // Lombok 建造者模式注解
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

/**
 * 登录成功响应 DTO — 返回给前端的登录结果
 *
 * 前端收到 token 后需要：
 * 1. 存入 localStorage 或 uni-app 的 storage 中
 * 2. 后续所有需要认证的请求都在 Header 中携带: Authorization: Bearer {token}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // 启用建造者模式，方便构建响应对象
public class LoginResponse {

    /** JWT 认证令牌（前端存入缓存，后续请求携带） */
    private String token; // JWT Token 字符串

    /** 用户 ID */
    private Long userId; // 登录用户的唯一标识

    /** 用户手机号 */
    private String phone; // 登录用户的手机号

    /** 用户昵称 */
    private String nickname; // 登录用户的昵称

    /** 用户角色（USER 或 ADMIN） */
    private String role; // 登录用户的角色类型
}
