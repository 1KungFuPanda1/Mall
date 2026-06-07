package com.b2c.mall.service; // Service 接口包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // MyBatis-Plus 分页对象
import com.b2c.mall.dto.response.ProductVO; // 商品视图对象
import com.b2c.mall.entity.Product; // 商品实体
import com.b2c.mall.entity.Category; // 分类实体

import java.util.List; // Java 列表集合

/**
 * 商品服务接口 — C 端商品浏览
 */
public interface ProductService {

    /**
     * 分页查询商品列表（支持分类筛选）
     *
     * @param page 当前页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID（可选，为 null 则查全部）
     * @return MyBatis-Plus 分页结果对象
     */
    Page<Product> getProductPage(Integer page, Integer pageSize, Long categoryId); // 分页查询商品

    /**
     * 根据分类查询上架商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Product> listByCategory(Long categoryId); // 按分类查商品

    /**
     * 查询商品详情（含 Redis 缓存）
     * 缓存策略：先查 Redis → 命中直接返回 → 未命中查 DB → 写入 Redis → 返回
     *
     * @param productId 商品ID
     * @return ProductVO 商品视图对象
     */
    ProductVO getProductDetail(Long productId); // 商品详情（含缓存）

    /**
     * 根据关键字搜索商品
     *
     * @param keyword 搜索关键字
     * @param page 当前页码
     * @param pageSize 每页数量
     * @return 分页搜索结果
     */
    Page<Product> searchProduct(String keyword, Integer page, Integer pageSize); // 商品搜索

    /**
     * 获取所有启用的商品分类
     *
     * @return 分类列表
     */
    List<Category> getCategoryList(); // 获取分类列表

    /**
     * 根据 ID 获取商品（不通过 VO 包装）
     *
     * @param productId 商品ID
     * @return Product 实体
     */
    Product getById(Long productId); // 根据ID获取商品实体
}
