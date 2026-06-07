package com.b2c.mall.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.b2c.mall.entity.Address; // 地址实体
import org.apache.ibatis.annotations.Mapper; // Mapper 标记

/** 收货地址 Mapper 接口 — 操作 t_address 表 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
