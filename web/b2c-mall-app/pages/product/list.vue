<template>
  <!-- ================================================
  商品列表页 — 按分类筛选展示商品
  ================================================ -->
  <view class="page-list">
    <view class="product-grid">
      <view class="product-card" v-for="item in list" :key="item.id" @click="goDetail(item.id)">
        <image class="product-img" :src="getImage(item.image)" mode="aspectFill" lazy-load />
        <view class="product-info">
          <text class="product-name text-ellipsis-2">{{ item.name }}</text>
          <view class="product-bottom">
            <text class="price">¥{{ item.price }}</text>
          </view>
        </view>
      </view>
    </view>
    <u-loadmore :status="loadStatus" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getProducts } from '@/services/index.js'

const categoryId = ref(null) // 分类ID（从上一页传入）
const list = ref([]) // 商品列表
const page = ref(1)
const loadStatus = ref('loadmore')

onLoad((options) => {
  categoryId.value = options.categoryId || null
  uni.setNavigationBarTitle({ title: options.name || '商品列表' }) // 动态标题
})

onMounted(() => loadData())
onReachBottom(() => {
  if (loadStatus.value === 'nomore') return
  page.value++
  loadData(true)
})

const loadData = async (append = false) => {
  loadStatus.value = 'loading'
  try {
    const res = await getProducts({ page: page.value, pageSize: 10, categoryId: categoryId.value })
    const records = res.records || []
    list.value = append ? [...list.value, ...records] : records
    loadStatus.value = records.length < 10 ? 'nomore' : 'loadmore'
  } catch (e) { loadStatus.value = 'loadmore' }
}

const getImage = (img) => img ? (img.includes(',') ? img.split(',')[0] : img) : '/static/default-product.png'
const goDetail = (id) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
</script>

<style lang="scss" scoped>
.product-grid { display: flex; flex-wrap: wrap; padding: 20rpx; }
.product-card {
  width: calc(50% - 10rpx); margin: 0 10rpx 20rpx 0; background: #fff;
  border-radius: 12rpx; overflow: hidden;
  &:nth-child(even) { margin-right: 0; }
  .product-img { width: 100%; height: 340rpx; }
  .product-info { padding: 16rpx; }
  .product-name { font-size: 26rpx; color: #333; height: 72rpx; }
  .product-bottom { margin-top: 10rpx; }
  .price { color: #FF4444; font-size: 30rpx; font-weight: bold; }
}
</style>
