package com.b2c.mall.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.b2c.mall.entity.Order; // 订单实体
import org.apache.ibatis.annotations.Mapper; // Mapper 标记

/** 订单 Mapper 接口 — 操作 t_order 表 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
