package com.b2c.mall.util; // 工具类包

import org.springframework.beans.factory.annotation.Autowired; // Spring 自动注入注解
import org.springframework.data.redis.core.HashOperations; // Redis Hash 操作接口
import org.springframework.data.redis.core.RedisTemplate; // Spring Redis 操作模板类
import org.springframework.data.redis.core.ValueOperations; // Redis String（键值对）操作接口
import org.springframework.stereotype.Component; // Spring 组件注解

import java.util.Map; // Java Map 接口
import java.util.Set; // Java Set 接口
import java.util.concurrent.TimeUnit; // 时间单位枚举（秒、毫秒、分钟等）

/**
 * Redis 缓存工具类 — 封装 Spring RedisTemplate 的常用操作方法
 *
 * 功能介绍：
 * - String 类型操作：用于存储验证码、JWT Token、商品缓存数据
 * - Hash 类型操作：用于存储购物车数据（每个用户一个 Hash）
 * - 提供统一的 TTL（过期时间）管理
 *
 * 使用场景对应：
 * 1. sms_code:{phone} → 短信验证码，TTL 300秒
 * 2. token:user:{userId} → JWT Token，TTL 86400秒
 * 3. product:hot:list → 首页热门商品缓存，TTL 1800秒
 * 4. product:detail:{id} → 商品详情缓存，TTL 1800秒
 * 5. cart:{userId} → 购物车 Hash，永不过期（仅在用户清空或下单时主动删除）
 *
 * @Component: 将此工具类标记为 Spring Bean，可被其他 Service 注入使用
 */
@Component
public class RedisUtil {

    /**
     * Spring Redis 操作模板
     * RedisTemplate 是 Spring Data Redis 的核心类，封装了对 Redis 的所有操作
     * 支持泛型，可以指定 key 和 value 的类型
     * 通过 @Autowired 自动注入 Spring 容器中已配置好的 RedisTemplate Bean
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // Redis 操作模板

    // ==================== String（字符串）类型操作 ====================

    /**
     * 设置键值对（永不过期）
     *
     * 用途：极少使用，大多数场景都需要设置过期时间
     *
     * @param key Redis 中的键名（如 "sms_code:13800138000"）
     * @param value 存储的值（可以是字符串、数字、JSON对象等）
     */
    public void set(String key, Object value) {
        // 获取 String 类型操作接口
        ValueOperations<String, Object> ops = redisTemplate.opsForValue(); // 获取 ValueOperations
        ops.set(key, value); // 执行 SET 操作
    }

    /**
     * 设置键值对（带过期时间）
     *
     * 这是最常用的方法，所有缓存数据都应设置合理的 TTL
     *
     * @param key Redis 中的键名
     * @param value 存储的值
     * @param timeout 过期时长数值
     * @param unit 时间单位（如 TimeUnit.SECONDS 秒、TimeUnit.MINUTES 分钟）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        // 获取 String 类型操作接口
        ValueOperations<String, Object> ops = redisTemplate.opsForValue(); // 获取 ValueOperations
        // 执行 SET 操作并设置过期时间
        ops.set(key, value, timeout, unit); // SET key value EX timeout
    }

    /**
     * 根据键获取值
     *
     * 从 Redis 中查询指定 key 对应的 value
     * 如果 key 不存在或已过期，返回 null
     *
     * @param key Redis 中的键名
     * @return 键对应的值（Object 类型，需要调用方自行强转），key 不存在返回 null
     */
    public Object get(String key) {
        // 获取 String 类型操作接口
        ValueOperations<String, Object> ops = redisTemplate.opsForValue(); // 获取 ValueOperations
        return ops.get(key); // 执行 GET 操作，返回 value 或 null
    }

    /**
     * 判断键是否存在
     *
     * 用途：判断验证码是否已过期、Token 是否仍有效等
     *
     * @param key Redis 中的键名
     * @return true = 键存在且未过期, false = 键不存在或已过期
     */
    public boolean hasKey(String key) {
        // hasKey 方法底层执行 EXISTS 命令
        return Boolean.TRUE.equals(redisTemplate.hasKey(key)); // 返回是否存在（处理 null 为 false）
    }

    /**
     * 删除指定的键
     *
     * 用途：验证码使用后删除、商品更新时清除缓存等
     *
     * @param key Redis 中的键名
     * @return true = 删除成功, false = 键不存在或无删除
     */
    public boolean delete(String key) {
        // delete 方法底层执行 DEL 命令，返回 true 表示至少删除了一个键
        return Boolean.TRUE.equals(redisTemplate.delete(key)); // 执行删除并返回结果
    }

