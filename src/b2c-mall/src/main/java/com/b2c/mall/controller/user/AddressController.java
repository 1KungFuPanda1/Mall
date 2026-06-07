package com.b2c.mall.controller.user; // C端地址控制器包

import com.b2c.mall.dto.request.AddressSaveRequest; // 地址请求
import com.b2c.mall.entity.Address; // 地址实体
import com.b2c.mall.service.AddressService; // 地址 Service
import io.swagger.v3.oas.annotations.Operation; // Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger 分组
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求
import jakarta.validation.Valid; // 参数校验
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.http.HttpStatus; // HTTP 状态码
import org.springframework.http.ResponseEntity; // HTTP 响应
import org.springframework.web.bind.annotation.*; // RESTful 注解

import java.util.List; // 列表

@RestController
@RequestMapping("/api/addresses") // 地址接口基础路径
@Tag(name = "C端-收货地址", description = "收货地址增删改查")
public class AddressController {

    @Autowired
    private AddressService addressService; // 注入地址 Service

    /** 获取地址列表 GET /api/addresses */
    @GetMapping
    @Operation(summary = "地址列表", description = "获取当前用户所有收货地址")
    public ResponseEntity<List<Address>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        return ResponseEntity.ok(addressService.listByUserId(userId)); // 返回地址列表
    }

    /** 新增地址 POST /api/addresses */
    @PostMapping
    @Operation(summary = "新增地址", description = "添加一个新收货地址")
    public ResponseEntity<Void> save(HttpServletRequest request, @Valid @RequestBody AddressSaveRequest saveRequest) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        addressService.save(userId, saveRequest); // 新增
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 返回 201
    }

    /** 修改地址 PUT /api/addresses/{id} */
    @PutMapping("/{id}")
    @Operation(summary = "修改地址", description = "修改指定收货地址的信息")
    public ResponseEntity<Void> update(HttpServletRequest request, @PathVariable Long id,
                                        @Valid @RequestBody AddressSaveRequest saveRequest) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        addressService.update(userId, id, saveRequest); // 更新
        return ResponseEntity.ok().build(); // 返回 200
    }

    /** 删除地址 DELETE /api/addresses/{id} */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除指定收货地址（逻辑删除）")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        addressService.delete(userId, id); // 删除
        return ResponseEntity.ok().build(); // 返回 200
    }

    /** 设为默认地址 PUT /api/addresses/{id}/default */
    @PutMapping("/{id}/default")
    @Operation(summary = "设为默认", description = "将指定地址设为默认收货地址")
    public ResponseEntity<Void> setDefault(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId"); // 用户ID
        addressService.setDefault(userId, id); // 设为默认
        return ResponseEntity.ok().build(); // 返回 200
    }
}
