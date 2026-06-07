package com.b2c.mall.dto.response; // 响应 DTO 包

import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Builder; // Lombok 建造者模式
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.math.BigDecimal; // 精确金额类型

/**
 * 订单商品明细 VO（视图对象）
 *
 * 订单明细为下单时商品信息的快照，保证历史数据不变
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemVO {

    /** 商品原始 ID（用于追溯） */
    private Long productId; // 商品原始ID

    /** 下单时商品名称快照 */
    private String productName; // 商品名称快照

    /** 下单时商品主图快照 URL */
    private String productImage; // 商品图片快照

    /** 下单时商品单价快照（元） */
    private BigDecimal price; // 成交单价

    /** 购买数量 */
    private Integer quantity; // 购买数量

    /** 小计金额（price × quantity） */
    private BigDecimal totalAmount; // 小计金额
}
