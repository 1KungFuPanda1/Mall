package com.b2c.mall.service; // Service 接口包

import com.b2c.mall.dto.response.CartItemVO; // 购物车商品视图对象

import java.util.List; // 列表集合

/**
 * 购物车服务接口 — 使用 Redis Hash 存储购物车数据
 *
 * Redis 数据结构说明：
 * Key: cart:{userId}
 * 类型: Hash
 * Field → Value 示例：
 *   field: "1001" (商品ID)
 *   value: '{"productId":1001,"name":"华为手机","image":"/img/1.jpg","price":6999.00,"quantity":2,"checked":true}'
 *
 * 设计选择（Redis vs MySQL）：
 * 选择 Redis 的原因是购物车数据访问频繁、修改频繁、数据量较小、
 * 且对一致性要求不是极高（偶尔丢失可接受）。
 * 如果使用 MySQL 存储，每次加购/修改数量都需要数据库写操作，对数据库压力较大。
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * 如果商品已在购物车中，则数量 +1
     * 如果商品不在购物车中，则新增一条记录
     *
     * @param userId 当前用户ID
     * @param productId 要添加的商品ID
     */
    void addToCart(Long userId, Long productId); // 添加到购物车

    /**
     * 修改购物车中商品的数量
     *
     * @param userId 当前用户ID
     * @param productId 商品ID
     * @param quantity 新的数量（会覆盖旧值）
     */
    void updateQuantity(Long userId, Long productId, Integer quantity); // 修改数量

    /**
     * 从购物车中删除某个商品
     *
     * @param userId 当前用户ID
     * @param productId 要删除的商品ID
     */
    void removeFromCart(Long userId, Long productId); // 删除商品

    /**
     * 获取用户购物车中所有商品列表
     *
     * @param userId 当前用户ID
     * @return 购物车商品列表
     */
    List<CartItemVO> getCartList(Long userId); // 获取购物车列表

    /**
     * 设置购物车中某个商品的选中/取消选中状态
     *
     * @param userId 当前用户ID
     * @param productId 商品ID
     * @param checked true=选中, false=取消选中
     */
    void checkItem(Long userId, Long productId, Boolean checked); // 修改选中状态

    /**
     * 清空购物车中已选中的商品（下单后调用）
     *
     * @param userId 当前用户ID
     */
    void clearCheckedItems(Long userId); // 清空已选商品

    /**
     * 获取购物车中已选中的商品列表（用于下单确认页展示）
     *
     * @param userId 当前用户ID
     * @return 已选中商品列表
     */
    List<CartItemVO> getCheckedItems(Long userId); // 获取已选商品
}
