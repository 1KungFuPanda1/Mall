package com.b2c.mall.exception; // 异常类包

/**
 * 自定义业务异常类
 *
 * 设计目的：
 * 将业务逻辑中预期的错误（如"库存不足"、"密码错误"等）与系统级别的未知异常区分开来。
 *
 * 使用方式：
 * 在 Service 层中，当业务规则校验不通过时，手动抛出 BusinessException：
 *   throw new BusinessException("库存不足");
 * 然后由 GlobalExceptionHandler 统一捕获并返回友好的错误响应给前端。
 *
 * 优势：
 * 1. 异常信息更友好，不暴露系统内部细节
 * 2. 统一异常处理，减少 Controller 中的 try-catch 模板代码
 * 3. 便于区分业务异常（预期内）和系统异常（预期外）
 */
public class BusinessException extends RuntimeException { // 继承运行时异常（不需要显式 catch）

    /**
     * 构造方法：传入错误提示信息
     *
     * @param message 业务错误描述（如"用户名已存在"、"库存不足"等）
     */
    public BusinessException(String message) {
        super(message); // 调用父类 RuntimeException 的构造方法，设置异常信息
    }
}
