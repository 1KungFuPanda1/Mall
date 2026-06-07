<template>
  <!-- ================================================
  支付成功页 — 下单后展示支付引导（模拟支付）
  ================================================ -->
  <view class="page-success">
    <!-- 成功图标 -->
    <view class="success-icon">
      <u-icon name="checkmark-circle" size="80" color="#07C160" />
    </view>
    <text class="success-text">下单成功！</text>
    <text class="amount-text">应付金额：¥{{ totalAmount }}</text>

    <!-- 支付中状态 -->
    <view v-if="paying" class="paying-box">
      <u-loading-icon size="40" text="正在支付..." />
    </view>

    <!-- 操作按钮 -->
    <view v-else class="action-btns">
      <button v-if="!paid" class="btn-pay" @click="handlePay">立即支付</button>
      <button v-else class="btn-paid" disabled>已支付</button>
      <button class="btn-orders" @click="goOrders">查看订单</button>
    </view>

    <!-- 支付说明 -->
    <view class="pay-note">
      <text class="note-title">支付说明</text>
      <text class="note-text">当前为模拟支付模式，点击"立即支付"将直接完成支付。</text>
      <text class="note-text">后续将接入支付宝沙箱环境进行真实支付。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { payOrder } from '@/services/index.js'

const orderId = ref('')
const totalAmount = ref('0.00')
const paying = ref(false)
const paid = ref(false)

onLoad((options) => {
  orderId.value = options.orderId || ''
  totalAmount.value = (options.totalAmount && options.totalAmount !== 'undefined') ? options.totalAmount : '0.00'
})

/** 立即支付 — 模拟支付流程 */
const handlePay = async () => {
  // 如果没有 orderId，直接走模拟支付
  if (!orderId.value) {
    await mockPay()
    return
  }

  // 有 orderId，先尝试调用真实接口
  paying.value = true
  try {
    await payOrder(orderId.value)
    uni.showToast({ title: '支付成功！', icon: 'success' })
    paid.value = true
    setTimeout(() => goOrders(), 1500)
  } catch (e) {
    // 真实接口失败，降级为模拟支付
    console.warn('真实支付接口调用失败，降级为模拟支付:', e.message || e)
    await mockPay()
  } finally {
    paying.value = false
  }
}

/** 模拟支付 */
const mockPay = () => {
  return new Promise((resolve) => {
    paying.value = true
    uni.showLoading({ title: '支付中...', mask: true })
    setTimeout(() => {
      uni.hideLoading()
      paying.value = false
      paid.value = true
      uni.showToast({ title: '模拟支付成功！', icon: 'success' })
      setTimeout(() => {
        goOrders()
        resolve()
      }, 1500)
    }, 1500)
  })
}

/** 跳转订单列表 */
const goOrders = () => {
  uni.switchTab({ url: '/pages/order/list' })
}
</script>

<style lang="scss" scoped>
.page-success {
  min-height: 100vh; background: #fff;
  display: flex; flex-direction: column; align-items: center; padding-top: 180rpx;
}
.success-icon { margin-bottom: 20rpx; }
.success-text { font-size: 36rpx; color: #333; font-weight: bold; }
.amount-text { font-size: 28rpx; color: #FF4444; margin-top: 16rpx; }

.paying-box {
  margin-top: 60rpx; padding: 40rpx;
  display: flex; flex-direction: column; align-items: center;
}

.action-btns {
  margin-top: 80rpx; width: 80%;
  display: flex; flex-direction: column; gap: 20rpx;
}
.btn-pay {
  width: 100%; height: 88rpx; line-height: 88rpx;
  background: linear-gradient(135deg, #FF6034, #FF8A6A); color: #fff;
  font-size: 32rpx; border-radius: 44rpx; text-align: center;
}
.btn-paid {
  width: 100%; height: 88rpx; line-height: 88rpx;
  background: #ccc; color: #fff;
  font-size: 32rpx; border-radius: 44rpx; text-align: center;
}
.btn-orders {
  width: 100%; height: 88rpx; line-height: 88rpx;
  background: #fff; color: #FF6034; border: 2rpx solid #FF6034;
  font-size: 32rpx; border-radius: 44rpx; text-align: center;
}

.pay-note {
  margin-top: 80rpx; padding: 30rpx; width: 85%;
  background: #f8f8f8; border-radius: 12rpx;
}
.note-title {
  display: block; font-size: 26rpx; color: #333; font-weight: bold; margin-bottom: 12rpx;
}
.note-text {
  display: block; font-size: 24rpx; color: #999; line-height: 1.6; margin-top: 6rpx;
}
</style>
