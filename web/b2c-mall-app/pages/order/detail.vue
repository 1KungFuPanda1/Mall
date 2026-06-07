<template>
  <!-- ================================================
  订单详情页 — 订单信息 + 商品明细 + 操作
  ================================================ -->
  <view class="page-order-detail" v-if="order">
    <!-- 订单状态卡片 -->
    <view class="status-card" :style="{ background: statusBg(order.status) }">
      <u-icon :name="statusIcon(order.status)" size="36" color="#fff" />
      <text class="status-text">{{ order.statusDesc }}</text>
    </view>

    <!-- 收货地址 -->
    <view class="info-card" v-if="addressInfo">
      <view class="info-title">收货信息</view>
      <text class="info-text">{{ addressInfo.receiverName }} {{ addressInfo.receiverPhone }}</text>
      <text class="info-text addr">
        {{ addressInfo.province }}{{ addressInfo.city }}{{ addressInfo.district }} {{ addressInfo.detail }}
      </text>
    </view>

    <!-- 订单编号 -->
    <view class="info-card">
      <view class="info-title">订单信息</view>
      <view class="info-row"><text class="label">订单编号</text><text class="val">{{ order.orderNo }}</text></view>
      <view class="info-row"><text class="label">下单时间</text><text class="val">{{ order.createTime }}</text></view>
      <view class="info-row" v-if="order.payTime"><text class="label">支付时间</text><text class="val">{{ order.payTime }}</text></view>
      <view class="info-row" v-if="order.deliveryTime"><text class="label">发货时间</text><text class="val">{{ order.deliveryTime }}</text></view>
      <view class="info-row" v-if="order.finishTime"><text class="label">完成时间</text><text class="val">{{ order.finishTime }}</text></view>
    </view>

    <!-- 商品明细 -->
    <view class="info-card">
      <view class="info-title">商品明细</view>
      <view class="goods-item" v-for="item in order.items" :key="item.productId">
        <image class="goods-img" :src="getImage(item.productImage)" mode="aspectFill" />
        <view class="goods-info">
          <text class="goods-name text-ellipsis">{{ item.productName }}</text>
          <text class="goods-price">¥{{ item.price }} x {{ item.quantity }}</text>
        </view>
        <text class="goods-subtotal">¥{{ item.totalAmount }}</text>
      </view>
      <view class="total-row">
        <text>合计：</text><text class="total-price">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="bottom-bar safe-bottom" v-if="showActions">
      <button class="action-btn cancel" v-if="order.status === 'WAIT_PAY'" @click="handleCancel">取消订单</button>
      <button class="action-btn primary" v-if="order.status === 'WAIT_PAY'" @click="handlePay">去支付</button>
      <button class="action-btn primary" v-if="order.status === 'RECEIVING'" @click="handleConfirm">确认收货</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail, payOrder, cancelOrder, confirmReceipt } from '@/services/index.js'

const order = ref(null)
let currentId = null

onLoad((options) => {
  currentId = options.id
  loadDetail()
})

const loadDetail = async () => {
  try {
    order.value = await getOrderDetail(currentId)
  } catch (e) { /* 错误已处理 */ }
}

// 解析地址JSON
const addressInfo = computed(() => {
  if (!order.value || !order.value.addressJson) return null
  try {
    return typeof order.value.addressJson === 'string'
      ? JSON.parse(order.value.addressJson)
      : order.value.addressJson
  } catch (e) { return null }
})

const showActions = computed(() => {
  return order.value && (order.value.status === 'WAIT_PAY' || order.value.status === 'RECEIVING')
})

const handlePay = async () => {
  try { await payOrder(currentId); loadDetail() } catch (e) {}
}
const handleCancel = async () => {
  uni.showModal({
    title: '提示', content: '确定要取消该订单吗？',
    success: async (res) => { if (res.confirm) { try { await cancelOrder(currentId); loadDetail() } catch (e) {} } }
  })
}
const handleConfirm = async () => {
  uni.showModal({
    title: '提示', content: '确认已收到商品？',
    success: async (res) => { if (res.confirm) { try { await confirmReceipt(currentId); loadDetail() } catch (e) {} } }
  })
}

const getImage = (img) => img || '/static/default-product.png'
const statusBg = (s) => {
  const map = { WAIT_PAY: '#FF9500', PAID: '#FF6034', RECEIVING: '#FF6034', COMPLETED: '#07C160', CANCELLED: '#999' }
  return `linear-gradient(135deg, ${map[s] || '#FF6034'}, ${s === 'WAIT_PAY' ? '#FFB800' : s === 'CANCELLED' ? '#bbb' : '#FF8A6A'})`
}
const statusIcon = (s) => {
  const map = { WAIT_PAY: 'clock', PAID: 'checkmark-circle', RECEIVING: 'car', COMPLETED: 'checkmark-circle', CANCELLED: 'close-circle' }
  return map[s] || 'info-circle'
}
</script>

<style lang="scss" scoped>
.page-order-detail { min-height: 100vh; background: #f5f5f5; padding-bottom: 120rpx; }

.status-card {
  display: flex; align-items: center; padding: 40rpx 30rpx; color: #fff;
  .status-text { font-size: 34rpx; font-weight: bold; margin-left: 16rpx; }
}

.info-card { background: #fff; padding: 24rpx; margin-bottom: 16rpx; }
.info-title { font-size: 28rpx; color: #333; font-weight: bold; margin-bottom: 16rpx; }
.info-text { font-size: 26rpx; color: #666; display: block; margin-bottom: 6rpx; }
.info-row { display: flex; justify-content: space-between; padding: 8rpx 0; }
.label { font-size: 24rpx; color: #999; }
.val { font-size: 24rpx; color: #666; }

.goods-item { display: flex; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.goods-img { width: 80rpx; height: 80rpx; border-radius: 8rpx; margin-right: 16rpx; }
.goods-info { flex: 1; }
.goods-name { font-size: 24rpx; color: #333; }
.goods-price { font-size: 22rpx; color: #999; }
.goods-subtotal { font-size: 26rpx; color: #333; margin-left: 16rpx; }
.total-row { text-align: right; padding-top: 16rpx; font-size: 28rpx; color: #333; }
.total-price { color: #FF4444; font-size: 34rpx; font-weight: bold; }

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  display: flex; justify-content: flex-end; gap: 16rpx; padding: 12rpx 24rpx;
  border-top: 1rpx solid #eee; z-index: 100;
  .action-btn { width: 200rpx; height: 72rpx; line-height: 72rpx; border-radius: 36rpx; font-size: 26rpx; text-align: center; }
  .cancel { background: #fff; color: #666; border: 1rpx solid #ddd; }
  .primary { background: linear-gradient(135deg, #FF6034, #FF8A6A); color: #fff; }
}
</style>
