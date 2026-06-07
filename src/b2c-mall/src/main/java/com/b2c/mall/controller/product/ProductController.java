package com.b2c.mall.controller.product; // C端商品控制器包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.b2c.mall.dto.response.ProductVO; // 商品视图
import com.b2c.mall.entity.Category; // 分类实体
import com.b2c.mall.entity.Product; // 商品实体
import com.b2c.mall.service.ProductService; // 商品 Service
import io.swagger.v3.oas.annotations.Operation; // Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger 分组
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.http.ResponseEntity; // HTTP 响应
import org.springframework.web.bind.annotation.*; // RESTful 注解

import java.util.List; // 列表

@RestController
@RequestMapping("/api/products") // 商品接口基础路径
@Tag(name = "C端-商品模块", description = "商品浏览、搜索、分类查询接口")
public class ProductController {

    @Autowired
    private ProductService productService; // 注入商品 Service

    /** 分页查询商品列表（含分类筛选）GET /api/products?page=1&pageSize=10&categoryId=1 */
    @GetMapping
    @Operation(summary = "商品列表", description = "分页查询商品，支持按分类筛选")
    public ResponseEntity<Page<Product>> listProducts(
            @RequestParam(defaultValue = "1") Integer page, // 当前页码，默认第1页
            @RequestParam(defaultValue = "10") Integer pageSize, // 每页数量，默认10条
            @RequestParam(required = false) Long categoryId) { // 分类ID（可选参数）
        Page<Product> result = productService.getProductPage(page, pageSize, categoryId); // 分页查询
        return ResponseEntity.ok(result); // 返回 200
    }

    /** 查询商品详情（含Redis缓存）GET /api/products/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "商品详情", description = "根据商品ID查询详细信息，使用Redis缓存加速")
    public ResponseEntity<ProductVO> getProductDetail(@PathVariable Long id) { // @PathVariable: URL路径变量
        ProductVO productVO = productService.getProductDetail(id); // 查询详情（含缓存）
        return ResponseEntity.ok(productVO); // 返回 200
    }

    /** 关键字搜索商品 GET /api/products/search?keyword=手机&page=1&pageSize=10 */
    @GetMapping("/search")
    @Operation(summary = "商品搜索", description = "根据关键字模糊搜索商品名称")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String keyword, // 搜索关键字（必填）
            @RequestParam(defaultValue = "1") Integer page, // 页码
            @RequestParam(defaultValue = "10") Integer pageSize) { // 每页数量
        Page<Product> result = productService.searchProduct(keyword, page, pageSize); // 搜索
        return ResponseEntity.ok(result); // 返回 200
    }
}
