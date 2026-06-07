package com.b2c.mall; // 声明包路径，遵循公司域名倒序 + 项目名

import org.mybatis.spring.annotation.MapperScan; // MyBatis-Plus 的 Mapper 接口扫描注解
import org.springframework.boot.SpringApplication; // Spring Boot 应用启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // Spring Boot 自动配置注解
import org.springframework.scheduling.annotation.EnableScheduling; // 启用 Spring 定时任务支持

/**
 * B2C 移动端商城 - Spring Boot 应用主启动类
 *
 * @SpringBootApplication: 组合注解，等价于 @Configuration + @EnableAutoConfiguration + @ComponentScan
 * 它会自动扫描当前包及其子包下的所有 Spring 组件（@Component、@Service、@Controller 等）
 *
 * @MapperScan: 指定 MyBatis-Plus 需要扫描的 Mapper 接口包路径
 * 扫描到的接口会被动态代理，注入到 Spring 容器中
 *
 * @EnableScheduling: 开启 Spring 的定时任务功能
 * 用于后续实现「超时未支付订单自动取消」等定时任务（可选）
 */
@SpringBootApplication // 标记此类为 Spring Boot 启动类，启用自动配置
@MapperScan("com.b2c.mall.mapper") // 扫描 Mapper 接口路径，生成动态代理实现类
@EnableScheduling // 开启定时任务支持
public class MallApplication {

    /**
     * Java 应用入口 main 方法
     * SpringApplication.run() 会启动内嵌的 Tomcat 服务器并初始化整个 Spring 容器
     *
     * @param args 命令行参数（可传入 --server.port=8080 等覆盖配置）
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用，传入启动类和命令行参数
        SpringApplication.run(MallApplication.class, args);
    }
}
