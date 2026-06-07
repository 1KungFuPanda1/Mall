package com.b2c.mall.config; // 配置类包

import com.b2c.mall.interceptor.JwtInterceptor; // JWT 拦截器
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.context.annotation.Configuration; // 配置类注解
import org.springframework.web.servlet.config.annotation.CorsRegistry; // CORS 跨域注册器
import org.springframework.web.servlet.config.annotation.InterceptorRegistry; // 拦截器注册器
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // 资源处理器注册器
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Spring MVC 配置接口

/**
 * Spring MVC Web 配置类
 *
 * 主要功能：
 * 1. 注册 JWT 拦截器，指定需要拦截和放行的路径
 * 2. 配置文件上传虚拟路径映射（访问本地图片文件）
 *
 * 实现 WebMvcConfigurer 接口，可以覆写 addInterceptors()、addResourceHandlers() 等方法
 *
 * @Configuration: 标明此类为 Spring 配置类，Spring 容器会扫描并执行其中的 Bean 定义和方法
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** JWT 拦截器，通过 @Autowired 注入 */
    @Autowired
    private JwtInterceptor jwtInterceptor; // 注入 JWT 认证拦截器

    /**
     * 配置 CORS 跨域 — 允许前端 H5 页面跨域访问后端 API
     *
     * 前置条件：HBuilderX 运行时 Vite 代理可能不生效，前端直接请求后端 IP 属于跨域，
     * 浏览器会触发 CORS 预检（OPTIONS 请求），必须后端明确放行才能通信。
     *
     * @param registry CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有路径均允许跨域
                .allowedOriginPatterns("*") // 允许所有来源（开发环境，生产需限制域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*") // 允许所有请求头（含 Authorization）
                .allowCredentials(true) // 允许携带 Cookie / Token
                .maxAge(3600); // 预检请求缓存 1 小时
    }

    /**
     * 配置拦截器 — 指定哪些路径需要认证、哪些路径不需要
     *
     * 拦截规则说明：
     * - addPathPatterns("/api/**"): 拦截所有 C 端 API 请求
     * - addPathPatterns("/admin/**"): 拦截所有后台管理 API 请求
     * - excludePathPatterns(...): 排除不需要认证的路径（白名单）
     *
     * 白名单路径（无需登录即可访问）：
     * - /api/user/login: 用户登录
     * - /api/user/register: 用户注册
     * - /api/user/send-sms: 发送短信验证码
     * - /api/products/**: 商品浏览（所有人都能看商品）
     * - /api/banners: 轮播图（不需要登录）
     * - /api/categories: 商品分类（不需要登录）
     * - /admin/login: 管理员登录（后台登录页需要放行）
     *
     * @param registry 拦截器注册器，用于配置拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor) // 注册 JWT 拦截器
                .addPathPatterns("/api/**") // 拦截所有 /api/ 下的请求
                .addPathPatterns("/admin/**") // 拦截所有 /admin/ 下的请求
                .excludePathPatterns( // 白名单：以下路径不需要认证
                        "/api/user/login", // 用户登录
                        "/api/user/register", // 用户注册
                        "/api/user/send-sms", // 发送短信验证码
                        "/api/products/**", // 商品浏览（含搜索、详情）
                        "/api/banners", // 轮播图
                        "/api/categories", // 商品分类
                        "/admin/login" // 管理员登录
                );
    }

    /**
     * 配置静态资源映射 — 将本地文件路径映射为 URL 访问路径
     *
     * 功能说明：
     * 商品图片上传后存储在本地磁盘的 d:/uploads 目录下，
     * 通过虚拟路径映射，前端可以通过 /uploads/xxx.jpg 访问这些文件。
     *
     * 例如：文件存储在 d:/uploads/123.jpg
     *      映射后可通过 http://localhost:8080/uploads/123.jpg 访问
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 URL 路径 /uploads/** 映射到本地文件系统路径 d:/uploads/
        registry.addResourceHandler("/uploads/**") // URL 匹配模式
                .addResourceLocations("file:d:/uploads/"); // 映射到本地文件路径
    }
}
