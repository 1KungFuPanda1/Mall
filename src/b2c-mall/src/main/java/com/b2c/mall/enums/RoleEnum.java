package com.b2c.mall.enums; // 枚举类包

/**
 * 用户角色枚举 — 定义系统中所有用户角色类型
 *
 * 角色权限说明：
 * - USER（普通用户）：可以浏览商品、加入购物车、下单、查看自己订单
 * - ADMIN（管理员）：可以登录后台管理系统，管理商品、订单、用户
 *
 * 判断权限时只需检查 JWT Token 中的 role 字段是否等于 ADMIN
 */
public enum RoleEnum {

    /** 普通注册用户（C端用户） */
    USER("USER", "普通用户"),

    /** 后台管理员 */
    ADMIN("ADMIN", "管理员");

    /** 角色编码 — 存入数据库的英文值 */
    private final String code;

    /** 角色中文描述 */
    private final String desc;

    /**
     * 枚举构造方法
     *
     * @param code 角色编码
     * @param desc 中文描述
     */
    RoleEnum(String code, String desc) {
        this.code = code; // 初始化角色编码
        this.desc = desc; // 初始化角色描述
    }

    // ==================== Getter 方法 ====================

    /** 获取角色编码 */
    public String getCode() {
        return code;
    }

    /** 获取角色中文描述 */
    public String getDesc() {
        return desc;
    }
}
