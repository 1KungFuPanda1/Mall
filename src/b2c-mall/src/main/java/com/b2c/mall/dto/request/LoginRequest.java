package com.b2c.mall.dto.request; // 请求 DTO 包

import jakarta.validation.constraints.NotBlank; // 非空校验注解
import lombok.Data; // Lombok 数据类

/**
 * 用户登录请求 DTO — 封装登录接口的入参
 *
 * 支持两种登录方式：
 * 1. 手机号 + 密码登录
 * 2. 邮箱 + 密码登录（如果用户绑定了邮箱）
 *
 * account 字段可以是手机号或邮箱，后端通过正则区分并分别查询
 */
@Data
public class LoginRequest {

    /**
     * 登录账号
     * 可以是手机号（11位数字）或邮箱（含 @ 符号）
     * @NotBlank: 不能为 null、空字符串或纯空格
     */
    @NotBlank(message = "账号不能为空") // 账号不能为空
    private String account; // 用户输入的手机号或邮箱

    /**
     * 登录密码
     * @NotBlank: 密码不能为空
     * 后端通过 BCrypt 比对数据库中的加密密码进行验证
     */
    @NotBlank(message = "密码不能为空") // 密码不能为空
    private String password; // 用户输入的原始密码
}
