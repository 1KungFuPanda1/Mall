package com.b2c.mall.dto.response; // 响应 DTO 包

import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Builder; // Lombok 建造者模式
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.math.BigDecimal; // 精确金额类型
import java.time.LocalDateTime; // 本地日期时间
import java.util.List; // 列表集合

/**
 * 订单详情 VO（视图对象）— C端和后台管理共用
 *
 * 包含订单主信息和订单商品明细列表
 * 订单明细使用快照数据，确保即使商品被删除/修改，历史订单数据不受影响
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderVO {

    /** 订单主键 ID */
    private Long id; // 订单主键ID

    /** 订单编号（yyyyMMddHHmmss + 6位随机数） */
    private String orderNo; // 订单编号

    /** 下单用户 ID */
    private Long userId; // 下单用户ID

    /** 下单用户手机号 */
    private String userPhone; // 下单用户手机号（后台管理展示用）

    /** 收货地址快照（JSON 字符串，前端解析后展示） */
    private String addressJson; // 收货地址JSON快照

    /** 订单总金额（元） */
    private BigDecimal totalAmount; // 订单总金额

    /** 订单状态（WAIT_PAY / PAID / RECEIVING / COMPLETED / CANCELLED） */
    private String status; // 订单状态编码

    /** 订单状态中文描述（如"待付款"） */
    private String statusDesc; // 订单状态中文说明

    /** 支付时间 */
    private LocalDateTime payTime; // 支付完成时间

    /** 发货时间 */
    private LocalDateTime deliveryTime; // 卖家发货时间

    /** 订单完成时间 */
    private LocalDateTime finishTime; // 交易完成时间

    /** 订单创建时间（下单时间） */
    private LocalDateTime createTime; // 订单创建时间

    /** 订单商品明细列表（快照数据） */
    private List<OrderItemVO> items; // 订单中的商品明细集合
}
