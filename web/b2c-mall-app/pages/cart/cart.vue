<template>
  <view class="page-cart">
    <!-- 加载状态 -->
    <view class="loading-box" v-if="loading">
      <u-loading-icon size="40" text="加载中..." />
    </view>

    <!-- 已登录时显示购物车内容 -->
    <template v-else-if="userStore.isLogin">
      <!-- 购物车商品列表 -->
      <view class="cart-list" v-if="cartStore.cartList.length > 0">
        <view class="cart-item" v-for="item in cartStore.cartList" :key="item.productId">
          <!-- 圆形复选框 -->
          <view class="check-circle" :class="{ checked: item.checked }" @click="toggleCheck(item)">
            <u-icon v-if="item.checked" name="checkmark" size="14" color="#fff" />
          </view>

          <!-- 商品图片 -->
          <image class="item-img" :src="getImage(item.image)" mode="aspectFill"
                 @click="goDetail(item.productId)" />

          <!-- 商品信息 -->
          <view class="item-info">
            <text class="item-name text-ellipsis-2" @click="goDetail(item.productId)">{{ item.name }}</text>
            <view class="item-bottom">
              <text class="price">¥{{ item.price }}</text>
              <!-- 数量加减 -->
              <view class="quantity-ctrl">
                <view class="qty-btn minus" :class="{ disabled: item.quantity <= 1 }"
                      @click="changeQty(item, item.quantity - 1)">
                  <u-icon name="minus" size="12" color="#666" />
                </view>
                <text class="qty-num">{{ item.quantity }}</text>
                <view class="qty-btn plus" @click="changeQty(item, item.quantity + 1)">
                  <u-icon name="plus" size="12" color="#666" />
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空购物车 -->
      <u-empty v-else text="购物车是空的" mode="car">
        <button class="btn-go-shopping" @click="goIndex">去逛逛</button>
      </u-empty>
    </template>

    <!-- 未登录提示 -->
    <u-empty v-else text="请先登录" mode="permission">
      <button class="btn-go-shopping" @click="goLogin">去登录</button>
    </u-empty>

    <!-- 底部结算栏 -->
    <view class="bottom-bar" v-if="userStore.isLogin && cartStore.cartList.length > 0">
      <view class="bar-left" @click="toggleAllCheck">
        <view class="check-circle" :class="{ checked: cartStore.isAllChecked }">
          <u-icon v-if="cartStore.isAllChecked" name="checkmark" size="14" color="#fff" />
        </view>
        <text class="all-check-text">全选</text>
      </view>
      <view class="bar-center">
        <text class="total-label">合计：</text>
        <text class="total-price">¥{{ cartStore.totalPrice }}</text>
      </view>
      <view class="btn-checkout" :class="{ active: cartStore.checkedCount > 0 }" @click="handleCheckout">
        结算<text v-if="cartStore.checkedCount > 0" class="btn-count">({{ cartStore.checkedCount }})</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store/cart.js'
import { useUserStore } from '@/store/user.js'

const cartStore = useCartStore()
const userStore = useUserStore()
const loading = ref(false)

onShow(() => {
  if (userStore.isLogin) loadCart()
})

const loadCart = async () => {
  loading.value = true
  try { await cartStore.fetchCartList() }
  catch (e) { uni.showToast({ title: '加载购物车失败', icon: 'none' }) }
  finally { loading.value = false }
}

/** 切换单个商品选中 — 带错误处理 */
const toggleCheck = async (item) => {
  const newChecked = !item.checked
  item.checked = newChecked
  try {
    await cartStore.toggleCheckAction(item.productId, newChecked)
  } catch (e) {
    console.error('[购物车] 单个勾选失败:', e, 'productId:', item.productId, '类型:', typeof item.productId)
    item.checked = !newChecked
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

/** 全选/取消全选 */
const toggleAllCheck = async () => {
  const newAllChecked = !cartStore.isAllChecked
  // 乐观更新：先改所有项的 UI
  cartStore.cartList.forEach(item => { item.checked = newAllChecked })
  try {
    await cartStore.toggleAllCheckAction(newAllChecked)
  } catch (e) {
    // 失败时回滚并刷新
    uni.showToast({ title: '操作失败', icon: 'none' })
    await cartStore.fetchCartList()
  }
}

/** 修改数量 */
const changeQty = async (item, newQty) => {
  if (newQty < 1) return
  try { await cartStore.updateQuantityAction(item.productId, newQty) }
  catch (e) { uni.showToast({ title: '修改失败', icon: 'none' }) }
}

const getImage = (img) => img || '/static/default-product.png'
const goDetail = (id) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })

