package com.b2c.mall.exception; // 异常处理包

import org.springframework.http.HttpStatus; // HTTP 标准状态码枚举
import org.springframework.http.ResponseEntity; // Spring HTTP 响应实体类
import org.springframework.validation.FieldError; // 字段校验错误对象
import org.springframework.web.bind.MethodArgumentNotValidException; // 参数校验异常类
import org.springframework.web.bind.annotation.ExceptionHandler; // 异常处理方法注解
import org.springframework.web.bind.annotation.RestControllerAdvice; // 全局控制器增强注解

import java.util.HashMap; // Java 哈希表
import java.util.Map; // Java Map 接口

/**
 * 全局异常处理器
 *
 * 设计说明：
 * 使用 Spring 的 @RestControllerAdvice 注解，拦截所有 Controller 中抛出的异常，
 * 统一处理并返回规范的 RESTful 风格响应。
 *
 * 响应策略（遵循 RESTful 标准）：
 * - 业务异常 → HTTP 400 Bad Request
 * - 参数校验异常 → HTTP 400 Bad Request
 * - 未知系统异常 → HTTP 500 Internal Server Error
 *
 * 响应体为 JSON 格式：{ "message": "错误描述" }
 *
 * @RestControllerAdvice: 组合注解
 * - @ControllerAdvice: 全局拦截所有 Controller
 * - @ResponseBody: 返回 JSON 而非视图页面
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     *
     * 触发时机：Service 层代码中 throw new BusinessException("xxx") 时
     * 返回 HTTP 400（请求错误），并携带业务友好的错误描述
     *
     * @ExceptionHandler: 指定此方法要拦截的异常类型
     *
     * @param e 被捕获的业务异常对象，从中提取错误信息
     * @return ResponseEntity 包含 400 状态码和错误信息 JSON
     */
    @ExceptionHandler(BusinessException.class) // 只处理 BusinessException 类型的异常
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException e) {
        // 创建响应体，message 字段存储业务错误描述
        Map<String, String> body = new HashMap<>(); // 创建 Map 作为响应体
        body.put("message", e.getMessage()); // 将异常信息放入 message 字段
        // 返回 HTTP 400 + JSON 响应体
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400 Bad Request
    }

    /**
     * 处理参数校验异常（使用 @Valid 注解的请求参数校验失败时触发）
     *
     * 触发时机：前端请求参数不满足 DTO 中的校验注解时
     * 例如：手机号格式不正确、密码长度不足等
     *
     * MethodArgumentNotValidException 中包含所有校验失败的字段和错误信息
     *
     * @param e 参数校验异常对象
     * @return ResponseEntity 包含 400 状态码和第一个校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // 只处理参数校验异常
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        // 获取校验失败的第一个字段错误信息
        FieldError fieldError = e.getBindingResult().getFieldError(); // 获取第一个校验失败的字段
        // 获取该字段的错误提示消息（即 DTO 注解中 message 的值，如"手机号格式不正确"）
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败"; // 错误信息
        // 创建响应体
        Map<String, String> body = new HashMap<>(); // 创建 Map 作为响应体
        body.put("message", message); // 将错误信息放入 message 字段
        // 返回 HTTP 400
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400 Bad Request
    }

    /**
     * 处理所有未预期的系统异常（兜底异常处理）
     *
     * 触发时机：任何未被其他 @ExceptionHandler 捕获的异常
     * 例如：空指针异常（NullPointerException）、数据库连接失败等
     *
     * 注意：生产环境不应将原始异常信息返回给前端（安全考虑），开发阶段可返回便于调试
     *
     * @param e 系统异常对象
     * @return ResponseEntity 包含 500 状态码和通用错误信息
     */
    @ExceptionHandler(Exception.class) // 处理所有未指定类型的异常（兜底）
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        // 开发阶段打印异常堆栈，便于调试定位问题
        e.printStackTrace(); // 控制台打印异常详细堆栈信息
        // 创建响应体（开发阶段返回具体异常信息便于调试，上线后可改为通用提示）
        Map<String, String> body = new HashMap<>(); // 创建 Map 作为响应体
        body.put("message", "服务器内部错误：" + e.getMessage()); // 异常信息
        // 返回 HTTP 500 + JSON 响应体
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body); // 500 Internal Server Error
    }
}
