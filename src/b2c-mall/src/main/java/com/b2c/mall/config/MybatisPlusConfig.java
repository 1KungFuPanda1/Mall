package com.b2c.mall.config; // 配置类包

import com.baomidou.mybatisplus.annotation.DbType; // MyBatis-Plus 数据库类型枚举
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor; // MyBatis-Plus 拦截器链
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor; // 乐观锁插件
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor; // 分页插件
import org.springframework.context.annotation.Bean; // Spring Bean 注解
import org.springframework.context.annotation.Configuration; // 配置类注解

/**
 * MyBatis-Plus 插件配置类
 *
 * 配置的核心插件：
 * 1. 分页插件（PaginationInnerInterceptor）：让 MyBatis-Plus 支持分页查询
 * 2. 乐观锁插件（OptimisticLockerInnerInterceptor）：让 @Version 注解生效，
 *    自动在 UPDATE 语句中追加版本号校验，实现防超卖功能
 *
 * @Configuration: 配置类注解
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建 MyBatis-Plus 拦截器 Bean
     *
     * MybatisPlusInterceptor 是一个拦截器链，可以添加多个内部拦截器（InnerInterceptor）
     * 拦截器会在 SQL 执行前后进行拦截和增强处理
     *
     * @return 配置好的 MybatisPlusInterceptor 实例
     */
    @Bean // 注册为 Spring Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建 MybatisPlus 拦截器链
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 新建拦截器链

        // ==================== 添加分页插件 ====================
        // 创建分页内部拦截器，指定数据库类型为 MySQL
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL); // 创建分页插件
        // 设置分页合理化：请求页数超过最大页时自动返回最后一页数据，而非空数据
        paginationInterceptor.setOverflow(true); // 超出最大页时返回最后一页
        // 将分页插件添加到拦截器链中
        interceptor.addInnerInterceptor(paginationInterceptor); // 注册分页插件

        // ==================== 添加乐观锁插件 ====================
        // 创建乐观锁内部拦截器
        // 当 Service 中调用 baseMapper.updateById(entity) 时，
        // 插件自动将 UPDATE 语句改写为：
        // UPDATE t_product SET stock=?, version=version+1 WHERE id=? AND version=?
        OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor(); // 创建乐观锁插件
        // 将乐观锁插件添加到拦截器链中
        interceptor.addInnerInterceptor(optimisticLockerInterceptor); // 注册乐观锁插件

        return interceptor; // 返回配置好的拦截器链
    }
}
