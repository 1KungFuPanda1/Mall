import request from './request'

/** 管理员登录 POST /admin/login */
export const adminLogin = (data) => request.post('/admin/login', data)

/** 今日统计 GET /admin/statistics/today */
export const getStatistics = () => request.get('/admin/statistics/today')

/** 分类列表 GET /admin/categories */
export const getCategoryList = () => request.get('/admin/categories')

/** 新增分类 POST /admin/categories */
export const addCategory = (data) => request.post('/admin/categories', data)

/** 修改分类 PUT /admin/categories/{id} */
export const updateCategory = (id, data) => request.put(`/admin/categories/${id}`, data)

/** 删除分类 DELETE /admin/categories/{id} */
export const deleteCategory = (id) => request.delete(`/admin/categories/${id}`)

/** 商品列表 GET /admin/products */
export const getProductList = (params) => request.get('/admin/products', { params })

/** 新增商品 POST /admin/products */
export const addProduct = (data) => request.post('/admin/products', data)

/** 修改商品 PUT /admin/products/{id} */
export const updateProduct = (id, data) => request.put(`/admin/products/${id}`, data)

/** 删除商品 DELETE /admin/products/{id} */
export const deleteProduct = (id) => request.delete(`/admin/products/${id}`)

/** 上下架 PUT /admin/products/{id}/status */
export const toggleProductStatus = (id) => request.put(`/admin/products/${id}/status`)

/** 图片上传 POST /admin/upload */
export const uploadFile = (formData) => request.post('/admin/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})

/** 订单列表 GET /admin/orders */
export const getOrderList = (params) => request.get('/admin/orders', { params })

/** 订单详情 GET /admin/orders/{id} */
export const getOrderDetail = (id) => request.get(`/admin/orders/${id}`)

/** 发货 PUT /admin/orders/{id}/deliver */
export const deliverOrder = (id, data) => request.put(`/admin/orders/${id}/deliver`, data)

/** 用户列表 GET /admin/users */
export const getUserList = (params) => request.get('/admin/users', { params })

/** 启用/禁用 PUT /admin/users/{id}/status */
export const toggleUserStatus = (id) => request.put(`/admin/users/${id}/status`)

/** 轮播图列表 GET /admin/banners */
export const getBannerList = () => request.get('/admin/banners')

/** 新增轮播图 POST /admin/banners */
export const addBanner = (data) => request.post('/admin/banners', data)

/** 修改轮播图 PUT /admin/banners/{id} */
export const updateBanner = (id, data) => request.put(`/admin/banners/${id}`, data)

/** 删除轮播图 DELETE /admin/banners/{id} */
export const deleteBanner = (id) => request.delete(`/admin/banners/${id}`)

/** 取消订单 PUT /admin/orders/{id}/cancel */
export const cancelOrder = (id) => request.put(`/admin/orders/${id}/cancel`)

/** 模拟支付 PUT /admin/orders/{id}/pay */
export const payOrder = (id) => request.put(`/admin/orders/${id}/pay`)

/** 确认收货 PUT /admin/orders/{id}/confirm */
export const confirmOrderReceipt = (id) => request.put(`/admin/orders/${id}/confirm`)

/** 结算数据汇总 GET /admin/statistics/summary */
export const getStatisticsSummary = () => request.get('/admin/statistics/summary')
