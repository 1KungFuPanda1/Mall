package com.b2c.mall.controller.admin; // 后台管理控制器包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.b2c.mall.dto.request.AdminLoginRequest; // 管理员登录请求
import com.b2c.mall.dto.request.LoginRequest; // 登录请求（复用）
import com.b2c.mall.dto.response.LoginResponse; // 登录响应
import com.b2c.mall.dto.response.OrderVO; // 订单视图
import com.b2c.mall.entity.*; // 所有实体类
import com.b2c.mall.enums.OrderStatusEnum; // 订单状态枚举
import com.b2c.mall.exception.BusinessException; // 业务异常
import com.b2c.mall.mapper.*; // 所有 Mapper
import com.b2c.mall.service.OrderService; // 订单 Service
import com.b2c.mall.service.UserService; // 用户 Service
import io.swagger.v3.oas.annotations.Operation; // Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger 分组
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.beans.factory.annotation.Value; // 配置注入
import org.springframework.http.HttpStatus; // HTTP 状态码
import org.springframework.http.ResponseEntity; // HTTP 响应
import org.springframework.transaction.annotation.Transactional; // 事务
import org.springframework.web.bind.annotation.*; // RESTful 注解
import org.springframework.web.multipart.MultipartFile; // 文件上传

import java.io.File; // 文件操作
import java.io.IOException; // IO 异常
import java.time.LocalDateTime; // 日期时间
import java.util.*; // 集合工具

@RestController
@RequestMapping("/admin") // 后台管理接口基础路径
@Tag(name = "后台管理", description = "管理员登录、商品管理、订单管理、用户管理等后台接口")
public class AdminController {

    @Autowired
    private UserService userService; // 用户 Service

    @Autowired
    private OrderService orderService; // 订单 Service

    @Autowired
    private UserMapper userMapper; // 用户 Mapper

    @Autowired
    private ProductMapper productMapper; // 商品 Mapper

    @Autowired
    private CategoryMapper categoryMapper; // 分类 Mapper

    @Autowired
    private OrderMapper orderMapper; // 订单 Mapper

    @Autowired
    private OrderItemMapper orderItemMapper; // 订单明细 Mapper

    @Autowired
    private BannerMapper bannerMapper; // 轮播图 Mapper

    /** 文件上传路径 */
    @Value("${app.upload.path:./uploads}")
    private String uploadPath; // 上传目录

    // ==================== 管理员登录 ====================
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员账号密码登录后台管理系统")
    public ResponseEntity<LoginResponse> adminLogin(@RequestBody AdminLoginRequest request) {
        LoginRequest loginRequest = new LoginRequest(); // 创建登录请求
        loginRequest.setAccount(request.getAccount()); // 设置账号
        loginRequest.setPassword(request.getPassword()); // 设置密码
        LoginResponse response = userService.login(loginRequest); // 调用登录
        if (!"ADMIN".equals(response.getRole())) {
            throw new BusinessException("非管理员账号，无法登录后台"); // 非管理员拒绝
        }
        return ResponseEntity.ok(response); // 返回 200
    }

    // ==================== 分类管理 ====================
    @GetMapping("/categories")
    @Operation(summary = "分类列表", description = "获取所有商品分类（含已禁用）")
    public ResponseEntity<List<Category>> categoryList() {
        return ResponseEntity.ok(categoryMapper.selectList(null)); // 查全部
    }

