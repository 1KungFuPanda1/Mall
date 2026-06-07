package com.b2c.mall.config; // 配置类包

import com.fasterxml.jackson.annotation.JsonAutoDetect; // Jackson JSON 属性可见性配置
import com.fasterxml.jackson.annotation.PropertyAccessor; // Jackson 属性访问器类型
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson JSON 对象映射器
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator; // Jackson 多态类型验证器
import org.springframework.context.annotation.Bean; // Spring Bean 定义注解
import org.springframework.context.annotation.Configuration; // 配置类注解
import org.springframework.data.redis.connection.RedisConnectionFactory; // Redis 连接工厂
import org.springframework.data.redis.core.RedisTemplate; // Redis 操作模板
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer; // JSON 序列化器（Jackson实现）
import org.springframework.data.redis.serializer.StringRedisSerializer; // 字符串序列化器

/**
 * Redis 序列化配置类
 *
 * 配置目的：
 * Spring Boot 默认使用 JdkSerializationRedisSerializer 进行序列化，
 * 存入 Redis 的数据是二进制字节，不直观且占用空间大。
 * 本配置将其改为 JSON 格式序列化，使得 Redis 中的数据可以直接阅读和调试。
 *
 * 修改后效果：
 * - Key 使用 StringRedisSerializer（字符串序列化，Redis 中直接看到 key 名称）
 * - Value 使用 Jackson2JsonRedisSerializer（JSON 序列化，Redis 中直接看到 JSON 结构）
 *
 * @Configuration: 标明此类为 Spring 配置类
 */
@Configuration
public class RedisConfig {

    /**
     * 创建自定义的 RedisTemplate Bean
     *
     * RedisTemplate 是 Spring Data Redis 的核心操作类，
     * 我们通过此方法覆盖 Spring Boot 默认的 RedisTemplate 配置。
     *
     * @param connectionFactory Redis 连接工厂（由 Spring Boot 根据 application.yml 自动创建）
     * @return 配置好的 RedisTemplate<String, Object> 实例
     */
    @Bean // 将返回的 RedisTemplate 作为 Spring Bean 注册到容器中
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 创建 RedisTemplate 实例
        RedisTemplate<String, Object> template = new RedisTemplate<>(); // 新建模板对象
        template.setConnectionFactory(connectionFactory); // 设置 Redis 连接工厂

        // ==================== 配置 Key 的序列化器 ====================
        // 使用字符串序列化器，使得 Key 在 Redis 中直接显示为可读字符串
        StringRedisSerializer stringSerializer = new StringRedisSerializer(); // 创建字符串序列化器
        template.setKeySerializer(stringSerializer); // 设置普通 Key 序列化器
        template.setHashKeySerializer(stringSerializer); // 设置 Hash Key（field）序列化器

        // ==================== 配置 Value 的序列化器 ====================
        // 使用 Jackson JSON 序列化器，使得 Value 在 Redis 中以 JSON 格式存储
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class); // 创建JSON序列化器

        // 创建 Jackson ObjectMapper 用于自定义 JSON 序列化行为
        ObjectMapper objectMapper = new ObjectMapper(); // 创建 ObjectMapper 实例
        // 设置属性可见性：所有属性的 getter/setter 都参与序列化（包括 private 字段）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // 所有属性可见
        // 启用多态类型处理：在 JSON 中添加 @class 字段，反序列化时能正确还原类型
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL); // 启用类型信息
        jsonSerializer.setObjectMapper(objectMapper); // 将 ObjectMapper 设置到序列化器中

        // 将 JSON 序列化器应用到 Value
        template.setValueSerializer(jsonSerializer); // 设置普通 Value 序列化器
        template.setHashValueSerializer(jsonSerializer); // 设置 Hash Value 序列化器

        // 初始化 RedisTemplate（调用 afterPropertiesSet 完成最终配置）
        template.afterPropertiesSet(); // 完成初始化
        return template; // 返回配置好的 RedisTemplate Bean
    }
}
