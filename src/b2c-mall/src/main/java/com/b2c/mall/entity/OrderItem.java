package com.b2c.mall.entity; // 实体类包

import com.baomidou.mybatisplus.annotation.IdType; // 主键策略注解
import com.baomidou.mybatisplus.annotation.TableId; // 主键标记注解
import com.baomidou.mybatisplus.annotation.TableName; // 表名映射注解
import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.math.BigDecimal; // 精确金额类型

/**
 * 订单商品明细实体类 — 对应数据库表 t_order_item
 *
 * 设计说明（快照模式）：
 * 订单明细存储的是下单那一刻的商品信息快照，而不是关联查询实时数据。
 * 原因：如果商品后续被删除、改名、改价，历史订单数据不能受影响。
 *
 * 快照字段：product_name、product_image、price 在下单时从 Product 表复制过来
 * 关联字段：product_id、order_id 保留原始关联，用于溯源
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_item") // 映射到 t_order_item 表
public class OrderItem {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属订单 ID（逻辑关联 t_order.id，一个订单包含多个订单明细） */
    private Long orderId;

    /** 商品原始 ID（逻辑关联 t_product.id，用于溯源，即使商品被删除） */
    private Long productId;

    /** 下单时的商品名称快照（防止商品改名后历史订单显示错误） */
    private String productName;

    /** 下单时的商品主图 URL 快照（防止图片被更换后历史订单显示异常） */
    private String productImage;

    /** 下单时的商品单价快照（防止价格调整后历史订单金额变化） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额（计算公式: price × quantity，冗余存储便于查询统计） */
    private BigDecimal totalAmount;
}
