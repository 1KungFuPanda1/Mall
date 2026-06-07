package com.b2c.mall.entity; // 实体类包

import com.baomidou.mybatisplus.annotation.*; // MyBatis-Plus 注解
import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.math.BigDecimal; // 精确的十进制数字类型（用于金额计算）
import java.time.LocalDateTime; // 本地日期时间

/**
 * 商品实体类 — 对应数据库表 t_product
 *
 * 关键字段说明：
 * - version: 乐观锁版本号，用于防止并发超卖（下单时校验版本号是否一致）
 * - stock: 库存数量，下单时通过乐观锁进行安全扣减
 * - image: 商品多图 URL，以逗号分隔存储（如 "/img/a.jpg,/img/b.jpg"）
 * - price: 使用 BigDecimal 而非 float/double，避免浮点数精度问题导致金额错误
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_product") // 映射到 t_product 表
public class Product {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商品分类 ID（逻辑关联 t_product_category.id） */
    private Long categoryId;

    /** 商品名称（用于展示和搜索） */
    private String name;

    /** 商品价格（使用 BigDecimal 保证精度，单位为元） */
    private BigDecimal price;

    /** 当前库存数量（展示 + 下单扣减的依据） */
    private Integer stock;

    /**
     * 乐观锁版本号
     * @Version: MyBatis-Plus 乐观锁插件注解
     * 每次更新商品库存时，版本号自动 +1
     * 更新 SQL: UPDATE t_product SET stock=?, version=version+1 WHERE id=? AND version=?
     */
    @Version
    private Integer version;

    /** 商品图片 URL 列表（多张图片以逗号分隔） */
    private String image;

    /** 商品图文详情描述（支持富文本 HTML） */
    private String description;

    /** 上下架状态：1 = 上架（C端可见）, 0 = 下架（C端不显示） */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最后更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记（@TableLogic 标记，自动过滤已删除记录） */
    @TableLogic
    private Integer deleted;
}
