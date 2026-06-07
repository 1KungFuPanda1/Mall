package com.b2c.mall.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.b2c.mall.entity.Category; // 分类实体
import org.apache.ibatis.annotations.Mapper; // Mapper 标记

/** 商品分类 Mapper 接口 — 操作 t_product_category 表 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
