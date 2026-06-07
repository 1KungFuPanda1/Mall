package com.b2c.mall.controller.order; // C端订单控制器包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.b2c.mall.dto.request.PlaceOrderRequest; // 下单请求
import com.b2c.mall.dto.response.OrderVO; // 订单视图
import com.b2c.mall.service.OrderService; // 订单 Service
import io.swagger.v3.oas.annotations.Operation; // Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger 分组
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求
import jakarta.validation.Valid; // 参数校验
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.http.HttpStatus; // HTTP 状态码
import org.springframework.http.ResponseEntity; // HTTP 响应
import org.springframework.web.bind.annotation.*; // RESTful 注解

@RestController
@RequestMapping("/api/orders") // 订单接口基础路径
@Tag(name = "C端-订单模块", description = "下单、支付、取消、确认收货等订单操作")
public class OrderController {

    @Autowired
    private OrderService orderService; // 注入订单 Service

    /** 提交订单 POST /api/orders */
    @PostMapping
    @Operation(summary = "提交订单", description = "根据购物车已选商品创建订单，使用乐观锁扣减库存")
    public ResponseEntity<OrderVO> placeOrder(HttpServletRequest request, // 获取用户ID
                                               @Valid @RequestBody PlaceOrderRequest placeRequest) { // 下单请求
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        OrderVO orderVO = orderService.placeOrder(userId, placeRequest); // 下单
        return ResponseEntity.status(HttpStatus.CREATED).body(orderVO); // 返回 201 + 订单信息
    }

    /** 订单列表 GET /api/orders?page=1&pageSize=10&status=WAIT_PAY */
    @GetMapping
    @Operation(summary = "订单列表", description = "分页查询用户订单，支持按状态筛选")
    public ResponseEntity<Page<OrderVO>> getUserOrders(
            HttpServletRequest request, // 获取用户ID
            @RequestParam(defaultValue = "1") Integer page, // 页码
            @RequestParam(defaultValue = "10") Integer pageSize, // 每页数量
            @RequestParam(required = false) String status) { // 订单状态（可选）
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        Page<OrderVO> orders = orderService.getUserOrders(userId, page, pageSize, status); // 分页查询
        return ResponseEntity.ok(orders); // 返回 200
    }

    /** 订单详情 GET /api/orders/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "订单详情", description = "查看订单完整信息及商品明细")
    public ResponseEntity<OrderVO> getOrderDetail(@PathVariable Long id) { // 订单ID
        OrderVO orderVO = orderService.getOrderDetail(id); // 查询详情
        return ResponseEntity.ok(orderVO); // 返回 200
    }

    /** 模拟支付 PUT /api/orders/{id}/pay */
    @PutMapping("/{id}/pay")
    @Operation(summary = "模拟支付", description = "模拟订单支付，将状态从待付款变更为已支付")
    public ResponseEntity<Void> payOrder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        orderService.payOrder(userId, id); // 支付
        return ResponseEntity.ok().build(); // 返回 200
    }

    /** 取消订单 PUT /api/orders/{id}/cancel */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消待付款订单，恢复商品库存")
    public ResponseEntity<Void> cancelOrder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        orderService.cancelOrder(userId, id); // 取消
        return ResponseEntity.ok().build(); // 返回 200
    }

    /** 确认收货 PUT /api/orders/{id}/confirm */
    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货", description = "用户收到商品后确认收货，订单状态变更为已完成")
    public ResponseEntity<Void> confirmReceipt(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        orderService.confirmReceipt(userId, id); // 确认收货
        return ResponseEntity.ok().build(); // 返回 200
    }
}
