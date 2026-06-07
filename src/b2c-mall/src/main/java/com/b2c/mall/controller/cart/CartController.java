package com.b2c.mall.controller.cart; // C端购物车控制器包

import com.b2c.mall.dto.response.CartItemVO; // 购物车商品视图
import com.b2c.mall.service.CartService; // 购物车 Service
import io.swagger.v3.oas.annotations.Operation; // Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger 分组
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.http.ResponseEntity; // HTTP 响应
import org.springframework.web.bind.annotation.*; // RESTful 注解

import java.util.HashMap; // HashMap
import java.util.List; // 列表
import java.util.Map; // Map

@RestController
@RequestMapping("/api/cart") // 购物车接口基础路径
@Tag(name = "C端-购物车模块", description = "购物车增删改查（Redis Hash存储）")
public class CartController {

    @Autowired
    private CartService cartService; // 注入购物车 Service

    /** 获取购物车列表 GET /api/cart */
    @GetMapping
    @Operation(summary = "购物车列表", description = "获取当前用户购物车中所有商品")
    public ResponseEntity<List<CartItemVO>> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId"); // 从拦截器获取用户ID
        List<CartItemVO> list = cartService.getCartList(userId); // 获取购物车
        return ResponseEntity.ok(list); // 返回 200
    }

    /** 添加商品到购物车 POST /api/cart?productId=1001 */
    @PostMapping
    @Operation(summary = "添加购物车", description = "将商品加入购物车，若已存在则数量+1")
    public ResponseEntity<Void> addToCart(
            HttpServletRequest request, // 获取用户ID
            @RequestParam Long productId) { // 请求参数：商品ID
        Long userId = (Long) request.getAttribute("userId"); // 获取用户ID
        cartService.addToCart(userId, productId); // 添加到购物车
        return ResponseEntity.ok().build(); // 返回 200
    }

    /** 修改购物车商品数量 PUT /api/cart/{productId}?quantity=3 */
    @PutMapping("/{productId}")
    @Operation(summary = "修改数量", description = "修改购物车中某商品的购买数量")
    public ResponseEntity<Void> updateQuantity(
            HttpServletRequest request, // 获取用户ID
            @PathVariable Long productId, // URL: 商品ID
            @RequestParam Integer quantity) { // 请求参数: 新数量
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        cartService.updateQuantity(userId, productId, quantity); // 更新数量
        return ResponseEntity.ok().build(); // 返回 200
    }

    /** 删除购物车商品 DELETE /api/cart/{productId} */
    @DeleteMapping("/{productId}")
    @Operation(summary = "删除商品", description = "从购物车中移除指定商品")
    public ResponseEntity<Void> removeFromCart(HttpServletRequest request, @PathVariable Long productId) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        cartService.removeFromCart(userId, productId); // 删除
        return ResponseEntity.ok().build(); // 返回 200
    }

    /** 选中/取消选中商品 PUT /api/cart/check/{productId} */
    @PutMapping("/check/{productId}")
    @Operation(summary = "选中/取消选中", description = "切换购物车中商品的选中状态")
    public ResponseEntity<Void> checkItem(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestBody Map<String, Boolean> body) { // 请求体: {"checked": true}
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        Boolean checked = body.get("checked"); // 获取选中状态
        cartService.checkItem(userId, productId, checked); // 更新选中状态
        return ResponseEntity.ok().build(); // 返回 200
    }
}