const handleCheckout = () => {
  if (cartStore.checkedCount === 0) return uni.showToast({ title: '请先勾选商品', icon: 'none' })
  if (!userStore.isLogin) return uni.showToast({ title: '请先登录', icon: 'none' })
  uni.navigateTo({ url: '/pages/order/confirm' })
}
const goIndex = () => uni.switchTab({ url: '/pages/index/index' })
const goLogin = () => uni.navigateTo({ url: '/pages/login/login' })
</script>

<style lang="scss" scoped>
.page-cart { min-height: 100vh; padding-bottom: 240rpx; background: #f5f5f5; }
.loading-box { display: flex; justify-content: center; padding-top: 200rpx; }

/* ===== 圆形复选框（核心组件）===== */
.check-circle {
  width: 40rpx; height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #d9d9d9;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  margin-right: 20rpx;
  transition: all 0.2s ease;
  box-sizing: border-box;
  &.checked {
    background: linear-gradient(135deg, #FF6034, #FF7A4D);
    border-color: transparent;
  }
}

/* ===== 商品卡片 ===== */
.cart-list { padding: 16rpx 20rpx; }
.cart-item {
  display: flex; align-items: center; background: #fff;
  margin-bottom: 16rpx; padding: 24rpx; border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.item-img {
  width: 180rpx; height: 180rpx; border-radius: 12rpx;
  margin-right: 20rpx; flex-shrink: 0;
  background: #f8f8f8;
}
.item-info {
  flex: 1; display: flex; flex-direction: column;
  justify-content: space-between; min-height: 180rpx;
}
.item-name { font-size: 28rpx; color: #333; line-height: 1.5; font-weight: 500; }

/* 价格 + 数量 */
.item-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8rpx; }
.price { color: #FF4444; font-size: 32rpx; font-weight: bold; }

/* 数量控制器 */
.quantity-ctrl { display: flex; align-items: center; background: #f5f5f5; border-radius: 10rpx; overflow: hidden; }
.qty-btn {
  width: 52rpx; height: 48rpx;
  display: flex; align-items: center; justify-content: center;
  &.minus { border-right: 1rpx solid #e8e8e8; }
  &.plus { border-left: 1rpx solid #e8e8e8; }
  &.disabled { opacity: 0.3; }
}
.qty-num { width: 64rpx; text-align: center; font-size: 28rpx; color: #333; font-weight: 500; }

/* ===== 底部结算栏 ===== */
.bottom-bar {
  position: fixed; bottom: 100rpx; left: 0; right: 0; background: #fff;
  display: flex; align-items: center; padding: 16rpx 24rpx;
  border-top: 1rpx solid #eee; z-index: 100;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
  .bar-left { display: flex; align-items: center; }
  .all-check-text { font-size: 28rpx; color: #333; margin-left: 12rpx; }
  .bar-center { flex: 1; text-align: right; padding-right: 24rpx; }
  .total-label { font-size: 26rpx; color: #666; }
  .total-price { color: #FF4444; font-size: 36rpx; font-weight: bold; }
}

/* 结算按钮 */
.btn-checkout {
  min-width: 180rpx; height: 72rpx; line-height: 72rpx;
  text-align: center; font-size: 30rpx; font-weight: bold;
  border-radius: 36rpx; color: #fff;
  background: #ccc; transition: all 0.25s ease;
  .btn-count { font-size: 24rpx; font-weight: normal; }
  &.active {
    background: linear-gradient(135deg, #FF6034, #FF7A4D);
    box-shadow: 0 6rpx 20rpx rgba(255,96,52,0.35);
  }
}

/* 空状态按钮 */
.btn-go-shopping {
  width: 260rpx; height: 76rpx; line-height: 76rpx;
  background: linear-gradient(135deg, #FF6034, #FF7A4D);
  color: #fff; border-radius: 38rpx; font-size: 28rpx;
  text-align: center; margin-top: 30rpx;
  box-shadow: 0 6rpx 20rpx rgba(255,96,52,0.3);
}
</style>
