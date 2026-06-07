package com.b2c.mall.dto.response; // 响应 DTO 包

import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Builder; // Lombok 建造者模式
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.math.BigDecimal; // 精确金额类型
import java.time.LocalDateTime; // 本地日期时间

/**
 * 商品信息 VO（视图对象）— C端展示用
 *
 * 前端展示字段：ID、名称、价格、库存、图片、分类、上架时间等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVO {

    /** 商品主键 ID */
    private Long id; // 商品唯一标识

    /** 所属分类 ID */
    private Long categoryId; // 所属分类主键ID

    /** 所属分类名称（关联查询 t_product_category 得到） */
    private String categoryName; // 分类名称（如"手机数码"）

    /** 商品名称 */
    private String name; // 商品名称

    /** 商品价格（元） */
    private BigDecimal price; // 商品价格

    /** 当前库存数量 */
    private Integer stock; // 库存数量

    /** 商品图片 URL（多张以逗号分隔） */
    private String image; // 商品图片列表

    /** 商品图文详情（富文本 HTML） */
    private String description; // 商品描述

    /** 上下架状态：1 = 上架, 0 = 下架 */
    private Integer status; // 商品状态

    /** 商品上架/创建时间 */
    private LocalDateTime createTime; // 创建时间
}
