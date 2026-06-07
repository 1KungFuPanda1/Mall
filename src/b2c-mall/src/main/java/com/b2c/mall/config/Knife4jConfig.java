package com.b2c.mall.config; // 配置类包

import io.swagger.v3.oas.models.OpenAPI; // OpenAPI 3.0 文档对象
import io.swagger.v3.oas.models.info.Contact; // 联系人信息
import io.swagger.v3.oas.models.info.Info; // API 基本信息
import io.swagger.v3.oas.models.info.License; // 许可证信息
import org.springframework.context.annotation.Bean; // Spring Bean 注解
import org.springframework.context.annotation.Configuration; // 配置类注解

/**
 * Knife4j（Swagger 增强版）接口文档配置类
 *
 * 功能说明：
 * 自动扫描 Controller 中的接口方法，生成在线 API 文档。
 * 文档页面支持在线调试（测试时自动携带请求参数和认证 Token）。
 *
 * 访问方式（启动项目后）：
 * - Knife4j 增强页面：http://localhost:8080/doc.html
 * - Swagger 原生页面：http://localhost:8080/swagger-ui/index.html
 *
 * 分组说明：
 * - C端接口组：路径匹配 /api/**，标题"移动端商城 - C端接口"
 * - 后台管理接口组：路径匹配 /admin/**，标题"移动端商城 - 管理后台接口"
 *
 * @Configuration: 配置类注解
 */
@Configuration
public class Knife4jConfig {

    /**
     * 创建 OpenAPI 文档对象 Bean
     *
     * OpenAPI 是 Swagger 3.0 的标准文档对象，
     * Knife4j 基于此对象生成美观的中文界面 API 文档。
     *
     * @return OpenAPI 实例，包含 API 的基本元信息
     */
    @Bean // 注册为 Spring Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI() // 创建 OpenAPI 3.0 文档对象
                .info(new Info() // 设置 API 基本信息
                        .title("B2C 移动端商城 API 文档") // 文档标题
                        .version("1.0.0") // API 版本号
                        .description("基于 Spring Boot 3 + MyBatis-Plus + Redis 的移动端 B2C 商城后端接口文档") // 文档描述
                        .contact(new Contact() // 联系人信息
                                .name("开发者名称") // 联系人姓名
                                .email("developer@example.com") // 联系人邮箱
                        )
                        .license(new License() // 许可证信息
                                .name("Apache 2.0") // 许可证名称
                                .url("https://www.apache.org/licenses/LICENSE-2.0") // 许可证URL
                        )
                );
    }
}
