package com.b2c.mall.dto.request; // 请求 DTO 包

import lombok.Data; // Lombok 数据类

/**
 * 管理员登录请求 DTO
 */
@Data
public class AdminLoginRequest {

    /** 管理员登录账号（手机号） */
    private String account; // 管理员手机号

    /** 管理员登录密码 */
    private String password; // 管理员密码
}
