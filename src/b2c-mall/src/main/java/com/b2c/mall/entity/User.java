package com.b2c.mall.entity; // 实体类包，对应数据库表结构

import com.baomidou.mybatisplus.annotation.*; // MyBatis-Plus 注解导入
import lombok.AllArgsConstructor; // Lombok 全参构造器注解
import lombok.Data; // Lombok 数据类注解（自动生成 getter/setter/toString/equals/hashCode）
import lombok.NoArgsConstructor; // Lombok 无参构造器注解

import java.time.LocalDateTime; // Java 8 本地日期时间类

/**
 * 用户实体类 — 对应数据库表 t_user
 *
 * @Data: Lombok 注解，编译时自动生成所有字段的 getter、setter、toString、equals、hashCode 方法
 * @NoArgsConstructor: 生成无参构造方法（MyBatis-Plus 反射实例化需要）
 * @AllArgsConstructor: 生成包含所有字段的构造方法（方便创建测试数据）
 * @TableName: 指定对应的数据库表名
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user") // 映射到 t_user 表
public class User {

    /**
     * 主键 ID
     * @TableId: 标记为主键字段
     * type = IdType.ASSIGN_ID: 使用雪花算法自动生成分布式唯一 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 手机号（唯一，用于登录和接收短信验证码） */
    private String phone;

    /** 邮箱（可选，用于邮箱验证码注册） */
    private String email;

    /** 密码（BCrypt 加密存储，数据库中永远不存明文） */
    private String password;

    /** 用户昵称（前端展示用） */
    private String nickname;

    /** 用户头像图片 URL 地址 */
    private String avatar;

    /** 用户角色：USER = 普通用户, ADMIN = 管理员 */
    private String role;

    /** 账户状态：1 = 启用（可正常登录）, 0 = 禁用（无法登录） */
    private Integer status;

    /**
     * 创建时间
     * @TableField(fill = FieldFill.INSERT): 插入时由 MyBatis-Plus 自动填充（配合 AOP 切面）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * @TableField(fill = FieldFill.INSERT_UPDATE): 插入和更新时都自动填充当前时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     * @TableLogic: MyBatis-Plus 逻辑删除注解
     * 值为 0（未删除）时正常查询，值为 1（已删除）时自动过滤
     * 执行 delete 操作时不会物理删除数据，而是将此字段更新为 1
     */
    @TableLogic
    private Integer deleted;
}
