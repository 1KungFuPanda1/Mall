package com.b2c.mall.util; // 工具类包

import io.jsonwebtoken.Claims; // JWT 载荷（Payload）对象，存储 Token 中携带的自定义数据
import io.jsonwebtoken.Jwts; // JWT 构建器和解析器的入口类
import io.jsonwebtoken.SignatureAlgorithm; // JWT 签名算法枚举
import io.jsonwebtoken.security.Keys; // JWT 密钥工具类，用于生成签名密钥
import org.springframework.beans.factory.annotation.Value; // Spring 属性注入注解，从配置文件读取值
import org.springframework.stereotype.Component; // Spring 组件注解，标记为 Bean

import javax.crypto.SecretKey; // 加密密钥类（Javax 加密库）
import java.nio.charset.StandardCharsets; // 标准字符集常量
import java.util.Date; // Java 工具类日期
import java.util.HashMap; // Java 哈希表（键值对集合）
import java.util.Map; // Java Map 接口

/**
 * JWT（JSON Web Token）工具类
 *
 * 核心功能：
 * 1. 生成 Token：用户登录成功后，将用户信息（ID、手机号、角色）加密编码为 JWT 字符串返回
 * 2. 解析 Token：从 HTTP 请求头中提取 Token 并解析出用户信息
 * 3. 验证 Token：校验 Token 是否过期、签名是否正确、是否被篡改
 *
 * JWT 结构说明：
 * Token 由三部分组成，以点号分隔：Header.Payload.Signature
 * - Header:  声明签名算法类型（HS256）
 * - Payload:  存储自定义数据（用户ID、角色等），Base64 编码，不加密（不要存敏感信息）
 * - Signature: 对 Header + Payload 的签名结果，用于防止数据篡改
 *
 * Token 流程图：
 * 用户登录 → 后端生成 Token → 返回给前端 → 前端存入缓存
 * → 后续请求在 Header 中携带 Token → 后端拦截器解析并校验
 *
 * @Component: 将此类标记为 Spring 管理的 Bean，可以被 @Autowired 注入到其他组件中
 */
@Component
public class JwtUtil {

    /**
     * JWT 签名密钥
     * 从 application-dev.yml 配置文件中读取 app.jwt.secret 的值
     * 密钥用于对 Token 进行数字签名，确保 Token 不被篡改
     */
    @Value("${app.jwt.secret}") // 从配置文件中注入密钥值
    private String secret; // JWT 签名密钥字符串

    /**
     * JWT Token 过期时间（毫秒）
     * 从 application-dev.yml 配置文件中读取 app.jwt.expiration 的值
     * 默认 86400000 毫秒 = 24 小时
     */
    @Value("${app.jwt.expiration}") // 从配置文件中注入过期时间
    private Long expiration; // Token 有效期（毫秒）

