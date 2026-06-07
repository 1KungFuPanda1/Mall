<template>
  <!-- ================================================
  确认订单页 — 收货地址 + 商品清单 + 提交订单
  ================================================ -->
  <view class="page-confirm">
    <!-- 收货地址区域 -->
    <view class="address-section" @click="goSelectAddress">
      <template v-if="selectedAddress">
        <view class="addr-top">
          <u-icon name="map" size="20" color="#FF6034" />
          <text class="addr-name">{{ selectedAddress.receiverName }}</text>
          <text class="addr-phone">{{ selectedAddress.receiverPhone }}</text>
        </view>
        <text class="addr-detail">
          {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }} {{ selectedAddress.detail }}
        </text>
      </template>
      <!-- 无地址时提示添加 -->
      <view v-else class="addr-empty">
        <u-icon name="plus-circle" size="20" color="#FF6034" />
        <text>请添加收货地址</text>
      </view>
      <u-icon name="arrow-right" size="16" color="#999" />
    </view>

    <!-- 商品清单 -->
    <view class="goods-section">
      <view class="goods-item" v-for="item in checkedItems" :key="item.productId">
        <image class="goods-img" :src="getImage(item.image)" mode="aspectFill" />
        <view class="goods-info">
          <text class="goods-name text-ellipsis-2">{{ item.name }}</text>
          <view class="goods-bottom">
            <text class="price">¥{{ item.price }}</text>
            <text class="qty">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单金额明细 -->
    <view class="amount-section">
      <view class="amount-row">
        <text>商品总额</text>
        <text class="amount-val">¥{{ cartStore.totalPrice }}</text>
      </view>
      <view class="amount-row total-row">
        <text>合计</text>
        <text class="total-val">¥{{ cartStore.totalPrice }}</text>
      </view>
    </view>

    <!-- 提交订单按钮 -->
    <view class="submit-bar safe-bottom">
      <view class="submit-left">
        <text>合计：</text>
        <text class="submit-price">¥{{ cartStore.totalPrice }}</text>
      </view>
      <button class="btn-submit" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '提交中...' : '提交订单' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAddressList, placeOrder } from '@/services/index.js'
import { useCartStore } from '@/store/cart.js'

const cartStore = useCartStore()
const selectedAddress = ref(null) // 选中的收货地址
const checkedItems = ref([]) // 已选中商品
const submitting = ref(false) // 提交状态

onMounted(() => {
  loadAddress() // 加载地址
  loadCheckedItems() // 加载选中商品
})

/** 加载默认收货地址 */
const loadAddress = async () => {
  try {
    const list = await getAddressList()
    if (list && list.length > 0) {
      // 优先选择默认地址，否则选第一个
      selectedAddress.value = list.find(a => a.isDefault === 1) || list[0]
    }
  } catch (e) { /* 无地址 */ }
}

/** 加载购物车已选中的商品 */
const loadCheckedItems = () => {
  checkedItems.value = cartStore.checkedItems
}

/** 提交订单 */
const handleSubmit = async () => {
  if (!selectedAddress.value) {
    return uni.showToast({ title: '请选择收货地址', icon: 'none' })
  }
  if (checkedItems.value.length === 0) {
    return uni.showToast({ title: '请选择商品', icon: 'none' })
  }

  submitting.value = true
  try {
    const order = await placeOrder({
      addressId: selectedAddress.value.id,
      remark: ''
    })
    // 调试：确认订单对象结构（可在控制台查看）
    console.log('[提交订单] 返回结果:', JSON.stringify(order), '原始:', order)
    console.log('[提交订单] orderId:', order?.id, 'orderNo:', order?.orderNo)
    uni.showToast({ title: '下单成功', icon: 'success' })
    // 跳转到支付成功页面
    setTimeout(() => {
      // 兼容多种可能的 ID 字段名
      const oid = order?.id || order?.orderId || order?.orderNo || ''
      const amount = cartStore.totalPrice || '0.00'
      uni.redirectTo({ url: `/pages/order/pay-success?orderId=${oid}&totalAmount=${amount}` })
    }, 500)
  } catch (e) { /* 错误已处理 */ }
  finally { submitting.value = false }
}

const goSelectAddress = () => {
  uni.navigateTo({ url: '/pages/address/list?select=true' })
}

const getImage = (img) => img || '/static/default-product.png'
</script>

<style lang="scss" scoped>
.page-confirm { min-height: 100vh; padding-bottom: 120rpx; background: #f5f5f5; }

.address-section {
  display: flex; align-items: center; background: #fff;
  padding: 24rpx; margin-bottom: 16rpx;
  .addr-top { display: flex; align-items: center; flex: 1; }
  .addr-name { font-size: 30rpx; color: #333; font-weight: bold; margin: 0 10rpx 0 10rpx; }
  .addr-phone { font-size: 26rpx; color: #666; }
  .addr-detail { font-size: 24rpx; color: #999; margin-top: 8rpx; }
  .addr-empty { flex: 1; display: flex; align-items: center; color: #FF6034; font-size: 28rpx; }
}

.goods-section { background: #fff; margin-bottom: 16rpx; padding: 0 24rpx; }
.goods-item { display: flex; padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.goods-img { width: 120rpx; height: 120rpx; border-radius: 8rpx; margin-right: 16rpx; }
.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.goods-name { font-size: 26rpx; color: #333; }
.goods-bottom { display: flex; justify-content: space-between; }
.price { color: #FF4444; font-size: 28rpx; font-weight: bold; }
.qty { font-size: 24rpx; color: #999; }

.amount-section { background: #fff; padding: 24rpx; margin-bottom: 16rpx; }
.amount-row { display: flex; justify-content: space-between; font-size: 26rpx; color: #666; margin-bottom: 12rpx; }
.total-row { font-size: 30rpx; color: #333; font-weight: bold; }
.total-val { color: #FF4444; }

.submit-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  display: flex; align-items: center; padding: 12rpx 24rpx;
  border-top: 1rpx solid #eee; z-index: 100;
  .submit-left { flex: 1; }
  .submit-price { color: #FF4444; font-size: 36rpx; font-weight: bold; }
  .btn-submit {
    width: 240rpx; height: 76rpx; line-height: 76rpx;
    background: linear-gradient(135deg, #FF6034, #FF8A6A);
    color: #fff; font-size: 30rpx; border-radius: 38rpx; text-align: center;
  }
}
</style>
