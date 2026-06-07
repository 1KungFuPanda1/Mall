package com.b2c.mall.dto.request; // 请求 DTO 包

import jakarta.validation.constraints.NotNull; // 非空校验注解
import jakarta.validation.constraints.Positive; // 正数校验注解
import lombok.Data; // Lombok 数据类

/**
 * 下单请求 DTO — 封装用户提交订单的入参
 *
 * 下单流程：
 * 1. 用户从购物车选中商品 → 确认订单页 → 提交此请求
 * 2. 后端校验收货地址、商品库存
 * 3. 使用乐观锁扣减库存 → 创建订单 → 生成订单明细 → 清空购物车已选商品
 * 4. 上述操作在同一数据库事务中，任意步骤失败则全部回滚
 */
@Data
public class PlaceOrderRequest {

    /**
     * 收货地址 ID
     * 用户从自己的地址列表中选择一个已存在的地址
     * @NotNull: 地址 ID 不能为空
     */
    @NotNull(message = "收货地址不能为空") // 地址ID不能为空
    private Long addressId; // 用户选择的收货地址主键 ID

    /**
     * 备注信息（可选）
     * 用户可以填写订单备注，如"请发顺丰快递"、"工作日送货"等
     */
    private String remark; // 订单备注（可为空）
}