    @PostMapping("/categories")
    @Operation(summary = "新增分类", description = "添加新商品分类")
    public ResponseEntity<Void> addCategory(@RequestBody Category category) {
        categoryMapper.insert(category); // 插入
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "修改分类", description = "修改分类名称、排序、状态")
    public ResponseEntity<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id); // 设置ID
        category.setUpdateTime(LocalDateTime.now()); // 更新时间
        categoryMapper.updateById(category); // 更新
        return ResponseEntity.ok().build(); // 200
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "删除分类", description = "删除分类（逻辑删除）")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id); // 逻辑删除
        return ResponseEntity.ok().build(); // 200
    }

    // ==================== 商品管理 ====================
    @GetMapping("/products")
    @Operation(summary = "商品列表", description = "分页查询所有商品（含已下架）")
    public ResponseEntity<Page<Product>> productList(
            @RequestParam(defaultValue = "1") Integer page, // 页码
            @RequestParam(defaultValue = "10") Integer pageSize, // 每页
            @RequestParam(required = false) String name, // 搜索关键词
            @RequestParam(required = false) Long categoryId, // 分类筛选
            @RequestParam(required = false) Integer status) { // 状态筛选
        Page<Product> pageObj = new Page<>(page, pageSize); // 分页对象
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>(); // 条件
        if (name != null && !name.isEmpty()) wrapper.like(Product::getName, name); // 模糊搜索
        if (categoryId != null) wrapper.eq(Product::getCategoryId, categoryId); // 按分类
        if (status != null) wrapper.eq(Product::getStatus, status); // 按状态
        wrapper.orderByDesc(Product::getCreateTime); // 最新优先
        return ResponseEntity.ok(productMapper.selectPage(pageObj, wrapper)); // 200
    }

    @PostMapping("/products")
    @Operation(summary = "新增商品", description = "添加新商品")
    public ResponseEntity<Void> addProduct(@RequestBody Product product) {
        productMapper.insert(product); // 插入
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "修改商品", description = "修改商品信息")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id); // 设置ID
        product.setUpdateTime(LocalDateTime.now()); // 更新时间
        productMapper.updateById(product); // 更新
        return ResponseEntity.ok().build(); // 200
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "删除商品", description = "删除商品（逻辑删除）")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id); // 逻辑删除
        return ResponseEntity.ok().build(); // 200
    }

    @PutMapping("/products/{id}/status")
    @Operation(summary = "上下架", description = "切换商品上下架状态")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Product product = productMapper.selectById(id); // 查询
        if (product == null) throw new BusinessException("商品不存在");
        product.setStatus(body.get("status")); // 更新状态
        productMapper.updateById(product); // 保存
        return ResponseEntity.ok().build(); // 200
    }

    // ==================== 订单管理 ====================
    @GetMapping("/orders")
    @Operation(summary = "订单列表", description = "分页查询所有订单，支持状态筛选")
    public ResponseEntity<Page<Order>> orderList(
            @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<Order> pageObj = new Page<>(page, pageSize); // 分页
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>(); // 条件
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status); // 状态
        wrapper.orderByDesc(Order::getCreateTime); // 最新
        return ResponseEntity.ok(orderMapper.selectPage(pageObj, wrapper)); // 200
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "订单详情", description = "查看订单详情")
    public ResponseEntity<OrderVO> orderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetail(id)); // 200
    }

    @PutMapping("/orders/{id}/deliver")
    @Operation(summary = "发货", description = "管理员操作发货，状态从已支付变更为待收货")
    @Transactional
    public ResponseEntity<Void> deliverOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id); // 查订单
        if (order == null) throw new BusinessException("订单不存在");
        if (!OrderStatusEnum.PAID.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许发货"); // 校验
        }
        order.setStatus(OrderStatusEnum.RECEIVING.getCode()); // 状态 → 待收货
        order.setDeliveryTime(LocalDateTime.now()); // 发货时间
        orderMapper.updateById(order); // 更新
        return ResponseEntity.ok().build(); // 200
    }

    // ==================== 用户管理 ====================
    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "分页查看所有注册用户")
    public ResponseEntity<Page<User>> userList(
            @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> pageObj = new Page<>(page, pageSize); // 分页
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(); // 条件
        wrapper.eq(User::getRole, "USER"); // 只查普通用户
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getPhone, keyword).or().like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime); // 最新
        Page<User> userPage = userMapper.selectPage(pageObj, wrapper); // 查询
        userPage.getRecords().forEach(u -> u.setPassword(null)); // 清除密码
        return ResponseEntity.ok(userPage); // 200
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "启用/禁用", description = "切换用户启用/禁用状态")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id); // 查询
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(body.get("status")); // 更新状态
        userMapper.updateById(user); // 保存
        return ResponseEntity.ok().build(); // 200
    }

    // ==================== 轮播图管理 ====================
    @GetMapping("/banners")
    @Operation(summary = "轮播图列表", description = "获取所有轮播图")
    public ResponseEntity<List<Banner>> bannerList() {
        return ResponseEntity.ok(bannerMapper.selectList(null)); // 查全部
    }

    @PostMapping("/banners")
    @Operation(summary = "新增轮播图", description = "添加新轮播图")
    public ResponseEntity<Void> addBanner(@RequestBody Banner banner) {
        bannerMapper.insert(banner); // 插入
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201
    }

    @PutMapping("/banners/{id}")
    @Operation(summary = "修改轮播图", description = "修改轮播图信息")
    public ResponseEntity<Void> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id); // 设置ID
        bannerMapper.updateById(banner); // 更新
        return ResponseEntity.ok().build(); // 200
    }

    @DeleteMapping("/banners/{id}")
    @Operation(summary = "删除轮播图", description = "删除轮播图（逻辑删除）")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id); // 逻辑删除
        return ResponseEntity.ok().build(); // 200
    }

    // ==================== 文件上传 ====================
    @PostMapping("/upload")
    @Operation(summary = "图片上传", description = "上传商品图片或轮播图")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new BusinessException("上传文件不能为空"); // 校验文件

        // 生成唯一文件名：UUID + 原文件扩展名
        String originalName = file.getOriginalFilename(); // 原文件名
        String suffix = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg"; // 提取扩展名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix; // UUID文件名

        // 创建目标文件
        File destDir = new File(uploadPath); // 上传目录
        if (!destDir.exists()) destDir.mkdirs(); // 目录不存在则创建
        File destFile = new File(destDir, newFileName); // 目标文件
        file.transferTo(destFile); // 保存文件到磁盘

        // 返回文件访问URL
        String fileUrl = "/uploads/" + newFileName; // 虚拟路径映射
        Map<String, String> result = new HashMap<>(); // 响应体
        result.put("url", fileUrl); // 文件URL
        return ResponseEntity.status(HttpStatus.CREATED).body(result); // 201
    }

    // ==================== 数据统计 ====================
    @GetMapping("/statistics/today")
    @Operation(summary = "今日统计", description = "今日订单数和营业额统计")
    public ResponseEntity<Map<String, Object>> todayStatistics() {
        Map<String, Object> result = new HashMap<>(); // 结果

        // 查询今日订单数（状态不为 CANCELLED）
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0); // 今日零点
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        todayWrapper.ge(Order::getCreateTime, todayStart); // 创建时间 >= 今日零点
        todayWrapper.ne(Order::getStatus, OrderStatusEnum.CANCELLED.getCode()); // 排除已取消订单
        Long todayOrders = orderMapper.selectCount(todayWrapper); // 统计数量
        result.put("todayOrders", todayOrders); // 今日订单数

        // 查询今日营业额（已支付/待收货/已完成订单的总金额）
        LambdaQueryWrapper<Order> amountWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        amountWrapper.ge(Order::getCreateTime, todayStart); // 创建时间 >= 今日零点
        amountWrapper.in(Order::getStatus, // 只统计有效订单
                OrderStatusEnum.PAID.getCode(), OrderStatusEnum.RECEIVING.getCode(), OrderStatusEnum.COMPLETED.getCode());
        List<Order> todayPaidOrders = orderMapper.selectList(amountWrapper); // 查询今日有效订单
        double todayAmount = todayPaidOrders.stream().mapToDouble(o -> o.getTotalAmount().doubleValue()).sum(); // 累加金额（BigDecimal转double）
        result.put("todayAmount", String.format("%.2f", todayAmount)); // 今日营业额（保留两位小数）

        // 查询总用户数
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        userWrapper.eq(User::getRole, "USER"); // 只查普通用户
        Long totalUsers = userMapper.selectCount(userWrapper); // 统计数量
        result.put("totalUsers", totalUsers); // 总用户数

        return ResponseEntity.ok(result); // 200
    }

    // ==================== 订单操作（管理员权限） ====================
    @PutMapping("/orders/{id}/cancel")
    @Operation(summary = "取消订单", description = "管理员取消待付款订单，恢复商品库存")
    @Transactional
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        // 查询订单
        Order order = orderMapper.selectById(id); // 按ID查订单
        if (order == null) {
            throw new BusinessException("订单不存在"); // 订单不存在
        }
        // 状态校验：只有待付款订单可以取消
        if (!OrderStatusEnum.WAIT_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许取消"); // 不是待付款状态
        }
        // 恢复库存（把下单时扣减的库存加回去）
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>(); // 创建条件
        wrapper.eq(OrderItem::getOrderId, id); // 查询该订单的所有明细
        List<OrderItem> items = orderItemMapper.selectList(wrapper); // 获取订单明细
        for (OrderItem item : items) {
            // 恢复每个商品的库存
            productMapper.restoreStock(item.getProductId(), item.getQuantity()); // 库存 + quantity
        }
        // 更新订单状态
        order.setStatus(OrderStatusEnum.CANCELLED.getCode()); // 状态 → 已取消
        order.setCancelTime(LocalDateTime.now()); // 记录取消时间
        orderMapper.updateById(order); // 更新到数据库
        return ResponseEntity.ok().build(); // 200
    }

    @PutMapping("/orders/{id}/pay")
    @Operation(summary = "模拟支付", description = "管理员模拟订单支付，状态从待付款变更为已支付")
    public ResponseEntity<Void> payOrder(@PathVariable Long id) {
        // 查询订单
        Order order = orderMapper.selectById(id); // 按ID查订单
        if (order == null) {
            throw new BusinessException("订单不存在"); // 订单不存在
        }
        // 状态校验：只有待付款订单可以支付
        if (!OrderStatusEnum.WAIT_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付"); // 不是待付款状态
        }
        // 更新订单状态和支付时间
        order.setStatus(OrderStatusEnum.PAID.getCode()); // 状态 → 已支付
        order.setPayTime(LocalDateTime.now()); // 记录支付时间
        orderMapper.updateById(order); // 更新到数据库
        return ResponseEntity.ok().build(); // 200
    }

    @PutMapping("/orders/{id}/confirm")
    @Operation(summary = "确认收货", description = "管理员确认收货，订单状态变更为已完成")
    public ResponseEntity<Void> confirmReceipt(@PathVariable Long id) {
        // 查询订单
        Order order = orderMapper.selectById(id); // 查询订单
        if (order == null) {
            throw new BusinessException("订单不存在"); // 订单不存在
        }
        // 状态校验：只有待收货状态可以确认收货
        if (!OrderStatusEnum.RECEIVING.getCode().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许确认收货"); // 不可确认
        }
        // 更新订单状态
        order.setStatus(OrderStatusEnum.COMPLETED.getCode()); // 状态 → 已完成
        order.setFinishTime(LocalDateTime.now()); // 记录完成时间
        orderMapper.updateById(order); // 更新到数据库
        return ResponseEntity.ok().build(); // 200
    }

    @GetMapping("/statistics/summary")
    @Operation(summary = "结算数据汇总", description = "今日/本月订单数、营业额、待发货数等多维度统计")
    public ResponseEntity<Map<String, Object>> statisticsSummary() {
        Map<String, Object> result = new HashMap<>(); // 结果

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0); // 今日零点
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0); // 本月第一天零点

        // 1. 今日订单数（排除已取消）
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.ge(Order::getCreateTime, todayStart);
        todayOrderWrapper.ne(Order::getStatus, OrderStatusEnum.CANCELLED.getCode());
        Long todayOrders = orderMapper.selectCount(todayOrderWrapper);
        result.put("todayOrders", todayOrders);

        // 2. 今日营业额（已支付/待收货/已完成订单）
        LambdaQueryWrapper<Order> todayAmountWrapper = new LambdaQueryWrapper<>();
        todayAmountWrapper.ge(Order::getCreateTime, todayStart);
        todayAmountWrapper.in(Order::getStatus,
                OrderStatusEnum.PAID.getCode(), OrderStatusEnum.RECEIVING.getCode(), OrderStatusEnum.COMPLETED.getCode());
        List<Order> todayPaidOrders = orderMapper.selectList(todayAmountWrapper);
        double todayAmount = todayPaidOrders.stream().mapToDouble(o -> o.getTotalAmount().doubleValue()).sum();
        result.put("todayAmount", String.format("%.2f", todayAmount));

        // 3. 待发货订单数（PAID 状态）
        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Order::getStatus, OrderStatusEnum.PAID.getCode());
        Long pendingOrders = orderMapper.selectCount(pendingWrapper);
        result.put("pendingOrders", pendingOrders);

        // 4. 本月订单数（排除已取消）
        LambdaQueryWrapper<Order> monthOrderWrapper = new LambdaQueryWrapper<>();
        monthOrderWrapper.ge(Order::getCreateTime, monthStart);
        monthOrderWrapper.ne(Order::getStatus, OrderStatusEnum.CANCELLED.getCode());
        Long monthOrders = orderMapper.selectCount(monthOrderWrapper);
        result.put("monthOrders", monthOrders);

        // 5. 本月营业额（已支付/待收货/已完成订单）
        LambdaQueryWrapper<Order> monthAmountWrapper = new LambdaQueryWrapper<>();
        monthAmountWrapper.ge(Order::getCreateTime, monthStart);
        monthAmountWrapper.in(Order::getStatus,
                OrderStatusEnum.PAID.getCode(), OrderStatusEnum.RECEIVING.getCode(), OrderStatusEnum.COMPLETED.getCode());
        List<Order> monthPaidOrders = orderMapper.selectList(monthAmountWrapper);
        double monthAmount = monthPaidOrders.stream().mapToDouble(o -> o.getTotalAmount().doubleValue()).sum();
        result.put("monthAmount", String.format("%.2f", monthAmount));

        // 6. 总用户数
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, "USER");
        Long totalUsers = userMapper.selectCount(userWrapper);
        result.put("totalUsers", totalUsers);

        return ResponseEntity.ok(result); // 200
    }
}
