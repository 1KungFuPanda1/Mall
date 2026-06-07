package com.b2c.mall.dto.request; // 请求 DTO 包

import jakarta.validation.constraints.Min; // 最小值校验注解
import jakarta.validation.constraints.NotNull; // 非空校验注解
import lombok.Data; // Lombok 数据类

/**
 * 添加商品到购物车请求 DTO
 *
 * 购物车使用 Redis Hash 存储，结构为：
 * Key: cart:{userId}
 * Field: {productId}
 * Value: {"productId":1,"name":"商品名","image":"/xxx.jpg","price":99.00,"quantity":2,"checked":false}
 */
@Data
public class AddCartRequest {

    /**
     * 商品 ID
     * @NotNull: 商品ID不能为 null
     */
    @NotNull(message = "商品ID不能为空") // 商品ID不能为空
    private Long productId; // 要加入购物车的商品主键 ID

    /**
     * 加入购物车的数量
     * @Min(1): 最少添加 1 件商品
     */
    @Min(value = 1, message = "数量至少为1") // 数量至少为 1
    private Integer quantity; // 购买数量，默认为 1
}
