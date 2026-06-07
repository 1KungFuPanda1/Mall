package com.b2c.mall.service; // Service 接口包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.b2c.mall.dto.request.PlaceOrderRequest; // 下单请求 DTO
import com.b2c.mall.dto.response.OrderVO; // 订单视图对象

/**
 * 订单服务接口 — 订单核心业务流程
 *
 * 订单状态流转：
 * WAIT_PAY →(支付)→ PAID →(发货)→ RECEIVING →(确认)→ COMPLETED
 * WAIT_PAY →(取消)→ CANCELLED
 */
public interface OrderService {

    /**
     * 用户下单（核心业务方法）
     *
     * 流程：
     * 1. 从购物车获取已选中的商品
     * 2. 校验商品库存
     * 3. 生成订单编号
     * 4. 使用乐观锁扣减商品库存
     * 5. 创建订单主记录 + 订单明细
     * 6. 清空购物车已选商品
     * 以上步骤在同一数据库事务中，任意一步失败则回滚
     *
     * @param userId 当前用户ID
     * @param request 下单请求（地址ID、备注等）
     * @return 订单视图对象（含订单号和明细）
     */
    OrderVO placeOrder(Long userId, PlaceOrderRequest request); // 下单

    /**
     * 模拟支付
     * 将订单状态从 WAIT_PAY 变更为 PAID
     *
     * @param userId 当前用户ID
     * @param orderId 订单ID
     */
    void payOrder(Long userId, Long orderId); // 支付

    /**
     * 取消订单（仅限待付款订单）
     * 取消后恢复库存
     *
     * @param userId 当前用户ID
     * @param orderId 订单ID
     */
    void cancelOrder(Long userId, Long orderId); // 取消订单

    /**
     * 确认收货
     * 将订单状态从 RECEIVING 变更为 COMPLETED
     *
     * @param userId 当前用户ID
     * @param orderId 订单ID
     */
    void confirmReceipt(Long userId, Long orderId); // 确认收货

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return OrderVO 含订单信息和商品明细
     */
    OrderVO getOrderDetail(Long orderId); // 订单详情

    /**
     * 分页查询用户订单列表
     *
     * @param userId 当前用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param status 订单状态（可选，null查全部）
     * @return 分页订单列表
     */
    Page<OrderVO> getUserOrders(Long userId, Integer page, Integer pageSize, String status); // 用户订单列表
}
