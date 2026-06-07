package com.b2c.mall.service.impl; // Service 实现类包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.b2c.mall.dto.response.ProductVO; // 商品视图对象
import com.b2c.mall.entity.Category; // 分类实体
import com.b2c.mall.entity.Product; // 商品实体
import com.b2c.mall.exception.BusinessException; // 业务异常
import com.b2c.mall.mapper.CategoryMapper; // 分类 Mapper
import com.b2c.mall.mapper.ProductMapper; // 商品 Mapper
import com.b2c.mall.service.ProductService; // 商品 Service 接口
import com.b2c.mall.util.RedisUtil; // Redis 工具
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson JSON 序列化工具
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.stereotype.Service; // Service 层注解

import java.util.List; // 列表集合
import java.util.concurrent.TimeUnit; // 时间单位枚举

/**
 * 商品服务实现类
 *
 * 核心设计：Redis 缓存策略 — Cache-Aside 模式（旁路缓存）
 * 读操作：先查 Redis → 命中返回 / 未命中查 DB → 写入 Redis → 返回
 * 写操作：先更新 DB → 删除 Redis 缓存（下次读取时重新加载，避免脏数据）
 *
 * 缓存 Key 规范：
 * - product:detail:{id} → 单个商品详情
 * - product:hot:list → 首页热门商品列表
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper; // 注入商品 Mapper

    @Autowired
    private CategoryMapper categoryMapper; // 注入分类 Mapper

    @Autowired
    private RedisUtil redisUtil; // 注入 Redis 工具

    /** JSON 序列化工具 */
    @Autowired
    private ObjectMapper objectMapper; // Jackson ObjectMapper

    /** 商品详情缓存 Key 前缀 */
    private static final String PRODUCT_DETAIL_KEY = "product:detail:"; // Redis key前缀

    /** 商品缓存过期时间（秒）— 30 分钟 */
    private static final long PRODUCT_CACHE_TTL = 1800L; // 30分钟

    /**
     * 分页查询商品列表
     *
     * 支持按分类筛选：
     * - categoryId 为空 → 查询全部上架商品
     * - categoryId 有值 → 按分类筛选
     *
     * 只查询 status=1（上架）且 deleted=0（未删除）的商品
     *
     * @param page 当前页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID（可选）
     * @return MyBatis-Plus 分页结果
     */
    @Override
    public Page<Product> getProductPage(Integer page, Integer pageSize, Long categoryId) {
        Page<Product> pageObj = new Page<>(page, pageSize); // 创建分页对象（当前页、每页数量）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>(); // 创建条件构造器
        wrapper.eq(Product::getStatus, 1); // WHERE status = 1（仅查询上架商品）
        // 如果传入了分类ID，追加分类筛选条件
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId); // WHERE category_id = ?
        }
        wrapper.orderByDesc(Product::getCreateTime); // ORDER BY create_time DESC（最新商品优先）
        return productMapper.selectPage(pageObj, wrapper); // 执行分页查询，返回结果
    }

    /**
     * 按分类查询商品列表
     *
     * @param categoryId 分类ID
     * @return 该分类下的所有上架商品
     */
    @Override
    public List<Product> listByCategory(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>(); // 创建查询条件
        wrapper.eq(Product::getCategoryId, categoryId); // 按分类筛选
        wrapper.eq(Product::getStatus, 1); // 只查上架商品
        wrapper.orderByDesc(Product::getCreateTime); // 最新优先
        return productMapper.selectList(wrapper); // 查询并返回结果
    }

    /**
     * 查询商品详情（带 Redis 缓存）
     *
     * 缓存查询流程（Cache-Aside 模式）：
     * 1. 从 Redis 查询 product:detail:{id}
     * 2. 如果命中缓存 → 直接返回
     * 3. 如果未命中 → 查询数据库 → 写入 Redis（TTL 30分钟）→ 返回
     *
     * @param productId 商品ID
     * @return ProductVO 商品详情视图对象
     */
    @Override
    public ProductVO getProductDetail(Long productId) {
        // ============ 步骤1：尝试从 Redis 缓存中获取商品详情 ============
        String cacheKey = PRODUCT_DETAIL_KEY + productId; // 组装缓存Key：product:detail:1001
        String cachedJson = (String) redisUtil.get(cacheKey); // 从 Redis 获取缓存数据（JSON 字符串）

        if (cachedJson != null) {
            // 缓存命中：将 JSON 字符串反序列化为 ProductVO 对象并直接返回
            try {
                return objectMapper.readValue(cachedJson, ProductVO.class); // 反序列化 JSON → ProductVO
            } catch (Exception e) {
                // 反序列化异常时不清空缓存，直接走数据库查询（容错处理）
                e.printStackTrace(); // 控制台打印异常
            }
        }

        // ============ 步骤2：缓存未命中，查询数据库 ============
        Product product = productMapper.selectById(productId); // 查询商品
        if (product == null) {
            throw new BusinessException("商品不存在"); // 商品不存在
        }

        // 通过分类ID查询分类名称
        Category category = categoryMapper.selectById(product.getCategoryId()); // 查询分类
        String categoryName = category != null ? category.getName() : "未分类"; // 获取分类名称

        // 构建 ProductVO 对象
        ProductVO productVO = ProductVO.builder() // 建造者模式
                .id(product.getId()) // 商品ID
                .categoryId(product.getCategoryId()) // 分类ID
                .categoryName(categoryName) // 分类名称
                .name(product.getName()) // 商品名称
                .price(product.getPrice()) // 商品价格
                .stock(product.getStock()) // 库存数量
                .image(product.getImage()) // 商品图片
                .description(product.getDescription()) // 商品描述
                .status(product.getStatus()) // 上架状态
                .createTime(product.getCreateTime()) // 创建时间
                .build(); // 构建完成

        // ============ 步骤3：将查询结果写入 Redis 缓存 ============
        try {
            String jsonStr = objectMapper.writeValueAsString(productVO); // 序列化 ProductVO → JSON 字符串
            redisUtil.set(cacheKey, jsonStr, PRODUCT_CACHE_TTL, TimeUnit.SECONDS); // 写入Redis，30分钟过期
        } catch (Exception e) {
            // 写入缓存失败不影响业务，仅记录日志
            e.printStackTrace(); // 控制台打印异常（实际项目建议用 log 框架）
        }

        return productVO; // 返回商品视图对象
    }

    /**
     * 关键字模糊搜索商品
     *
     * 使用 MyBatis-Plus 的 LIKE 条件实现模糊查询
     * 只搜索上架商品
     *
     * @param keyword 搜索关键字
     * @param page 当前页码
     * @param pageSize 每页数量
     * @return 分页搜索结果
     */
    @Override
    public Page<Product> searchProduct(String keyword, Integer page, Integer pageSize) {
        Page<Product> pageObj = new Page<>(page, pageSize); // 创建分页对象
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>(); // 创建条件构造器
        // 模糊搜索：商品名称包含关键字
        wrapper.like(Product::getName, keyword); // WHERE name LIKE '%keyword%'
        wrapper.eq(Product::getStatus, 1); // 只搜上架商品
        wrapper.orderByDesc(Product::getCreateTime); // 最新优先
        return productMapper.selectPage(pageObj, wrapper); // 执行分页查询
    }

    /**
     * 获取所有启用的商品分类
     *
     * 按 sort 字段升序排列（权重小的靠前）
     * 只查 status=1（启用）且 deleted=0（未删除）的分类
     *
     * @return 分类列表
     */
    @Override
    public List<Category> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>(); // 创建条件构造器
        wrapper.eq(Category::getStatus, 1); // 只查启用的分类
        wrapper.orderByAsc(Category::getSort); // 按排序权重升序
        return categoryMapper.selectList(wrapper); // 查询并返回
    }

    /**
     * 根据 ID 获取商品实体
     *
     * @param productId 商品ID
     * @return Product 实体
     */
    @Override
    public Product getById(Long productId) {
        return productMapper.selectById(productId); // 直接查询数据库
    }
}
