package com.b2c.mall.controller.user; // C端用户控制器包

import com.b2c.mall.dto.request.LoginRequest; // 登录请求 DTO
import com.b2c.mall.dto.request.RegisterRequest; // 注册请求 DTO
import com.b2c.mall.dto.request.SendSmsRequest; // 短信验证码请求 DTO
import com.b2c.mall.dto.response.LoginResponse; // 登录响应 DTO
import com.b2c.mall.entity.User; // 用户实体
import com.b2c.mall.service.UserService; // 用户 Service
import io.swagger.v3.oas.annotations.Operation; // Swagger 接口说明注解
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger 接口分组注解
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求对象
import jakarta.validation.Valid; // 参数校验注解
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.http.HttpStatus; // HTTP 状态码
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // RESTful 注解集合

/**
 * C端用户控制器 — 处理用户注册、登录、个人信息等接口
 *
 * RESTful 接口响应遵循标准 HTTP 状态码：
 * - 200 OK: 请求成功
 * - 201 Created: 资源创建成功（如注册）
 * - 400 Bad Request: 参数错误或业务异常
 *
 * @RestController: 组合注解，@Controller + @ResponseBody，所有方法返回 JSON
 * @RequestMapping("/api/user"): 定义基础路径，所有方法路径以此为前缀
 * @Tag: Knife4j/Swagger 接口分组标签
 */
@RestController // RESTful 控制器
@RequestMapping("/api/user") // C端用户接口基础路径
@Tag(name = "C端-用户模块", description = "用户注册、登录、个人信息管理接口") // Swagger 分组
public class UserController {

    @Autowired
    private UserService userService; // 注入用户 Service

    /**
     * 发送短信验证码
     *
     * 接口路径：POST /api/user/send-sms
     * 无需登录即可访问（已在拦截器白名单中）
     *
     * @param request 发送短信请求（含手机号）
     * @return 200 OK
     */
    @PostMapping("/send-sms") // POST /api/user/send-sms
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送6位数字验证码，有效期5分钟") // Swagger 说明
    public ResponseEntity<Void> sendSms(@Valid @RequestBody SendSmsRequest request) {
        // @Valid: 触发 DTO 中的校验注解（如手机号格式校验）
        // @RequestBody: 从 HTTP 请求体中反序列化 JSON 到 Java 对象
        userService.sendSmsCode(request.getPhone()); // 调用 Service 发送验证码
        return ResponseEntity.ok().build(); // 返回 HTTP 200 空响应体
    }

    /**
     * 用户注册
     *
     * 接口路径：POST /api/user/register
     * 无需登录即可访问
     *
     * @param request 注册请求（手机号、密码、验证码）
     * @return 201 Created
     */
    @PostMapping("/register") // POST /api/user/register
    @Operation(summary = "用户注册", description = "手机号+验证码+密码注册新账号") // Swagger 说明
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request); // 调用 Service 完成注册（含验证码校验、密码加密等）
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 返回 HTTP 201 表示资源创建成功
    }

    /**
     * 用户登录
     *
     * 接口路径：POST /api/user/login
     * 无需登录即可访问
     *
     * @param request 登录请求（账号、密码）
     * @return 200 OK + LoginResponse（JWT Token + 用户信息）
     */
    @PostMapping("/login") // POST /api/user/login
    @Operation(summary = "用户登录", description = "手机号/邮箱 + 密码登录，返回JWT Token") // Swagger 说明
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request); // 调用 Service 登录
        return ResponseEntity.ok(response); // 返回 HTTP 200 + Token 信息
    }

    /**
     * 获取当前登录用户信息
     *
     * 需要登录后携带 Token 才能访问
     * 用户ID从拦截器设置的 request 属性中获取（JwtInterceptor 解析 Token 后设置）
     *
     * @param request HTTP 请求对象（用于获取拦截器存入的 userId）
     * @return 200 OK + User 实体
     */
    @GetMapping("/info") // GET /api/user/info
    @Operation(summary = "获取当前用户信息", description = "根据JWT Token获取当前登录用户的详细信息") // Swagger 说明
    public ResponseEntity<User> getUserInfo(HttpServletRequest request) {
        // 从请求属性中获取用户ID（由 JwtInterceptor 在 preHandle 中设置）
        Long userId = (Long) request.getAttribute("userId"); // 获取拦截器存入的 userId
        User user = userService.getUserById(userId); // 查询用户信息
        // 清除密码字段（安全考虑，不将密码返回给前端）
        user.setPassword(null); // 置空密码
        return ResponseEntity.ok(user); // 返回 HTTP 200 + 用户信息
    }
}
