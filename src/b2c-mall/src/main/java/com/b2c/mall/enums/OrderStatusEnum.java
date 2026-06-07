package com.b2c.mall.enums; // 枚举类包

/**
 * 订单状态枚举 — 定义订单所有可能的状态及其流转规则
 *
 * 订单状态流转流程（严格按照需求文档设计）：
 * ┌──────────┐    支付     ┌──────────┐    发货     ┌───────────┐   确认收货   ┌───────────┐
 * │ WAIT_PAY │ ────────→ │   PAID   │ ────────→ │ RECEIVING │ ──────────→ │ COMPLETED │
 * │  待付款   │           │  已支付   │           │   待收货   │             │   已完成    │
 * └────┬─────┘           └──────────┘           └───────────┘             └───────────┘
 *      │
 *      │ 取消订单
 *      ▼
 * ┌───────────┐
 * │ CANCELLED │
 * │   已取消   │
 * └───────────┘
 *
 * 取消规则：只有「待付款(WAT_PAY)」状态的订单可以取消
 */
public enum OrderStatusEnum {

    /** 待付款：用户已提交订单但尚未支付 */
    WAIT_PAY("WAIT_PAY", "待付款"),

    /** 已支付：用户已完成支付，等待管理员发货（本项目中支付为模拟操作） */
    PAID("PAID", "已支付"),

    /** 待收货：管理员已发货，等待用户确认收货 */
    RECEIVING("RECEIVING", "待收货"),

    /** 已完成：用户已确认收货，订单交易完成 */
    COMPLETED("COMPLETED", "已完成"),

    /** 已取消：用户主动取消订单（仅待付款状态可操作） */
    CANCELLED("CANCELLED", "已取消");

    /**
     * 状态编码 — 存入数据库的英文状态值
     * 使用英文编码而非中文，方便系统国际化扩展和程序判断
     */
    private final String code;

    /**
     * 状态描述 — 前端展示的中文说明文字
     * 如"待付款"、"已支付"等，业务人员一眼能看懂
     */
    private final String desc;

    /**
     * 枚举构造方法
     * 每个枚举常量创建时都会传入 code 和 desc 两个参数
     *
     * @param code 状态编码（英文，存入数据库）
     * @param desc 状态描述（中文，前端展示）
     */
    OrderStatusEnum(String code, String desc) {
        this.code = code; // 初始化状态编码
        this.desc = desc; // 初始化状态描述
    }

    /**
     * 判断当前状态是否允许取消订单
     * 业务规则：只有「待付款」状态的订单才能被用户取消
     *
     * @return true = 可以取消, false = 不可取消
     */
    public boolean canCancel() {
        return this == WAIT_PAY; // 当且仅当状态为 WAIT_PAY 时返回 true
    }

    /**
     * 判断当前状态是否允许支付
     * 业务规则：只有「待付款」状态的订单才能进行支付
     *
     * @return true = 可以支付, false = 不可支付
     */
    public boolean canPay() {
        return this == WAIT_PAY; // 只有待付款状态能支付
    }

    /**
     * 判断当前状态是否允许管理员发货
     * 业务规则：只有「已支付」状态的订单才能发货
     *
     * @return true = 可以发货, false = 不可发货
     */
    public boolean canDeliver() {
        return this == PAID; // 只有已支付状态能发货
    }

    /**
     * 判断当前状态是否允许用户确认收货
     * 业务规则：只有「待收货」状态的订单才能确认收货
     *
     * @return true = 可以确认收货, false = 不可确认收货
     */
    public boolean canConfirm() {
        return this == RECEIVING; // 只有待收货状态能确认收货
    }

    // ==================== Getter 方法 ====================

    /** 获取状态编码 */
    public String getCode() {
        return code;
    }

    /** 获取状态中文描述 */
    public String getDesc() {
        return desc;
    }
}
