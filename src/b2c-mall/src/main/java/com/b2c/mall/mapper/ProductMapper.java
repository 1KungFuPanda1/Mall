package com.b2c.mall.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.b2c.mall.entity.Product; // 商品实体
import org.apache.ibatis.annotations.Mapper; // Mapper 标记
import org.apache.ibatis.annotations.Param; // 参数标记（用于自定义 SQL）
import org.apache.ibatis.annotations.Update; // 更新语句注解（用于自定义 SQL）

/**
 * 商品 Mapper 接口 — 操作 t_product 表
 *
 * 重要说明：
 * 1. 实体类 Product 中使用了 @Version 注解的 version 字段，
 *    当调用 updateById() 时，MyBatis-Plus 乐观锁插件会自动校验版本号
 * 2. @TableLogic 标记的 deleted 字段，查询时会自动添加 WHERE deleted = 0
 * 3. deductStock / restoreStock 为手动编写的乐观锁 SQL，使用 @Update 注解直接定义
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 扣减商品库存（乐观锁方式）
     *
     * SQL 说明：
     * UPDATE t_product SET stock = stock - #{quantity}
     * WHERE id = #{productId} AND stock >= #{quantity} AND deleted = 0
     *
     * 注意：此方法返回影响行数（int），而非更新后对象
     * 影响行数 = 1 → 扣减成功
     * 影响行数 = 0 → 库存不足（stock < quantity）
     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响行数（1 = 成功, 0 = 失败）
     */
    @Update("UPDATE t_product SET stock = stock - #{quantity} " + // 扣减库存
            "WHERE id = #{productId} AND stock >= #{quantity} AND deleted = 0") // 乐观锁条件
    int deductStock(@Param("productId") Long productId, // 商品ID参数
                    @Param("quantity") Integer quantity); // 扣减数量参数

    /**
     * 恢复商品库存（取消订单时调用）
     *
     * SQL 说明：
     * UPDATE t_product SET stock = stock + #{quantity}
     * WHERE id = #{productId} AND deleted = 0
     *
     * @param productId 商品ID
     * @param quantity 恢复数量
     */
    @Update("UPDATE t_product SET stock = stock + #{quantity} " + // 恢复库存
            "WHERE id = #{productId} AND deleted = 0") // 条件
    void restoreStock(@Param("productId") Long productId, // 商品ID参数
                      @Param("quantity") Integer quantity); // 恢复数量参数
}
