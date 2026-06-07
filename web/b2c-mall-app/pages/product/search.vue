<template>
  <!-- ================================================
  商品搜索页 — 关键字模糊搜索
  ================================================ -->
  <view class="page-search">
    <!-- 搜索框 -->
    <view class="search-header">
      <input class="search-input" v-model="keyword" placeholder="输入商品名称搜索"
             confirm-type="search" @confirm="doSearch" />
      <button class="btn-search" @click="doSearch">搜索</button>
    </view>

    <!-- 搜索结果列表 -->
    <view class="search-result" v-if="keyword">
      <view class="product-item" v-for="item in list" :key="item.id" @click="goDetail(item.id)">
        <image class="item-img" :src="getImage(item.image)" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name text-ellipsis-2">{{ item.name }}</text>
          <text class="price">¥{{ item.price }}</text>
        </view>
      </view>
      <!-- 空状态 -->
      <u-empty v-if="list.length === 0 && !loading" text="没有找到相关商品" mode="search" />
    </view>

    <!-- 搜索历史/热门搜索（未输入关键字时展示） -->
    <view v-else class="hot-search">
      <text class="hot-title">热门搜索</text>
      <view class="hot-tags">
        <text class="tag" v-for="tag in hotTags" :key="tag" @click="keyword = tag; doSearch()">{{ tag }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { searchProducts } from '@/services/index.js'

const keyword = ref('') // 搜索关键字
const list = ref([]) // 搜索结果
const loading = ref(false) // 加载状态
const hotTags = ['手机', '电脑', 'T恤', '鞋子', '华为'] // 热门搜索词

const doSearch = async () => {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    const res = await searchProducts({ keyword: keyword.value, page: 1, pageSize: 20 })
    list.value = res.records || []
  } catch (e) { list.value = [] }
  finally { loading.value = false }
}

const getImage = (img) => img ? (img.includes(',') ? img.split(',')[0] : img) : '/static/default-product.png'
const goDetail = (id) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
</script>

<style lang="scss" scoped>
.page-search { background: #fff; min-height: 100vh; }
.search-header {
  display: flex; align-items: center; padding: 16rpx 20rpx; background: #fff;
  border-bottom: 1rpx solid #eee;
  .search-input { flex: 1; height: 68rpx; background: #f5f5f5; border-radius: 34rpx; padding: 0 30rpx; font-size: 26rpx; }
  .btn-search { width: 120rpx; height: 68rpx; line-height: 68rpx; background: #FF6034; color: #fff; font-size: 26rpx; border-radius: 34rpx; text-align: center; margin-left: 16rpx; }
}
.product-item { display: flex; padding: 20rpx; border-bottom: 1rpx solid #f5f5f5; }
.item-img { width: 160rpx; height: 160rpx; border-radius: 12rpx; margin-right: 20rpx; }
.item-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.item-name { font-size: 28rpx; color: #333; }
.price { color: #FF4444; font-size: 34rpx; font-weight: bold; }
.hot-search { padding: 30rpx; }
.hot-title { font-size: 28rpx; color: #999; }
.hot-tags { display: flex; flex-wrap: wrap; margin-top: 20rpx; }
.tag { padding: 10rpx 24rpx; background: #f5f5f5; border-radius: 30rpx; font-size: 24rpx; color: #666; margin: 0 16rpx 16rpx 0; }
</style>