    /**
     * 根据密钥字符串生成加密密钥对象（SecretKey）
     *
     * SecretKey 是 JWT 签名算法所需的密钥对象
     * 使用 HMAC-SHA256 算法至少需要 256 位（32 字节）的密钥
     * Keys.hmacShaKeyFor() 方法将字节数组转换为符合算法要求的密钥对象
     *
     * @return SecretKey 加密密钥对象，用于 JWT 的签名和验证
     */
    private SecretKey getSigningKey() {
        // 将密钥字符串转为 UTF-8 字节数组
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8); // 获取密钥的字节表示
        // 使用 HMAC-SHA256 算法生成签名密钥对象
        return Keys.hmacShaKeyFor(keyBytes); // 返回符合算法要求的密钥对象
    }

    /**
     * 生成 JWT Token
     *
     * Token 生成步骤：
     * 1. 获取当前时间
     * 2. 计算过期时间 = 当前时间 + 配置的过期时长
     * 3. 设置 Payload（用户信息 + 过期时间 + 签发时间）
     * 4. 使用 HMAC-SHA256 算法 + 密钥进行签名
     *
     * Payload 中包含的字段：
     * - userId: 用户主键 ID（用于后续查询用户数据、关联订单等）
     * - phone: 用户手机号（用于日志记录和展示）
     * - role: 用户角色（用于权限校验）
     *
     * @param userId 用户主键 ID（登录成功后从数据库查询得到）
     * @param phone 用户手机号
     * @param role 用户角色（USER 或 ADMIN）
     * @return 生成的 JWT Token 字符串，格式为 xxx.yyy.zzz
     */
    public String generateToken(Long userId, String phone, String role) {
        // 获取当前系统时间（毫秒时间戳）
        Date now = new Date(); // 签发时间

        // 计算过期时间 = 当前时间 + 配置的有效期
        Date expirationDate = new Date(now.getTime() + expiration); // 过期时间点

        // 构造 JWT Payload 中的自定义声明（Claims）
        Map<String, Object> claims = new HashMap<>(); // 创建 Payload 数据容器
        claims.put("userId", userId); // 存入用户ID
        claims.put("phone", phone); // 存入用户手机号
        claims.put("role", role); // 存入用户角色

        // 构建 JWT Token 字符串
        return Jwts.builder() // 创建 JWT 构建器
                .claims(claims) // 设置自定义声明数据（Payload）
                .subject(String.valueOf(userId)) // 设置主题（通常为用户ID字符串）
                .issuedAt(now) // 设置签发时间（iat 字段）
                .expiration(expirationDate) // 设置过期时间（exp 字段）
                .signWith(getSigningKey()) // 使用 HMAC-SHA256 算法 + 密钥进行签名
                .compact(); // 完成构建，返回三部分的 Token 字符串
    }

    /**
     * 解析 JWT Token，提取其中的所有声明数据（Claims）
     *
     * Claims 是 JWT Payload 中的键值对数据，包含用户 ID、手机号、角色等信息
     * 如果 Token 无效（过期、签名错误、被篡改），此方法会抛出异常
     *
     * 解析流程：
     * 1. 创建 JWT 解析器，设置签名密钥
     * 2. 解析 Token 字符串，得到 Claims 对象
     * 3. 从 Claims 中读取业务数据
     *
     * @param token JWT Token 字符串（从 HTTP 请求头 Authorization: Bearer xxx 中提取）
     * @return Claims 对象，包含 Payload 中的所有声明数据
     */
    public Claims parseToken(String token) {
        // 创建 JWT 解析器，设置验证密钥
        return Jwts.parser() // 创建解析器构建器
                .verifyWith(getSigningKey()) // 设置签名验证密钥（签名不匹配会抛异常）
                .build() // 构建解析器
                .parseSignedClaims(token) // 解析 Token 并返回 Claims（过期会抛异常）
                .getPayload(); // 获取 Payload 部分的数据
    }

    /**
     * 从 Token 中提取用户 ID
     *
     * 解析 Token → 获取 Claims → 读取 userId 字段
     *
     * @param token JWT Token 字符串
     * @return 用户主键 ID（Long 类型）
     */
    public Long getUserIdFromToken(String token) {
        // 解析 Token 获取 Claims
        Claims claims = parseToken(token); // 解析 Token
        // 从 Claims 中取出 userId，从 Object 转为 Long
        return claims.get("userId", Long.class); // 返回用户ID
    }

    /**
     * 从 Token 中提取用户角色
     *
     * 用途：在拦截器中校验用户是否具有访问某接口的权限
     *
     * @param token JWT Token 字符串
     * @return 用户角色（"USER" 或 "ADMIN"）
     */
    public String getRoleFromToken(String token) {
        // 解析 Token 获取 Claims
        Claims claims = parseToken(token); // 解析 Token
        // 从 Claims 中取出 role，转换为 String
        return claims.get("role", String.class); // 返回用户角色
    }

    /**
     * 从 Token 中提取用户手机号
     *
     * 用途：日志记录、业务校验等
     *
     * @param token JWT Token 字符串
     * @return 用户手机号
     */
    public String getPhoneFromToken(String token) {
        // 解析 Token 获取 Claims
        Claims claims = parseToken(token); // 解析 Token
        // 从 Claims 中取出 phone，转换为 String
        return claims.get("phone", String.class); // 返回用户手机号
    }

    /**
     * 判断 Token 是否已过期
     *
     * 通过比较 Token 中的过期时间与当前系统时间来判断
     *
     * @param token JWT Token 字符串
     * @return true = Token 已过期, false = Token 仍在有效期内
     */
    public boolean isTokenExpired(String token) {
        // 解析 Token 获取 Claims
        Claims claims = parseToken(token); // 解析 Token
        // 获取过期时间
        Date expirationDate = claims.getExpiration(); // 取出 exp 字段
        // 比较过期时间是否在当前时间之前
        return expirationDate.before(new Date()); // true = 已过期, false = 未过期
    }

    /**
     * 验证 Token 是否有效
     *
     * 验证逻辑：
     * 1. 解析 Token（签名校验 + 格式校验）
     * 2. 检查是否过期
     *
     * @param token JWT Token 字符串
     * @return true = Token 有效且未过期, false = Token 无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            // 尝试解析 Token（签名验证 + 过期检查）
            parseToken(token); // 成功解析说明签名和格式都正确
            return true; // 解析成功，Token 有效
        } catch (Exception e) {
            // 任何异常（过期、签名错误、格式错误）都视为 Token 无效
            return false; // Token 无效
        }
    }
}
