package com.b2c.mall.entity; // 实体类包

import com.baomidou.mybatisplus.annotation.*; // MyBatis-Plus 注解
import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.time.LocalDateTime; // 本地日期时间

/**
 * 商品分类实体类 — 对应数据库表 t_product_category
 *
 * 功能说明：
 * - 管理商品的分类体系，如"手机数码"、"电脑办公"、"服装鞋帽"等
 * - 支持排序（sort 字段）控制前台展示顺序
 * - 支持启用/禁用状态切换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_product_category") // 映射到 t_product_category 表
public class Category {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 商品分类名称（如"手机数码"） */
    private String name;

    /** 排序权重值（数值越小越靠前展示，默认 0） */
    private Integer sort;

    /** 分类状态：1 = 启用（前端展示）, 0 = 禁用（前端不显示） */
    private Integer status;

    /** 分类创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 分类更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