    /**
     * 设置指定键的过期时间
     *
     * 用途：对已有的 key 设置或延长过期时间
     *
     * @param key Redis 中的键名
     * @param timeout 过期时长数值
     * @param unit 时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        // expire 方法底层执行 EXPIRE 命令
        redisTemplate.expire(key, timeout, unit); // 设置过期时间
    }

    // ==================== Hash（哈希表）类型操作 ====================

    /**
     * 向 Hash 表中添加一个字段（field → value）
     *
     * Hash 结构类比 Java 中的 Map<String, Map<String, Object>>：
     * - 外层 key = Redis 中的键（如 "cart:1001"）
     * - 内层 field = 字段名（如 "101" = 商品ID）
     * - value = 字段值（如 '{"productName":"xxx",...}')
     *
     * 用途：购物车设计中，key = cart:用户ID, field = 商品ID, value = 购物车商品JSON
     *
     * @param key Redis 键（如 "cart:1001"）
     * @param field Hash 中的字段名（如商品ID "101"）
     * @param value 字段值（如购物车商品 JSON 字符串）
     */
    public void hSet(String key, String field, Object value) {
        // 获取 Hash 类型操作接口
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash(); // 获取 HashOperations
        // 执行 HSET 操作，向 Hash 中添加一个键值对
        ops.put(key, field, value); // HSET cart:1001 101 '{"name":"xxx"}'
    }

    /**
     * 从 Hash 表中获取一个字段的值
     *
     * 用途：查询购物车中某个商品的信息
     *
     * @param key Redis 键
     * @param field Hash 中的字段名
     * @return 字段对应的值，不存在返回 null
     */
    public Object hGet(String key, String field) {
        // 获取 Hash 类型操作接口
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash(); // 获取 HashOperations
        // 执行 HGET 操作，获取指定字段的值
        return ops.get(key, field); // HGET cart:1001 101
    }

    /**
     * 从 Hash 表中删除一个或多个字段
     *
     * 用途：从购物车中删除某个商品、下单后清空已选商品
     *
     * @param key Redis 键
     * @param fields 要删除的字段名数组
     * @return 实际删除的字段数量
     */
    public Long hDel(String key, Object... fields) {
        // 获取 Hash 类型操作接口
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash(); // 获取 HashOperations
        // 执行 HDEL 操作，删除指定字段
        return ops.delete(key, fields); // HDEL cart:1001 101 102
    }

    /**
     * 获取 Hash 表中所有字段名
     *
     * 用途：获取购物车中所有商品 ID 列表
     *
     * @param key Redis 键
     * @return 所有字段名的 Set 集合
     */
    public Set<String> hKeys(String key) {
        // 获取 Hash 类型操作接口
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash(); // 获取 HashOperations
        // 执行 HKEYS 操作，获取所有字段名
        return ops.keys(key); // HKEYS cart:1001 → {"101", "102", "103"}
    }

    /**
     * 获取 Hash 表中所有的字段和值的映射
     *
     * 用途：获取用户购物车中全部商品数据，用于购物车列表展示
     *
     * @param key Redis 键
     * @return Map<字段名, 值>，空 Hash 返回空 Map
     */
    public Map<String, Object> hGetAll(String key) {
        // 获取 Hash 类型操作接口
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash(); // 获取 HashOperations
        // 执行 HGETALL 操作，获取完整 Hash 数据
        return ops.entries(key); // HGETALL cart:1001 → {"101":"json1", "102":"json2"}
    }

    /**
     * 判断 Hash 表中是否存在某字段
     *
     * 用途：判断商品是否已在购物车中（存在则数量 +1，不存在则新增）
     *
     * @param key Redis 键
     * @param field Hash 中的字段名
     * @return true = 字段存在, false = 字段不存在
     */
    public boolean hExists(String key, String field) {
        // 获取 Hash 类型操作接口
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash(); // 获取 HashOperations
        // 执行 HEXISTS 操作，判断字段是否存在
        return ops.hasKey(key, field); // HEXISTS cart:1001 101
    }

    /**
     * 删除整个 Hash 键
     *
     * 用途：清空用户购物车（用户手动清空）
     *
     * @param key Redis 键
     */
    public void hDelAll(String key) {
        // 直接删除整个 key
        redisTemplate.delete(key); // DEL cart:1001
    }
}
