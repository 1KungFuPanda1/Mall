package com.b2c.mall.dto.request; // 请求 DTO 包

import jakarta.validation.constraints.NotBlank; // 非空校验注解
import jakarta.validation.constraints.Pattern; // 正则校验注解
import lombok.Data; // Lombok 数据类

/**
 * 发送短信验证码请求 DTO
 *
 * 用途：用户注册时，先调用此接口获取短信验证码
 * 验证码生成后存储在 Redis 中（key = "sms_code:手机号"，TTL = 5分钟）
 * 同时通过阿里云短信 SDK 发送到用户手机
 */
@Data
public class SendSmsRequest {

    /**
     * 接收验证码的手机号
     * @NotBlank: 手机号不能为空
     * @Pattern: 校验 11 位中国大陆手机号码格式
     */
    @NotBlank(message = "手机号不能为空") // 手机号不能为空
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") // 正则校验手机号格式
    private String phone; // 目标手机号码
}
