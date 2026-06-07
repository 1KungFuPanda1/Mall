package com.b2c.mall.entity; // 实体类包

import com.baomidou.mybatisplus.annotation.*; // MyBatis-Plus 注解
import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.time.LocalDateTime; // 本地日期时间

/**
 * 首页轮播图实体类 — 对应数据库表 t_banner
 *
 * 功能说明：
 * - 管理移动端首页顶部的 Banner 轮播广告
 * - 支持图片 URL、跳转链接、排序权重
 * - 管理员可在后台随时更新，前端实时展示
 * - 通过 sort 字段控制轮播顺序，status 控制是否显示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_banner") // 映射到 t_banner 表
public class Banner {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 轮播图图片的 URL 地址（上传到服务器后的访问路径） */
    private String imageUrl;

    /** 点击轮播图后的跳转链接（如商品详情页 URL，可为空表示不跳转） */
    private String linkUrl;

    /** 排序权重值（数值越小越靠前展示，用于控制多张轮播图的展示顺序） */
    private Integer sort;

    /** 启停状态：1 = 启用（前端展示）, 0 = 禁用（前端不显示） */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最后更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
