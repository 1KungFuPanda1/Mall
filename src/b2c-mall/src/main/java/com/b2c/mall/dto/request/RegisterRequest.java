package com.b2c.mall.dto.request; // 请求 DTO 包

import jakarta.validation.constraints.NotBlank; // 非空校验注解（Spring Validation）
import jakarta.validation.constraints.Pattern; // 正则校验注解
import jakarta.validation.constraints.Size; // 长度校验注解
import lombok.Data; // Lombok 数据类

/**
 * 用户注册请求 DTO — 封装注册接口的入参
 *
 * 校验规则说明：
 * - 手机号必须是 11 位中国大陆手机号码格式
 * - 密码长度 6~20 位
 * - 验证码为 6 位数字
 * - 所有字段均不能为空
 */
@Data
public class RegisterRequest {

    /**
     * 手机号
     * @NotBlank: 不允许为 null、空字符串、纯空格字符串
     * @Pattern: 正则校验中国大陆手机号格式（1 开头，第二位 3-9，后面 9 位任意数字）
     */
    @NotBlank(message = "手机号不能为空") // 如果手机号为空，返回此错误提示
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") // 正则匹配 11 位手机号
    private String phone; // 用户输入的手机号

    /**
     * 登录密码
     * @Size: 限制密码长度在 6 到 20 个字符之间
     * 密码在存储前会使用 BCrypt 加密，数据库不存明文
     */
    @NotBlank(message = "密码不能为空") // 密码不能为空
    @Size(min = 6, max = 20, message = "密码长度需要在6-20位之间") // 密码长度限制
    private String password; // 用户输入的原始密码

    /**
     * 短信验证码
     * 由后端发送短信接口生成并存入 Redis，用户从短信中获取后填入
     * @NotBlank: 验证码不能为空
     */
    @NotBlank(message = "验证码不能为空") // 验证码不能为空
    private String smsCode; // 用户输入的 6 位短信验证码
}
