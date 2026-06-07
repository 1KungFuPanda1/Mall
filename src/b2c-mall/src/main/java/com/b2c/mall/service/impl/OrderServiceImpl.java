package com.b2c.mall.service.impl; // Service 实现类包

import cn.hutool.core.util.RandomUtil; // 随机数工具
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.b2c.mall.dto.request.PlaceOrderRequest; // 下单请求
import com.b2c.mall.dto.response.CartItemVO; // 购物车商品视图
import com.b2c.mall.dto.response.OrderItemVO; // 订单明细视图
import com.b2c.mall.dto.response.OrderVO; // 订单视图
import com.b2c.mall.entity.Address; // 地址实体
import com.b2c.mall.entity.Order; // 订单实体
import com.b2c.mall.entity.OrderItem; // 订单明细实体
import com.b2c.mall.entity.Product; // 商品实体
import com.b2c.mall.enums.OrderStatusEnum; // 订单状态枚举
import com.b2c.mall.exception.BusinessException; // 业务异常
import com.b2c.mall.mapper.AddressMapper; // 地址 Mapper
import com.b2c.mall.mapper.OrderItemMapper; // 订单明细 Mapper
import com.b2c.mall.mapper.OrderMapper; // 订单 Mapper
import com.b2c.mall.mapper.ProductMapper; // 商品 Mapper
import com.b2c.mall.mapper.UserMapper; // 用户 Mapper
import com.b2c.mall.service.CartService; // 购物车 Service
import com.b2c.mall.service.OrderService; // 订单 Service 接口
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 序列化
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.stereotype.Service; // Service 注解
import org.springframework.transaction.annotation.Transactional; // 事务注解

import java.math.BigDecimal; // 精确金额
import java.time.LocalDateTime; // 本地日期时间
import java.time.format.DateTimeFormatter; // 日期格式化
import java.util.ArrayList; // ArrayList
import java.util.List; // List
import java.util.stream.Collectors; // Stream 流操作

