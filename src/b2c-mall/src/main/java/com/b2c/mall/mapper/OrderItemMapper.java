package com.b2c.mall.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.b2c.mall.entity.OrderItem; // 订单明细实体
import org.apache.ibatis.annotations.Mapper; // Mapper 标记

/** 订单商品明细 Mapper 接口 — 操作 t_order_item 表 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
