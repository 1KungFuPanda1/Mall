package com.b2c.mall.service; // Service 接口包

import com.b2c.mall.entity.Banner; // 轮播图实体
import java.util.List; // 列表

/** 轮播图服务接口 */
public interface BannerService {
    /** 获取所有启用的轮播图（按排序权重升序） */
    List<Banner> listEnabled();
}
