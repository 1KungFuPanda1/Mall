// ============================================
// B2C 移动端商城 — 全部 API 接口集合
// 按模块分组：用户、商品、购物车、订单、地址、公共
// 每个函数对应后端一个接口，参数和路径与 api文档.json 严格一致
// ============================================
import { get, post, put, del } from '@/utils/request.js' // 导入封装的请求方法

// ==================== 用户模块 ====================

/** 发送短信验证码 POST /api/user/send-sms */
export const sendSms = (data) => post('/api/user/send-sms', data)

/** 用户注册 POST /api/user/register */
export const register = (data) => post('/api/user/register', data)

/** 用户登录 POST /api/user/login */
export const login = (data) => post('/api/user/login', data)

/** 获取当前用户信息 GET /api/user/info */
export const getUserInfo = () => get('/api/user/info')

// ==================== 商品模块 ====================

/** 商品列表（分页+分类筛选） GET /api/products */
export const getProducts = (params) => get('/api/products', params)

/** 商品详情（含Redis缓存） GET /api/products/{id} */
export const getProductDetail = (id) => get(`/api/products/${id}`)

/** 商品搜索 GET /api/products/search */
export const searchProducts = (params) => get('/api/products/search', params)

// ==================== 购物车模块 ====================

/** 获取购物车列表 GET /api/cart */
export const getCartList = () => get('/api/cart')

/** 添加商品到购物车 POST /api/cart?productId=xxx */
export const addToCart = (productId) => post(`/api/cart?productId=${productId}`)

/** 修改购物车商品数量 PUT /api/cart/{productId}?quantity=xxx */
export const updateCartQuantity = (productId, quantity) => put(`/api/cart/${String(productId)}?quantity=${quantity}`)

/** 删除购物车商品 DELETE /api/cart/{productId} */
export const removeFromCart = (productId) => del(`/api/cart/${String(productId)}`)

/** 选中/取消选中商品 PUT /api/cart/check/{productId} */
export const checkCartItem = (productId, checked) => put(`/api/cart/check/${String(productId)}`, { checked })

// ==================== 订单模块 ====================

/** 提交订单 POST /api/orders */
export const placeOrder = (data) => post('/api/orders', data)

/** 订单列表 GET /api/orders?page&pageSize&status */
export const getOrders = (params) => get('/api/orders', params)

/** 订单详情 GET /api/orders/{id} */
export const getOrderDetail = (id) => get(`/api/orders/${id}`)

/** 模拟支付 PUT /api/orders/{id}/pay */
export const payOrder = (id) => put(`/api/orders/${id}/pay`)

/** 取消订单 PUT /api/orders/{id}/cancel */
export const cancelOrder = (id) => put(`/api/orders/${id}/cancel`)

/** 确认收货 PUT /api/orders/{id}/confirm */
export const confirmReceipt = (id) => put(`/api/orders/${id}/confirm`)

// ==================== 收货地址模块 ====================

/** 获取地址列表 GET /api/addresses */
export const getAddressList = () => get('/api/addresses')

/** 新增地址 POST /api/addresses */
export const addAddress = (data) => post('/api/addresses', data)

/** 修改地址 PUT /api/addresses/{id} */
export const updateAddress = (id, data) => put(`/api/addresses/${id}`, data)

/** 删除地址 DELETE /api/addresses/{id} */
export const deleteAddress = (id) => del(`/api/addresses/${id}`)

/** 设为默认地址 PUT /api/addresses/{id}/default */
export const setDefaultAddress = (id) => put(`/api/addresses/${id}/default`)

// ==================== 公共模块 ====================

/** 轮播图列表 GET /api/banners */
export const getBanners = () => get('/api/banners')

/** 商品分类列表 GET /api/categories */
export const getCategories = () => get('/api/categories')