/**
 * 订单服务实现类
 *
 * 核心业务逻辑：
 * 1. 下单：购物车选中商品 → 乐观锁扣减库存 → 创建订单 → 清空购物车 ✓ 事务保障
 * 2. 支付：WAIT_PAY → PAID（模拟操作）
 * 3. 取消：WAIT_PAY → CANCELLED + 库存回滚
 * 4. 确认收货：RECEIVING → COMPLETED
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper; // 订单 Mapper

    @Autowired
    private OrderItemMapper orderItemMapper; // 订单明细 Mapper

    @Autowired
    private ProductMapper productMapper; // 商品 Mapper

    @Autowired
    private AddressMapper addressMapper; // 地址 Mapper

    @Autowired
    private UserMapper userMapper; // 用户 Mapper

    @Autowired
    private CartService cartService; // 购物车 Service

    @Autowired
    private ObjectMapper objectMapper; // Jackson JSON

    /**
     * 用户下单（核心业务方法）
     *
     * @Transactional: 整个方法在一个数据库事务中执行
     * 任意一步失败（异常抛出），所有已执行的数据库操作自动回滚
     *
     * @param userId 用户ID
     * @param request 下单请求
     * @return 订单视图对象
     */
    @Override
    @Transactional // 数据库事务注解
    public OrderVO placeOrder(Long userId, PlaceOrderRequest request) {
        // ============ 步骤1：从购物车获取已选中的商品 ============
        List<CartItemVO> checkedItems = cartService.getCheckedItems(userId); // 获取选中商品
        if (checkedItems.isEmpty()) {
            throw new BusinessException("请先选择要购买的商品"); // 没有选中商品
        }

        // ============ 步骤2：校验收货地址 ============
        Address address = addressMapper.selectById(request.getAddressId()); // 查询地址
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在"); // 地址不属于当前用户
        }

        // ============ 步骤3：乐观锁扣减商品库存 ============
        for (CartItemVO cartItem : checkedItems) {
            // 每次扣减一条商品（乐观锁：WHERE version = ? AND stock >= ?）
            int rows = productMapper.deductStock(cartItem.getProductId(), cartItem.getQuantity()); // 执行扣减
            if (rows == 0) {
                // 乐观锁冲突：版本号不匹配或库存不足 → 回滚整个事务
                throw new BusinessException("商品【" + cartItem.getName() + "】库存不足或系统繁忙，请稍后重试");
            }
        }

        // ============ 步骤4：构建订单编号 ============
        // 格式：yyyyMMddHHmmss + 6位随机数字
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) // 时间戳部分
                + RandomUtil.randomNumbers(6); // 随机数部分，如 "20260530143021123456"

        // ============ 步骤5：计算订单总金额 ============
        BigDecimal totalAmount = BigDecimal.ZERO; // 初始化为0
        for (CartItemVO item : checkedItems) {
            // 每行小计 = 单价 × 数量
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())); // 单价×数量
            totalAmount = totalAmount.add(itemTotal); // 累加到总金额
        }

        // ============ 步骤6：创建订单主记录 ============
        Order order = new Order(); // 创建订单实体
        order.setOrderNo(orderNo); // 订单编号
        order.setUserId(userId); // 下单用户ID
        // 将收货地址序列化为 JSON 快照（防止地址被删后订单信息缺失）
        try {
            order.setAddressJson(objectMapper.writeValueAsString(address)); // 地址转JSON
        } catch (Exception e) {
            throw new BusinessException("下单失败"); // 序列化异常
        }
        order.setTotalAmount(totalAmount); // 订单总金额
        order.setStatus(OrderStatusEnum.WAIT_PAY.getCode()); // 初始状态：待付款
        orderMapper.insert(order); // 插入订单

        // ============ 步骤7：创建订单商品明细记录（快照） ============
        for (CartItemVO cartItem : checkedItems) {
            OrderItem orderItem = new OrderItem(); // 创建订单明细实体
            orderItem.setOrderId(order.getId()); // 所属订单ID
            orderItem.setProductId(cartItem.getProductId()); // 商品原始ID（用于追溯）
            orderItem.setProductName(cartItem.getName()); // 商品名称快照
            orderItem.setProductImage(cartItem.getImage()); // 商品图片快照
            orderItem.setPrice(cartItem.getPrice()); // 单价快照
            orderItem.setQuantity(cartItem.getQuantity()); // 购买数量
            // 小计金额 = 单价 × 数量
            orderItem.setTotalAmount(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemMapper.insert(orderItem); // 插入订单明细
        }

        // ============ 步骤8：清空购物车中已选中的商品 ============
        cartService.clearCheckedItems(userId); // 从Redis中删除已下单的商品

        // ============ 步骤9：构建并返回订单视图 ============
        return buildOrderVO(order); // 构建视图对象
    }

    /**
     * 模拟支付
     *
     * 业务规则：
     * 1. 只有状态为 WAIT_PAY（待付款）的订单可以支付
     * 2. 支付后状态变为 PAID（已支付）
     * 3. 记录支付时间
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    @Override
    @Transactional // 事务保障
    public void payOrder(Long userId, Long orderId) {
        // 查询订单
        Order order = orderMapper.selectById(orderId); // 按ID查订单
        if (order == null) {
            throw new BusinessException("订单不存在"); // 订单不存在
        }
        // 校验：只有下单用户本人才能支付
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单"); // 不是本人订单
        }
        // 状态校验：只有待付款订单可以支付
        if (!OrderStatusEnum.WAIT_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付"); // 不是待付款状态
        }
        // 更新订单状态和支付时间
        order.setStatus(OrderStatusEnum.PAID.getCode()); // 状态 → 已支付
        order.setPayTime(LocalDateTime.now()); // 记录支付时间
        orderMapper.updateById(order); // 更新到数据库
    }

    /**
     * 取消订单
     *
     * 业务规则：
     * 1. 只有 WAIT_PAY 状态的订单可以取消
     * 2. 取消后状态变为 CANCELLED
     * 3. 恢复商品库存（把扣掉的库存加回去）
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    @Override
    @Transactional // 事务保障
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId); // 查询订单
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在或无权操作"); // 校验
        }
        // 状态校验：只有待付款可以取消
        if (!OrderStatusEnum.WAIT_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许取消"); // 不可取消
        }
        // 恢复库存（把下单时扣减的库存加回去）
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>(); // 创建条件
        wrapper.eq(OrderItem::getOrderId, orderId); // 查询该订单的所有明细
        List<OrderItem> items = orderItemMapper.selectList(wrapper); // 获取订单明细
        for (OrderItem item : items) {
            // 恢复每个商品的库存
            productMapper.restoreStock(item.getProductId(), item.getQuantity()); // 库存 + quantity
        }
        // 更新订单状态
        order.setStatus(OrderStatusEnum.CANCELLED.getCode()); // 状态 → 已取消
        order.setCancelTime(LocalDateTime.now()); // 记录取消时间
        orderMapper.updateById(order); // 更新到数据库
    }

    /**
     * 确认收货
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    @Override
    @Transactional // 事务保障
    public void confirmReceipt(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId); // 查询订单
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在或无权操作"); // 校验
        }
        // 状态校验：只有待收货状态可以确认收货
        if (!OrderStatusEnum.RECEIVING.getCode().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许确认收货"); // 不可确认
        }
        order.setStatus(OrderStatusEnum.COMPLETED.getCode()); // 状态 → 已完成
        order.setFinishTime(LocalDateTime.now()); // 记录完成时间
        orderMapper.updateById(order); // 更新到数据库
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return OrderVO
     */
    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId); // 查询订单
        if (order == null) {
            throw new BusinessException("订单不存在"); // 不存在
        }
        return buildOrderVO(order); // 构建视图对象
    }

    /**
     * 分页查询用户订单列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param status 订单状态（可选）
     * @return 分页订单列表
     */
    @Override
    public Page<OrderVO> getUserOrders(Long userId, Integer page, Integer pageSize, String status) {
        Page<Order> pageObj = new Page<>(page, pageSize); // 创建分页对象
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>(); // 创建条件
        wrapper.eq(Order::getUserId, userId); // 只查当前用户的订单
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status); // 按状态筛选
        }
        wrapper.orderByDesc(Order::getCreateTime); // 最新订单优先
        Page<Order> orderPage = orderMapper.selectPage(pageObj, wrapper); // 执行分页查询

        // 将 Order 分页对象转换为 OrderVO 分页对象
        Page<OrderVO> voPage = new Page<>(page, pageSize); // 创建 VO 分页对象
        voPage.setTotal(orderPage.getTotal()); // 设置总记录数
        // 转换每条订单记录
        List<OrderVO> voList = orderPage.getRecords().stream() // 流式处理
                .map(this::buildOrderVO) // 每条转换为 OrderVO
                .collect(Collectors.toList()); // 收集为 List
        voPage.setRecords(voList); // 设置转换后的记录列表
        return voPage; // 返回
    }

    /**
     * 构建订单视图对象（Order → OrderVO）
     *
     * @param order 订单实体
     * @return OrderVO 订单视图
     */
    private OrderVO buildOrderVO(Order order) {
        // 查询订单明细
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>(); // 创建条件
        itemWrapper.eq(OrderItem::getOrderId, order.getId()); // 按订单ID查询
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper); // 查询明细列表

        // 将 OrderItem 转换为 OrderItemVO
        List<OrderItemVO> itemVOs = items.stream().map(item -> // 流式处理
                OrderItemVO.builder() // 建造者模式
                        .productId(item.getProductId()) // 商品ID
                        .productName(item.getProductName()) // 商品名称快照
                        .productImage(item.getProductImage()) // 商品图片快照
                        .price(item.getPrice()) // 成交单价
                        .quantity(item.getQuantity()) // 购买数量
                        .totalAmount(item.getTotalAmount()) // 小计金额
                        .build() // 构建
        ).collect(Collectors.toList()); // 收集为List

        // 获取状态描述
        String statusDesc = ""; // 状态中文描述
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            if (statusEnum.getCode().equals(order.getStatus())) {
                statusDesc = statusEnum.getDesc(); // 匹配中文描述
                break;
            }
        }

        return OrderVO.builder() // 建造者模式
                .id(order.getId()) // 订单ID
                .orderNo(order.getOrderNo()) // 订单编号
                .userId(order.getUserId()) // 用户ID
                .addressJson(order.getAddressJson()) // 地址快照
                .totalAmount(order.getTotalAmount()) // 总金额
                .status(order.getStatus()) // 状态编码
                .statusDesc(statusDesc) // 状态中文描述
                .payTime(order.getPayTime()) // 支付时间
                .deliveryTime(order.getDeliveryTime()) // 发货时间
                .finishTime(order.getFinishTime()) // 完成时间
                .createTime(order.getCreateTime()) // 创建时间
                .items(itemVOs) // 订单明细
                .build(); // 构建完成
    }
}
