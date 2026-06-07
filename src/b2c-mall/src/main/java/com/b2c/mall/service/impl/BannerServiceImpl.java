package com.b2c.mall.service.impl; // Service 实现类包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器
import com.b2c.mall.entity.Banner; // 轮播图实体
import com.b2c.mall.mapper.BannerMapper; // 轮播图 Mapper
import com.b2c.mall.service.BannerService; // 轮播图 Service
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.stereotype.Service; // Service 注解

import java.util.List; // 列表

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper; // 轮播图 Mapper

    @Override
    public List<Banner> listEnabled() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>(); // 条件构造器
        wrapper.eq(Banner::getStatus, 1); // 只查启用状态
        wrapper.orderByAsc(Banner::getSort); // 按排序权重升序
        return bannerMapper.selectList(wrapper); // 查询返回
    }
}
