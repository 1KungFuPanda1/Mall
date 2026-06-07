<template>
  <!-- ================================================
  商品详情页 — 商品信息 + 加入购物车
  ================================================ -->
  <view class="page-detail" v-if="product">
    <!-- 商品图片轮播 -->
    <swiper class="img-swiper" indicator-dots circular v-if="imageList.length">
      <swiper-item v-for="(img, i) in imageList" :key="i">
        <image :src="img" mode="aspectFill" class="swiper-img" />
      </swiper-item>
    </swiper>

    <!-- 商品基本信息 -->
    <view class="info-card">
      <view class="price-row">
        <text class="price">¥{{ product.price }}</text>
        <text class="category-tag">{{ product.categoryName }}</text>
      </view>
      <text class="product-name">{{ product.name }}</text>
      <text class="stock-text">库存：{{ product.stock }} 件</text>
    </view>

    <!-- 商品描述（富文本） -->
    <view class="desc-card" v-if="product.description">
      <text class="desc-title">商品详情</text>
      <view class="desc-content" v-html="product.description" />
    </view>

    <!-- 底部操作栏 — 固定吸附 -->
    <view class="bottom-bar safe-bottom">
      <view class="bar-left">
        <view class="icon-item" @click="goCart">
          <u-icon name="shopping-cart" size="26" color="#333" />
          <text class="icon-text">购物车</text>
          <!-- 购物车数量角标 -->
          <u-badge v-if="cartStore.cartCount > 0" :value="cartStore.cartCount"
                   absolute :offset="[8, -4]" />
        </view>
      </view>
      <!-- 加入购物车 & 立即购买按钮 -->
      <button class="btn-add-cart" @click="handleAddCart">加入购物车</button>
    </view>
  </view>

  <!-- 加载中 -->
  <u-loading-page v-else loadingText="加载中..." />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getProductDetail } from '@/services/index.js'
import { useCartStore } from '@/store/cart.js'
import { useUserStore } from '@/store/user.js'

const cartStore = useCartStore() // 购物车 Store
const userStore = useUserStore() // 用户 Store
const product = ref(null) // 商品详情数据
const productId = ref(null) // 当前商品ID

// 解析多图URL列表
const imageList = computed(() => {
  if (!product.value || !product.value.image) return []
  return product.value.image.includes(',')
    ? product.value.image.split(',')
    : [product.value.image]
})

onLoad((options) => {
  productId.value = options.id // 接收商品ID参数
  loadDetail() // 加载详情
})

/** 加载商品详情 */
const loadDetail = async () => {
  try {
    product.value = await getProductDetail(productId.value)
  } catch (e) { /* 错误已处理 */ }
}

/** 加入购物车 */
const handleAddCart = async () => {
  // 校验登录状态
  if (!userStore.isLogin) {
    uni.showModal({
      title: '提示',
      content: '请先登录后再操作',
      success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/login/login' })
      }
    })
    return
  }
  // 调用购物车 Store 添加到购物车
  await cartStore.addAction(productId.value)
}

/** 跳转购物车 */
const goCart = () => uni.switchTab({ url: '/pages/cart/cart' })
</script>

<style lang="scss" scoped>
.page-detail { padding-bottom: 120rpx; }

// 商品大图轮播
.img-swiper { width: 100%; height: 750rpx; }
.swiper-img { width: 100%; height: 100%; }

// 商品信息卡片
.info-card { background: #fff; padding: 24rpx; margin-bottom: 16rpx; }
.price-row { display: flex; align-items: center; justify-content: space-between; }
.price { color: #FF4444; font-size: 48rpx; font-weight: bold; }
.category-tag { background: #FFF4F0; color: #FF6034; font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 12rpx; }
.product-name { font-size: 32rpx; color: #333; font-weight: bold; margin-top: 12rpx; display: block; }
.stock-text { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }

// 商品描述
.desc-card { background: #fff; padding: 24rpx; margin-bottom: 16rpx; }
.desc-title { font-size: 28rpx; color: #333; font-weight: bold; display: block; margin-bottom: 16rpx; }
.desc-content { font-size: 26rpx; color: #666; line-height: 1.8; }

// 底部操作栏
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  display: flex; align-items: center; padding: 12rpx 24rpx;
  border-top: 1rpx solid #eee; z-index: 100;
  .bar-left { display: flex; flex: 1; }
  .icon-item { position: relative; display: flex; flex-direction: column; align-items: center; margin-right: 40rpx; }
  .icon-text { font-size: 20rpx; color: #666; margin-top: 4rpx; }
  .btn-add-cart {
    width: 340rpx; height: 76rpx; line-height: 76rpx;
    background: linear-gradient(135deg, #FF6034, #FF8A6A);
    color: #fff; font-size: 28rpx; border-radius: 38rpx; text-align: center;
  }
}
</style>
