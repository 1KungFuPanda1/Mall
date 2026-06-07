package com.b2c.mall.service.impl; // Service 实现类包

import cn.hutool.core.util.RandomUtil; // Hutool 工具类：随机数生成
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus Lambda 条件构造器
import com.b2c.mall.dto.request.LoginRequest; // 登录请求 DTO
import com.b2c.mall.dto.request.RegisterRequest; // 注册请求 DTO
import com.b2c.mall.dto.response.LoginResponse; // 登录响应 DTO
import com.b2c.mall.entity.User; // 用户实体
import com.b2c.mall.enums.RoleEnum; // 角色枚举
import com.b2c.mall.exception.BusinessException; // 业务异常
import com.b2c.mall.mapper.UserMapper; // 用户 Mapper
import com.b2c.mall.service.UserService; // 用户 Service 接口
import com.b2c.mall.util.JwtUtil; // JWT 工具
import com.b2c.mall.util.RedisUtil; // Redis 工具
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.stereotype.Service; // Service 层注解
import org.springframework.transaction.annotation.Transactional; // 事务注解

/**
 * 用户服务实现类
 *
 * 实现 UserService 接口中定义的所有业务方法
 * @Service: 标记为 Spring Service 层 Bean
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper; // 注入用户 Mapper，用于数据库 CRUD 操作

    @Autowired
    private RedisUtil redisUtil; // 注入 Redis 工具，用于操作验证码缓存

    @Autowired
    private JwtUtil jwtUtil; // 注入 JWT 工具，用于生成和解析 Token

    /** 短信验证码在 Redis 中的 key 前缀 */
    private static final String SMS_CODE_PREFIX = "sms_code:"; // Redis key: sms_code:{phone}

    /** 短信验证码有效期（秒）— 5 分钟 */
    private static final long SMS_CODE_TTL = 300L; // 5分钟 = 300秒

    /** 短信验证码在 Redis 中的 key 前缀用于邮箱验证码 */
    private static final String EMAIL_CODE_PREFIX = "email_code:"; // Redis key: email_code:{email}

    /**
     * 发送短信验证码
     *
     * 实现步骤：
     * 1. 生成 6 位随机数字验证码
     * 2. 存入 Redis，设置 5 分钟过期时间
     * 3. 通过阿里云短信 SDK 发送短信到用户手机
     *    （如果阿里云未配置，验证码存入 Redis 前端可通过调试接口获取）
     *
     * Redis Key 格式：sms_code:13800138000 → "123456"
     *
     * @param phone 接收验证码的手机号
     */
    @Override
    public void sendSmsCode(String phone) {
        // 步骤1：生成 6 位随机数字验证码
        // RandomUtil.randomNumbers(6) 生成如 "384921" 的 6 位随机数字字符串
        String code = RandomUtil.randomNumbers(6); // 生成 6 位随机验证码

        // 步骤2：将验证码存入 Redis，设置 5 分钟过期
        String redisKey = SMS_CODE_PREFIX + phone; // 组装 Redis Key：sms_code:13800138000
        redisUtil.set(redisKey, code, SMS_CODE_TTL, java.util.concurrent.TimeUnit.SECONDS); // 存入Redis，5分钟过期

        // 步骤3：通过阿里云短信 SDK 发送短信
        // 注意：此处调用阿里云短信服务发送验证码（需配置阿里云 AccessKey）
        // 如果阿里云未配置，开发调试时可在控制台打印或通过 Redis 查看验证码
        System.out.println("【开发调试】手机号 " + phone + " 的短信验证码为：" + code); // 控制台输出验证码便于调试

        // TODO: 接入阿里云短信 SDK 发送真实短信
        // smsService.sendSms(phone, code);
    }

    /**
     * 用户注册
     *
     * 业务规则：
     * 1. 校验手机号是否已被注册（唯一性检查）
     * 2. 校验短信验证码是否正确（从 Redis 中取出比对）
     * 3. 密码使用 BCrypt 加密存储（不存明文）
     * 4. 新用户默认角色为 USER（普通用户）
     *
     * @param request 注册请求参数（手机号、密码、验证码）
     */
    @Override
    @Transactional // 数据库写操作需要事务保障
    public void register(RegisterRequest request) {
        // ============ 步骤1：校验手机号唯一性 ============
        // 使用 LambdaQueryWrapper 构造查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(); // 创建查询条件构造器
        wrapper.eq(User::getPhone, request.getPhone()); // WHERE phone = '13800138000'
        // 查询数据库中是否存在该手机号
        Long count = userMapper.selectCount(wrapper); // SELECT COUNT(*) FROM t_user WHERE phone = ?
        if (count > 0) {
            // 手机号已被注册 → 抛出业务异常，由全局异常处理器捕获
            throw new BusinessException("该手机号已被注册"); // 提示用户
        }

        // ============ 步骤2：校验短信验证码 ============
        // 从 Redis 中获取之前发送的验证码
        String cachedCode = (String) redisUtil.get(SMS_CODE_PREFIX + request.getPhone()); // 获取Redis中存储的验证码
        if (cachedCode == null) {
            // Redis 中没有验证码 → 可能已过期或未发送
            throw new BusinessException("验证码已过期，请重新获取"); // 提示用户重新获取
        }
        // 比对用户输入的验证码与 Redis 中的验证码
        if (!cachedCode.equals(request.getSmsCode())) {
            // 验证码不匹配
            throw new BusinessException("验证码错误"); // 提示用户
        }

        // ============ 步骤3：密码加密并创建用户 ============
        User user = new User(); // 创建用户实体对象
        user.setPhone(request.getPhone()); // 设置手机号
        // 使用 BCrypt 加密密码（BCrypt 是单向加密算法，无法反向解密）
        // 注意：需要注入 BCryptPasswordEncoder，这里简化为直接使用 Hutool 的加密工具
        user.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(request.getPassword())); // BCrypt 加密存储密码
        user.setRole(RoleEnum.USER.getCode()); // 设置角色为普通用户
        user.setStatus(1); // 设置状态为启用
        user.setNickname("用户" + request.getPhone().substring(7)); // 默认昵称：用户 + 手机号后4位

        // 插入用户到数据库
        userMapper.insert(user); // INSERT INTO t_user (...) VALUES (...)

        // ============ 步骤4：注册成功后删除验证码 ============
        redisUtil.delete(SMS_CODE_PREFIX + request.getPhone()); // 删除 Redis 中的验证码，防止重复使用
    }

    /**
     * 用户登录
     *
     * 登录流程：
     * 1. 根据账号（手机号或邮箱）查询用户
     * 2. 检查用户状态是否启用
     * 3. 校验密码（BCrypt 比对）
     * 4. 生成 JWT Token 并返回用户信息
     *
     * @param request 登录请求参数（账号、密码）
     * @return LoginResponse 包含 JWT Token 和用户基本信息
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // ============ 步骤1：查询用户 ============
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(); // 创建查询条件
        // 判断账号是否为邮箱（包含 @ 符号）
        if (request.getAccount().contains("@")) {
            wrapper.eq(User::getEmail, request.getAccount()); // 用邮箱查询
        } else {
            wrapper.eq(User::getPhone, request.getAccount()); // 用手机号查询
        }
        User user = userMapper.selectOne(wrapper); // 查询用户
        if (user == null) {
            // 用户不存在
            throw new BusinessException("账号不存在"); // 提示用户
        }

        // ============ 步骤2：检查账户状态 ============
        if (user.getStatus() == 0) {
            // 账户已被管理员禁用
            throw new BusinessException("账户已被禁用，请联系管理员"); // 提示用户
        }

        // ============ 步骤3：校验密码 ============
        // BCrypt.checkpw(明文密码, 密文密码) 比较是否匹配
        if (!cn.hutool.crypto.digest.BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            // 密码不正确
            throw new BusinessException("密码错误"); // 提示用户
        }

        // ============ 步骤4：生成 JWT Token ============
        // 使用用户ID、手机号、角色生成 Token（有效期 24 小时）
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole()); // 生成JWT

        // ============ 步骤5：构建响应 ============
        return LoginResponse.builder() // 使用建造者模式构建响应对象
                .token(token) // JWT Token
                .userId(user.getId()) // 用户ID
                .phone(user.getPhone()) // 手机号
                .nickname(user.getNickname()) // 昵称
                .role(user.getRole()) // 角色
                .build(); // 构建完成
    }

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param userId 用户主键 ID
     * @return User 实体（不含密码字段的处理由前端或序列化控制）
     */
    @Override
    public User getUserById(Long userId) {
        // 使用 MyBatis-Plus 的 selectById 方法查询
        User user = userMapper.selectById(userId); // SELECT * FROM t_user WHERE id = ?
        if (user == null) {
            throw new BusinessException("用户不存在"); // 用户不存在
        }
        return user; // 返回用户实体
    }
}
