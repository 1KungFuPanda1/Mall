package com.b2c.mall.entity; // 实体类包

import com.baomidou.mybatisplus.annotation.*; // MyBatis-Plus 注解
import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.math.BigDecimal; // 精确金额类型
import java.time.LocalDateTime; // 本地日期时间

/**
 * 订单主表实体类 — 对应数据库表 t_order
 *
 * 关键业务说明：
 * - order_no: 时间戳 + 6位随机数生成的订单编号，如 20260530143021123456
 * - address_json: 下单时的收货地址快照（JSON字符串），防止地址删除后订单数据不完整
 * - status: 订单状态，在 OrderStatusEnum 枚举中定义流转规则
 *   状态流转: WAIT_PAY → (支付) → PAID → (发货) → RECEIVING → (确认收货) → COMPLETED
 *   可取消: WAIT_PAY 状态 → CANCELLED
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order") // 映射到 t_order 表
public class Order {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单编号（格式：yyyyMMddHHmmss + 6位随机数，唯一索引保证不重复） */
    private String orderNo;

    /** 下单用户 ID（逻辑关联 t_user.id） */
    private Long userId;

    /** 收货地址完整快照（JSON 格式字符串，存储下单时的收货人、电话、地址等信息） */
    private String addressJson;

    /** 订单总金额（元，为所有订单明细 amount 之和） */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     * WAIT_PAY = 待付款, PAID = 已支付, RECEIVING = 待收货,
     * COMPLETED = 已完成, CANCELLED = 已取消
     */
    private String status;

    /** 支付完成时间（用户点击模拟支付按钮后记录的时间） */
    private LocalDateTime payTime;

    /** 管理员发货时间（后台操作发货时记录的时间） */
    private LocalDateTime deliveryTime;

    /** 订单完成时间（用户确认收货时记录的时间） */
    private LocalDateTime finishTime;

    /** 订单取消时间（用户取消订单时记录的时间） */
    private LocalDateTime cancelTime;

    /** 订单创建时间（即用户提交下单的时间，自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 订单最后更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记（订单一般不物理删除，保留数据用于对账和审计） */
    @TableLogic
    private Integer deleted;
}
