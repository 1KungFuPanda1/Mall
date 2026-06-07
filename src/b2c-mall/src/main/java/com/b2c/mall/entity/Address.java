package com.b2c.mall.entity; // 实体类包

import com.baomidou.mybatisplus.annotation.*; // MyBatis-Plus 注解
import lombok.AllArgsConstructor; // Lombok 全参构造器
import lombok.Data; // Lombok 数据类
import lombok.NoArgsConstructor; // Lombok 无参构造器

import java.time.LocalDateTime; // 本地日期时间

/**
 * 收货地址实体类 — 对应数据库表 t_address
 *
 * 业务规则说明：
 * - 一个用户可以创建多个收货地址，但最多只能有一个默认地址（is_default = 1）
 * - 当用户设置新默认地址时，需将其他地址的 is_default 改为 0
 * - 删除时采用逻辑删除（进入回收站），避免历史订单引用失效
 * - 下单时会将选中的地址完整快照存入 t_order.address_json
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_address") // 映射到 t_address 表
public class Address {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属用户 ID（逻辑关联 t_user.id） */
    private Long userId;

    /** 收货人真实姓名 */
    private String receiverName;

    /** 收货人手机号码 */
    private String receiverPhone;

    /** 省份（如"广东省"） */
    private String province;

    /** 城市（如"深圳市"） */
    private String city;

    /** 区/县（如"南山区"） */
    private String district;

    /** 详细地址（街道、小区、门牌号等） */
    private String detail;

    /** 是否默认地址：1 = 是（该用户唯一默认）, 0 = 否 */
    private Integer isDefault;

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
