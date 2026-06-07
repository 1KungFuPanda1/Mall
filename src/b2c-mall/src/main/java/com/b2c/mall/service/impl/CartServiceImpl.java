package com.b2c.mall.service.impl; // Service 实现类包

import com.b2c.mall.dto.response.CartItemVO; // 购物车商品视图对象
import com.b2c.mall.entity.Product; // 商品实体
import com.b2c.mall.exception.BusinessException; // 业务异常
import com.b2c.mall.mapper.ProductMapper; // 商品 Mapper
import com.b2c.mall.service.CartService; // 购物车 Service 接口
import com.b2c.mall.util.RedisUtil; // Redis 工具
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson JSON
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.stereotype.Service; // Service 注解

import java.util.ArrayList; // ArrayList 集合
import java.util.List; // List 集合
import java.util.Map; // Map 接口

/**
 * 购物车服务实现类 — 基于 Redis Hash 存储
 *
 * 核心数据结构（Redis Hash）：
 * ┌─────────────────┬────────────────────────────────────────────┐
 * │  Key: cart:1001 │  Field → Value                             │
 * ├─────────────────┼────────────────────────────────────────────┤
 * │  cart:1001      │  "101" → {"productId":101,"name":"手机",   │
 * │  (userId=1001)  │           "price":6999,"quantity":2,...}   │
 * │                 │  "102" → {"productId":102,"name":"电脑",   │
 * │                 │           "price":9999,"quantity":1,...}   │
 * └─────────────────┴────────────────────────────────────────────┘
 *
 * 操作对应 Redis 命令：
 * - 添加: HSET cart:1001 101 '{"productId":101,...}'
 * - 查询: HGETALL cart:1001
 * - 删除: HDEL cart:1001 101
 * - 修改: 先 HGET → 修改 Java 对象 → HSET 覆盖
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisUtil redisUtil; // 注入 Redis 工具

    @Autowired
    private ProductMapper productMapper; // 注入商品 Mapper（用于获取商品信息）

    @Autowired
    private ObjectMapper objectMapper; // 注入 Jackson（JSON 序列化/反序列化）

    /** 购物车 Redis Key 前缀 */
    private static final String CART_KEY_PREFIX = "cart:"; // Redis key: cart:{userId}

    /**
     * 添加商品到购物车
     *
     * 处理逻辑：
     * 1. 查询商品是否存在（校验上架状态和库存）
     * 2. 检查购物车中是否已有该商品
     *    - 已有：数量 +1，更新
     *    - 没有：新增购物车条目
     * 3. 将商品信息快照存入 Redis Hash
     *
     * @param userId 当前用户ID
     * @param productId 要添加的商品ID
     */
    @Override
    public void addToCart(Long userId, Long productId) {
        // ============ 步骤1：校验商品是否存在 ============
        Product product = productMapper.selectById(productId); // 查询商品
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架"); // 商品不可购买
        }
        if (product.getStock() <= 0) {
            throw new BusinessException("商品库存不足"); // 无库存
        }

        // ============ 步骤2：组装购物车商品数据 ============
        String cartKey = CART_KEY_PREFIX + userId; // Redis Key：cart:1001
        String field = String.valueOf(productId); // Hash Field：商品ID转字符串

        // 检查购物车中是否已有该商品
        if (redisUtil.hExists(cartKey, field)) {
            // 购物车中已有该商品：获取原数据，数量+1
            String existingJson = (String) redisUtil.hGet(cartKey, field); // 获取原JSON
            try {
                CartItemVO existItem = objectMapper.readValue(existingJson, CartItemVO.class); // 反序列化
                existItem.setQuantity(existItem.getQuantity() + 1); // 数量+1
                String updatedJson = objectMapper.writeValueAsString(existItem); // 序列化为JSON
                redisUtil.hSet(cartKey, field, updatedJson); // 更新到Redis
            } catch (Exception e) {
                throw new BusinessException("操作购物车异常"); // 反序列化失败
            }
        } else {
            // 购物车中没有该商品：新增购物车条目
            CartItemVO cartItem = CartItemVO.builder() // 建造者模式
                    .productId(product.getId()) // 商品ID
                    .name(product.getName()) // 商品名称快照
                    .image(product.getImage()) // 商品图片快照
                    .price(product.getPrice()) // 商品单价快照
                    .quantity(1) // 初始数量为1
                    .checked(false) // 默认未选中
                    .build(); // 构建完成
            try {
                String jsonStr = objectMapper.writeValueAsString(cartItem); // 序列化为JSON
                redisUtil.hSet(cartKey, field, jsonStr); // 存入Redis Hash
            } catch (Exception e) {
                throw new BusinessException("添加购物车失败"); // 序列化失败
            }
        }
    }

    /**
     * 修改购物车商品数量
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 新数量
     */
    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String cartKey = CART_KEY_PREFIX + userId; // Redis Key
        String field = String.valueOf(productId); // Hash Field
        // 检查商品是否在购物车中
        if (!redisUtil.hExists(cartKey, field)) {
            throw new BusinessException("购物车中没有该商品"); // 商品不在购物车
        }
        // 获取原数据
        String existingJson = (String) redisUtil.hGet(cartKey, field); // 获取原JSON
        try {
            CartItemVO item = objectMapper.readValue(existingJson, CartItemVO.class); // 反序列化
            item.setQuantity(quantity); // 覆盖数量
            String updatedJson = objectMapper.writeValueAsString(item); // 序列化
            redisUtil.hSet(cartKey, field, updatedJson); // 更新到Redis
        } catch (Exception e) {
            throw new BusinessException("修改购物车失败"); // 异常
        }
    }

    /**
     * 从购物车中移除商品
     *
     * @param userId 用户ID
     * @param productId 商品ID
     */
    @Override
    public void removeFromCart(Long userId, Long productId) {
        String cartKey = CART_KEY_PREFIX + userId; // Redis Key
        String field = String.valueOf(productId); // Hash Field
        redisUtil.hDel(cartKey, field); // HDEL cart:1001 101
    }

    /**
     * 获取购物车中所有商品
     *
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    @Override
    public List<CartItemVO> getCartList(Long userId) {
        String cartKey = CART_KEY_PREFIX + userId; // Redis Key
        // HGETALL cart:1001 → 获取整个 Hash 的所有 Field 和 Value
        Map<String, Object> entries = redisUtil.hGetAll(cartKey); // 获取所有条目
        List<CartItemVO> result = new ArrayList<>(); // 创建结果列表
        // 遍历所有 Field-Value 对
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            try {
                // 将 JSON Value 反序列化为 CartItemVO 对象
                CartItemVO item = objectMapper.readValue((String) entry.getValue(), CartItemVO.class); // 反序列化
                result.add(item); // 添加到结果列表
            } catch (Exception e) {
                e.printStackTrace(); // 记录异常，跳过损坏的数据
            }
        }
        return result; // 返回购物车商品列表
    }

    /**
     * 设置商品选中/取消选中状态
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @param checked 选中状态
     */
    @Override
    public void checkItem(Long userId, Long productId, Boolean checked) {
        String cartKey = CART_KEY_PREFIX + userId; // Redis Key
        String field = String.valueOf(productId); // Hash Field
        if (!redisUtil.hExists(cartKey, field)) {
            throw new BusinessException("购物车中没有该商品"); // 不存在
        }
        String existingJson = (String) redisUtil.hGet(cartKey, field); // 获取原JSON
        try {
            CartItemVO item = objectMapper.readValue(existingJson, CartItemVO.class); // 反序列化
            item.setChecked(checked); // 更新选中状态
            String updatedJson = objectMapper.writeValueAsString(item); // 序列化
            redisUtil.hSet(cartKey, field, updatedJson); // 更新到Redis
        } catch (Exception e) {
            throw new BusinessException("操作失败"); // 异常
        }
    }

    /**
     * 清空购物车中已选中的商品（下单成功后调用）
     *
     * @param userId 用户ID
     */
    @Override
    public void clearCheckedItems(Long userId) {
        // 先获取所有购物车商品
        List<CartItemVO> allItems = getCartList(userId); // 获取全部商品
        // 遍历并删除已选中的商品
        for (CartItemVO item : allItems) {
            if (Boolean.TRUE.equals(item.getChecked())) {
                removeFromCart(userId, item.getProductId()); // 逐条删除选中商品
            }
        }
    }

    /**
     * 获取购物车中已选中的商品（用于下单确认页）
     *
     * @param userId 用户ID
     * @return 已选中商品列表
     */
    @Override
    public List<CartItemVO> getCheckedItems(Long userId) {
        List<CartItemVO> allItems = getCartList(userId); // 获取全部商品
        List<CartItemVO> checkedItems = new ArrayList<>(); // 创建结果列表
        // 过滤出 checked = true 的商品
        for (CartItemVO item : allItems) {
            if (Boolean.TRUE.equals(item.getChecked())) {
                checkedItems.add(item); // 添加到结果
            }
        }
        return checkedItems; // 返回已选商品列表
    }
}
