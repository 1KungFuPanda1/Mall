package com.b2c.mall.interceptor; // 拦截器包

import com.b2c.mall.enums.RoleEnum; // 角色枚举
import com.b2c.mall.util.JwtUtil; // JWT 工具类
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求对象
import jakarta.servlet.http.HttpServletResponse; // HTTP 响应对象
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.http.HttpStatus; // HTTP 状态码
import org.springframework.stereotype.Component; // Spring 组件
import org.springframework.web.servlet.HandlerInterceptor; // Spring MVC 拦截器接口

/**
 * JWT 认证拦截器
 *
 * 核心功能：
 * 在每个需要认证的请求到达 Controller 之前，拦截请求并进行以下几项检查：
 * 1. 检查请求头中是否携带 Authorization 字段
 * 2. 验证 Token 格式（必须以 "Bearer " 开头）
 * 3. 验证 Token 签名和有效期
 * 4. 根据请求路径判断需要什么角色权限（USER 或 ADMIN）
 * 5. 验证通过后，将用户信息放入请求属性中，放行请求
 *
 * 请求流程：
 * 前端 HTTP 请求 → 拦截器 preHandle() → JWT 校验 → Controller
 *
 * 白名单路径：/api/user/login、/api/user/register、/api/user/send-sms
 * 这些路径在 WebMvcConfig 中配置为不拦截
 *
 * @Component: 标记为 Spring Bean，供 WebMvcConfig 注入
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /** JWT 工具类，用于 Token 解析和验证 */
    @Autowired
    private JwtUtil jwtUtil; // 注入 JWT 工具类

    /**
     * 请求前置处理（在 Controller 方法执行前调用）
     *
     * @param request HTTP 请求对象，包含请求头、参数等
     * @param response HTTP 响应对象，用于设置状态码和响应体
     * @param handler 处理器对象（被调用的 Controller 方法）
     * @return true = 放行请求, false = 拦截请求（不执行 Controller）
     * @throws Exception 可能的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 设置响应内容类型为 JSON（返回错误信息时使用）
        response.setContentType("application/json;charset=UTF-8"); // 设置 UTF-8 编码的 JSON 响应

        // ==================== 步骤0：放行 CORS 预检请求 ====================
        // 浏览器在跨域请求前会发送 OPTIONS 预检请求，该请求不带 Authorization 头
        // 必须直接放行，否则浏览器因预检失败而拒绝实际请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.OK.value()); // 设置 HTTP 200
            return true; // 放行预检请求
        }

        // ==================== 步骤1：获取请求头中的 Authorization 字段 ====================
        // 从 HTTP 请求头中读取 Authorization 字段的值
        // 前端请求格式：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        String authorizationHeader = request.getHeader("Authorization"); // 获取 Authorization 请求头

        // 检查 Authorization 请求头是否存在
        if (authorizationHeader == null) {
            // 请求头不存在 → 用户未登录 → 返回 401
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 设置 HTTP 状态码 401
            response.getWriter().write("{\"message\":\"未登录，请先登录\"}"); // 返回错误信息 JSON
            return false; // 拦截请求，不放行
        }

        // ==================== 步骤2：检查 Token 格式 ====================
        // JWT Token 必须以 "Bearer " 开头（后面有一个空格）
        if (!authorizationHeader.startsWith("Bearer ")) {
            // Token 格式不正确 → 返回 401
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 设置 HTTP 状态码 401
            response.getWriter().write("{\"message\":\"Token格式错误\"}"); // 返回错误信息
            return false; // 拦截请求
        }

        // 提取 "Bearer " 后面的 Token 字符串
        // substring(7) 跳过 "Bearer " 这 7 个字符，获取真正的 Token
        String token = authorizationHeader.substring(7); // 提取纯 Token 字符串

        // ==================== 步骤3：验证 Token 有效性 ====================
        // 调用 JwtUtil 验证 Token 签名和是否过期
        if (!jwtUtil.validateToken(token)) {
            // Token 无效或已过期 → 返回 401
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 设置 HTTP 状态码 401
            response.getWriter().write("{\"message\":\"Token无效或已过期，请重新登录\"}"); // 返回错误信息
            return false; // 拦截请求
        }

        // ==================== 步骤4：角色权限校验 ====================
        // 从 Token 中提取用户角色
        String role = jwtUtil.getRoleFromToken(token); // 获取角色（USER 或 ADMIN）
        // 获取当前请求的 URL 路径（如 /admin/products、/api/cart）
        String requestUri = request.getRequestURI(); // 获取请求路径

        // 如果请求的是后台管理接口（路径以 /admin 开头），则要求角色必须为 ADMIN
        if (requestUri.startsWith("/admin") && !RoleEnum.ADMIN.getCode().equals(role)) {
            // 角色不是管理员 → 返回 403（无权限）
            response.setStatus(HttpStatus.FORBIDDEN.value()); // 设置 HTTP 状态码 403
            response.getWriter().write("{\"message\":\"无权限访问管理后台\"}"); // 返回错误信息
            return false; // 拦截请求
        }

        // ==================== 步骤5：验证通过，设置用户信息并放行 ====================
        // 从 Token 中提取用户 ID
        Long userId = jwtUtil.getUserIdFromToken(token); // 获取用户ID
        // 将用户 ID 和角色存入请求属性中
        // 后续 Controller 可以通过 request.getAttribute("userId") 获取当前登录用户
        request.setAttribute("userId", userId); // 设置用户ID属性
        request.setAttribute("role", role); // 设置角色属性
        return true; // 放行请求，允许进入 Controller
    }
}
