package com.b2c.mall.dto.response; // 响应 DTO 包

import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Builder; // Lombok 建造者模式
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.math.BigDecimal; // 精确金额类型

/**
 * 购物车商品 VO（视图对象）— 返回给前端的购物车数据
 *
 * 与 Redis Hash 中存储的 JSON 结构一一对应
 * 每个购物车商品包含：商品信息快照 + 购买数量 + 选中状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemVO {

    /** 商品 ID */
    private Long productId; // 商品主键 ID

    /** 商品名称快照 */
    private String name; // 商品名称

    /** 商品主图 URL 快照 */
    private String image; // 商品图片地址

    /** 商品单价快照（下单时以此为据） */
    private BigDecimal price; // 商品单价

    /** 购物车中该商品的数量 */
    private Integer quantity; // 购买数量

    /** 是否被选中（用于结算时判断哪些商品参与计算总价） */
    private Boolean checked; // 选中状态：true = 已选中, false = 未选中
}
