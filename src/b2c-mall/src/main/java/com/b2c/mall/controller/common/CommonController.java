package com.b2c.mall.controller.common; // 公共接口控制器包

import com.b2c.mall.entity.Banner; // 轮播图实体
import com.b2c.mall.entity.Category; // 分类实体
import com.b2c.mall.service.BannerService; // 轮播图 Service
import com.b2c.mall.service.ProductService; // 商品 Service（用于获取分类）
import io.swagger.v3.oas.annotations.Operation; // Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger 分组
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.http.ResponseEntity; // HTTP 响应
import org.springframework.web.bind.annotation.*; // RESTful 注解

import java.util.List; // 列表

@RestController // RESTful 控制器
@Tag(name = "公共接口", description = "轮播图和分类查询，无需登录")
public class CommonController {

    @Autowired
    private BannerService bannerService; // 注入轮播图 Service

    @Autowired
    private ProductService productService; // 注入商品 Service（获取分类）

    /** 获取轮播图列表 GET /api/banners */
    @GetMapping("/api/banners")
    @Operation(summary = "轮播图列表", description = "获取所有启用的首页轮播图")
    public ResponseEntity<List<Banner>> getBanners() {
        List<Banner> banners = bannerService.listEnabled(); // 查询启用的轮播图
        return ResponseEntity.ok(banners); // 返回 200
    }

    /** 获取商品分类列表 GET /api/categories */
    @GetMapping("/api/categories")
    @Operation(summary = "分类列表", description = "获取所有启用的商品分类")
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> categories = productService.getCategoryList(); // 查询分类
        return ResponseEntity.ok(categories); // 返回 200
    }
}
