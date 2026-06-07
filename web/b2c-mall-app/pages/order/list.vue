<template>
  <view class="page-order-list">
    <!-- 订单状态Tab切换 -->
    <view class="tabs">
      <view class="tab-item" v-for="tab in tabs" :key="tab.value"
            :class="{ active: currentStatus === tab.value }"
            @click="switchTab(tab.value)">
        {{ tab.label }}
      </view>
    </view>

    <!-- 订单列表 -->
    <view v-if="orderList.length > 0" class="order-list">
      <view class="order-card" v-for="order in orderList" :key="order.id"
            @click="goDetail(order.id)">
        <view class="order-header">
          <text class="order-no">订单号：{{ order.orderNo }}</text>
          <text class="order-status" :style="{ color: statusColor(order.status) }">
            {{ order.statusDesc }}
          </text>
        </view>
        <view class="order-goods" v-if="order.items">
          <view class="goods-row" v-for="item in order.items" :key="item.productId">
            <image class="goods-img" :src="getImage(item.productImage)" mode="aspectFill" />
            <view class="goods-info">
              <text class="goods-name text-ellipsis">{{ item.productName }}</text>
              <text class="goods-price">¥{{ item.price }} x{{ item.quantity }}</text>
            </view>
          </view>
        </view>
        <view class="order-footer">
          <text class="total-text">共 {{ totalCount(order.items) }} 件商品 合计：</text>
          <text class="total-price">¥{{ order.totalAmount }}</text>
        </view>
        <view class="order-actions" v-if="order.status !== 'COMPLETED' && order.status !== 'CANCELLED'">
          <button class="action-btn cancel-btn" v-if="order.status === 'WAIT_PAY'"
                  @click.stop="handleCancel(order.id)">取消订单</button>
          <button class="action-btn pay-btn" v-if="order.status === 'WAIT_PAY'"
                  @click.stop="handlePay(order.id)">去支付</button>
          <button class="action-btn confirm-btn" v-if="order.status === 'RECEIVING'"
                  @click.stop="handleConfirm(order.id)">确认收货</button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <u-empty v-if="orderList.length === 0 && !hasError" text="暂无订单" mode="order" />
    <!-- 错误状态 -->
    <view v-if="hasError" class="error-state">
      <u-empty text="加载失败" mode="error" />
      <text class="error-msg">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadOrders()">重新加载</button>
    </view>

    <u-loadmore :status="loadStatus" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { getOrders, payOrder, cancelOrder, confirmReceipt } from '@/services/index.js'

const tabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'WAIT_PAY' },
  { label: '待收货', value: 'RECEIVING' },
  { label: '已完成', value: 'COMPLETED' }
]

const currentStatus = ref('')
const orderList = ref([])
const page = ref(1)
const loadStatus = ref('loadmore')
const hasError = ref(false)
const errorMsg = ref('')

onShow(() => { page.value = 1; loadOrders() })
// 上拉触底：loading/nomore 守卫防止重复触发和并发请求
onReachBottom(() => {
  if (loadStatus.value === 'loading' || loadStatus.value === 'nomore') return
  page.value++
  loadOrders(true)
})

const switchTab = (status) => {
  currentStatus.value = status
  page.value = 1
  loadOrders()
}

const loadOrders = async (append = false) => {
  loadStatus.value = 'loading'
  hasError.value = false
  errorMsg.value = ''
  try {
    const params = { page: page.value, pageSize: 10 }
    if (currentStatus.value) params.status = currentStatus.value
    const res = await getOrders(params)
    const records = Array.isArray(res) ? res : (res?.records || res?.list || [])
    if (append) {
      orderList.value.push(...records)
    } else {
      orderList.value = records
    }
    loadStatus.value = records.length < 10 ? 'nomore' : 'loadmore'
  } catch (e) {
    // 失败时回退页码（仅追加模式），防止跳页
    if (append) page.value = Math.max(1, page.value - 1)
    hasError.value = true
    errorMsg.value = e?.message || '加载失败'
    loadStatus.value = 'loadmore'
    if (e?.statusCode === 401) {
      uni.showModal({ title: '登录已过期', content: '请重新登录', showCancel: false,
        success: () => uni.reLaunch({ url: '/pages/login/login' }) })
    }
  }
}

const handlePay = async (id) => {
  try { await payOrder(id); uni.showToast({ title: '支付成功', icon: 'success' }); loadOrders() }
  catch (e) {}
}
const handleCancel = async (id) => {
  uni.showModal({ title: '提示', content: '确定要取消该订单吗？',
    success: async (res) => { if (res.confirm) { try { await cancelOrder(id); loadOrders() } catch (e) {} } } })
}
const handleConfirm = async (id) => {
  uni.showModal({ title: '提示', content: '确认已收到商品？',
    success: async (res) => { if (res.confirm) { try { await confirmReceipt(id); loadOrders() } catch (e) {} } } })
}

const totalCount = (items) => items ? items.reduce((s, i) => s + i.quantity, 0) : 0
const getImage = (img) => img || '/static/default-product.png'
const statusColor = (s) => ({ WAIT_PAY: '#FF9500', RECEIVING: '#FF6034', COMPLETED: '#07C160', CANCELLED: '#999', PAID: '#FF6034' }[s] || '#333')
const goDetail = (id) => uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
</script>

<style lang="scss" scoped>
.page-order-list { min-height: 100vh; background: #f5f5f5; padding-bottom: 60rpx; }

.tabs { display: flex; background: #fff; padding: 0 20rpx; margin-bottom: 12rpx; }
.tab-item { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #666; border-bottom: 4rpx solid transparent; }
.tab-item.active { color: #FF6034; border-bottom-color: #FF6034; font-weight: bold; }

.order-card { background: #fff; margin: 12rpx 20rpx; border-radius: 12rpx; padding: 20rpx; }
.order-header { display: flex; justify-content: space-between; padding-bottom: 16rpx; border-bottom: 1rpx solid #f5f5f5; }
.order-no { font-size: 24rpx; color: #999; }
.order-status { font-size: 26rpx; font-weight: bold; }

.goods-row { display: flex; padding: 16rpx 0; }
.goods-img { width: 100rpx; height: 100rpx; border-radius: 8rpx; margin-right: 16rpx; }
.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.goods-name { font-size: 26rpx; color: #333; }
.goods-price { font-size: 24rpx; color: #999; }

.order-footer { text-align: right; padding: 16rpx 0; }
.total-text { font-size: 24rpx; color: #666; }
.total-price { color: #FF4444; font-size: 30rpx; font-weight: bold; }

.order-actions { display: flex; justify-content: flex-end; gap: 16rpx; padding-top: 12rpx; }
.action-btn { width: 160rpx; height: 56rpx; line-height: 56rpx; font-size: 24rpx; border-radius: 28rpx; text-align: center; border: 1rpx solid #ddd; color: #666; background: #fff; }
.pay-btn { background: linear-gradient(135deg, #FF6034, #FF8A6A); color: #fff; border: none; }
.confirm-btn { background: #FF6034; color: #fff; border: none; }

.error-state { text-align: center; padding: 40rpx 0; }
.error-msg { display: block; font-size: 24rpx; color: #999; margin: 16rpx 0; }
.retry-btn { margin-top: 20rpx; width: 240rpx; height: 64rpx; line-height: 64rpx; font-size: 26rpx; border-radius: 32rpx; background: #FF6034; color: #fff; }
</style>
