<template>
  <!-- ================================================
  首页 — C端移动商城入口页
  模块：顶部搜索栏 | 轮播图 | 分类导航 | 商品列表
  ================================================ -->
  <view class="page-index">
    <!-- 顶部搜索栏（点击跳转搜索页） -->
    <view class="search-bar" @click="goSearch">
      <u-icon name="search" size="20" color="#999" /> <!-- 搜索图标 -->
      <text class="search-text">搜索商品</text> <!-- 占位提示 -->
    </view>

    <!-- 轮播图 -->
    <u-swiper
      v-if="banners.length > 0" :list="bannerList" height="300rpx"
      indicator indicatorMode="dot" circular radius="12"
      keyName="imageUrl" />
    <!-- 轮播图加载中的占位 -->
    <view v-else class="swiper-placeholder" />

    <!-- 分类导航（横向滚动图标网格） -->
    <view class="category-nav">
      <scroll-view scroll-x class="category-scroll">
        <view class="category-item" v-for="(cat, index) in categoryList" :key="cat.id || index"
              @click="goCategory(cat.id, cat.name)">
          <!-- 分类图标（emoji） -->
          <text class="category-emoji">{{ getCategoryEmoji(cat.name) }}</text>
          <text class="category-name">{{ cat.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 商品列表标题 -->
    <view class="section-title">
      <text class="title-text">热门推荐</text>
    </view>

    <!-- 商品瀑布流/网格列表（两列） -->
    <view class="product-grid">
      <view class="product-card" v-for="product in productList" :key="product.id"
            @click="goDetail(product.id)">
        <!-- 商品图片 -->
        <image class="product-img" :src="getImage(product.image)"
               mode="aspectFill" lazy-load />
        <!-- 商品信息 -->
        <view class="product-info">
          <text class="product-name text-ellipsis-2">{{ product.name }}</text>
          <view class="product-bottom">
            <text class="price">¥{{ product.price }}</text>
            <text class="sales">已售 {{ product.stock || 0 }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载更多提示 -->
    <u-loadmore :status="loadStatus" loadingText="加载中..." loadmoreText="上拉加载更多"
                nomoreText="没有更多了" />

    <!-- 回到顶部按钮 -->
    <u-back-top :scroll-top="scrollTop" top="600" />
  </view>
</template>

<script setup>
// ================================================
// 首页逻辑 — Composition API
// ================================================
import { ref, onMounted, computed } from 'vue'
import { onPageScroll, onReachBottom } from '@dcloudio/uni-app'
import { getBanners, getCategories, getProducts } from '@/services/index.js'

// ==================== 响应式数据 ====================
const banners = ref([]) // 轮播图列表
const categories = ref([]) // 商品分类列表
const productList = ref([]) // 商品列表
const scrollTop = ref(0) // 滚动位置（用于回到顶部按钮）
const page = ref(1) // 当前页码
const pageSize = ref(10) // 每页数量
const loadStatus = ref('loadmore') // 加载状态: loadmore/loading/nomore

// ==================== 分类 Emoji 映射 ====================
/** 分类名称 → emoji 图标 */
const categoryEmojiMap = {
  '手机数码': '📱', '手机': '📱', '数码': '📱',
  '电脑办公': '💻', '电脑': '💻', '办公': '💻',
  '服装鞋帽': '👕', '服装': '👕', '鞋帽': '👟', '鞋': '👟',
  '食品生鲜': '🍎', '食品': '🍎', '生鲜': '🥬', '水果': '🍎',
  '家居家电': '🏠', '家居': '🏠', '家电': '🏠',
}
const defaultEmoji = '📦'

// ==================== 计算属性 ====================
/** 将轮播图数据转为 u-swiper 组件需要的格式 */
const bannerList = computed(() => {
  return banners.value.map(b => ({ imageUrl: b.imageUrl, linkUrl: b.linkUrl }))
})

/** 分类列表：后端数据 + 本地 fallback */
const categoryList = computed(() => {
  if (categories.value.length > 0) return categories.value
  // 后端没返回时显示默认分类
  return [
    { id: 1, name: '手机数码', icon: 'phone' },
    { id: 2, name: '电脑办公', icon: 'computer' },
    { id: 3, name: '服装鞋帽', icon: 'clothes' },
    { id: 4, name: '食品生鲜', icon: 'food' },
    { id: 5, name: '家居家电', icon: 'home' }
  ]
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadBanners() // 加载轮播图
  loadCategories() // 加载分类
  loadProducts() // 加载商品列表
})

// 监听页面滚动（用于回到顶部按钮）
onPageScroll((e) => { scrollTop.value = e.scrollTop })

// 上拉触底加载更多
onReachBottom(() => {
  if (loadStatus.value === 'nomore') return // 没有更多了
  page.value++ // 翻页
  loadProducts(true) // 追加加载
})

// ==================== 数据加载方法 ====================
/** 加载轮播图 */
const loadBanners = async () => {
  try { banners.value = await getBanners() } catch (e) { /* 静默失败 */ }
}

/** 加载商品分类 */
const loadCategories = async () => {
  try { categories.value = await getCategories() } catch (e) { /* 静默失败 */ }
}

/** 加载商品列表 */
const loadProducts = async (append = false) => {
  loadStatus.value = 'loading'
  try {
    const res = await getProducts({ page: page.value, pageSize: pageSize.value })
    const records = res.records || []
    if (append) {
      productList.value.push(...records) // 追加数据
    } else {
      productList.value = records // 替换数据
    }
    // 判断是否还有更多数据
    loadStatus.value = records.length < pageSize.value ? 'nomore' : 'loadmore'
  } catch (e) {
    loadStatus.value = 'loadmore'
  }
}

// ==================== 图片/图标处理 ====================
/** 获取分类 emoji（按名称关键词匹配） */
const getCategoryEmoji = (name) => {
  if (!name) return defaultEmoji
  for (const [key, emoji] of Object.entries(categoryEmojiMap)) {
    if (name.includes(key)) return emoji
  }
  return defaultEmoji
}

/** 获取商品首张图片（image 字段可能是逗号分隔的多图，来自后端 /uploads/ 路径） */
const getImage = (image) => {
  if (!image) return '' // 无图时显示空白，由 CSS 背景色兜底
  return image.includes(',') ? image.split(',')[0] : image
}

// ==================== 页面跳转 ====================
const goSearch = () => uni.navigateTo({ url: '/pages/product/search' })
const goDetail = (id) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
const goCategory = (id, name) => uni.navigateTo({ url: `/pages/product/list?categoryId=${id}&name=${name}` })
</script>

<style lang="scss" scoped>
.page-index { padding-bottom: 20rpx; }

// 搜索栏
.search-bar {
  display: flex; align-items: center; background: #fff;
  margin: 16rpx 20rpx; padding: 16rpx 20rpx;
  border-radius: 40rpx; border: 1rpx solid #eee;
  .search-text { margin-left: 10rpx; color: #999; font-size: 26rpx; }
}

// 轮播图占位
.swiper-placeholder { height: 300rpx; background: #fff; margin: 0 20rpx; border-radius: 12rpx; }

// 分类导航
.category-nav {
  background: #fff; margin: 16rpx 20rpx; border-radius: 12rpx; padding: 20rpx 0;
  .category-scroll { display: flex; white-space: nowrap; padding: 0 10rpx; }
  .category-item {
    display: inline-flex; flex-direction: column; align-items: center;
    width: 140rpx; flex-shrink: 0;
    .category-emoji {
      font-size: 44rpx; line-height: 80rpx;
    }
    .category-name { margin-top: 10rpx; font-size: 24rpx; color: #333; }
  }
}

// 区块标题
.section-title {
  display: flex; align-items: center; padding: 20rpx 20rpx 10rpx;
  .title-text { font-size: 32rpx; font-weight: bold; color: #333; }
}

// 商品网格
.product-grid {
  display: flex; flex-wrap: wrap; padding: 0 20rpx;
  .product-card {
    width: calc(50% - 10rpx); margin: 0 10rpx 20rpx 0;
    background: #fff; border-radius: 12rpx; overflow: hidden;
    &:nth-child(even) { margin-right: 0; }
    .product-img { width: 100%; height: 340rpx; background: #f5f5f5; }
    .product-info { padding: 16rpx; }
    .product-name { font-size: 26rpx; color: #333; line-height: 1.4; height: 72rpx; }
    .product-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 10rpx; }
    .price { color: #FF4444; font-size: 30rpx; font-weight: bold; }
    .sales { font-size: 22rpx; color: #999; }
  }
}
</style>
