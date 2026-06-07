package com.b2c.mall.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.b2c.mall.entity.Banner; // 轮播图实体
import org.apache.ibatis.annotations.Mapper; // Mapper 标记

/** 轮播图 Mapper 接口 — 操作 t_banner 表 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {
}
