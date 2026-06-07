<template>
  <!-- ================================================
  个人中心页 — 用户信息 + 功能入口
  ================================================ -->
  <view class="page-user">
    <!-- 用户信息头部 -->
    <view class="user-header">
      <!-- 已登录 -->
      <template v-if="userStore.isLogin">
        <image class="avatar" :src="userStore.userInfo?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="user-info">
          <text class="nickname">{{ userStore.userInfo?.nickname || '用户' }}</text>
          <text class="phone">{{ maskPhone(userStore.userInfo?.phone || '') }}</text>
        </view>
      </template>
      <!-- 未登录 -->
      <template v-else>
        <image class="avatar" src="/static/default-avatar.png" mode="aspectFill" />
        <view class="user-info" @click="goLogin">
          <text class="nickname">点击登录</text>
          <text class="phone">登录后享受更多权益</text>
        </view>
      </template>
    </view>

    <!-- 我的订单入口 -->
    <view class="section-card">
      <view class="section-title">
        <text>我的订单</text>
        <text class="more" @click="goOrders('')">全部订单 ></text>
      </view>
      <view class="order-entries">
        <view class="entry" v-for="entry in orderEntries" :key="entry.key"
              @click="goOrders(entry.key)">
          <u-icon :name="entry.icon" size="28" color="#FF6034" />
          <text class="entry-text">{{ entry.label }}</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goAddress">
        <view class="menu-left">
          <u-icon name="map" size="22" color="#FF6034" />
          <text class="menu-text">收货地址</text>
        </view>
        <u-icon name="arrow-right" size="16" color="#ccc" />
      </view>
      <view class="menu-item" @click="handleLogout" v-if="userStore.isLogin">
        <view class="menu-left">
          <u-icon name="setting" size="22" color="#999" />
          <text class="menu-text" style="color:#999">退出登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { useUserStore } from '@/store/user.js'

const userStore = useUserStore()

// 订单入口配置
const orderEntries = [
  { key: 'WAIT_PAY', icon: 'clock', label: '待付款' },
  { key: 'RECEIVING', icon: 'car', label: '待收货' },
  { key: 'COMPLETED', icon: 'checkmark-circle', label: '已完成' }
]

/** 手机号脱敏（138****1234） */
const maskPhone = (phone) => {
  if (!phone || phone.length < 11) return phone
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

const goLogin = () => uni.navigateTo({ url: '/pages/login/login' })
const goOrders = (status) => {
  if (!userStore.isLogin) return uni.navigateTo({ url: '/pages/login/login' })
  uni.switchTab({ url: '/pages/order/list' })
}
const goAddress = () => {
  if (!userStore.isLogin) return uni.navigateTo({ url: '/pages/login/login' })
  uni.navigateTo({ url: '/pages/address/list' })
}
const handleLogout = () => {
  uni.showModal({
    title: '提示', content: '确定退出登录？',
    success: (res) => {
      if (res.confirm) userStore.logout()
    }
  })
}
</script>

<style lang="scss" scoped>
.page-user { min-height: 100vh; background: #f5f5f5; }

.user-header {
  display: flex; align-items: center; padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #FF6034, #FF8A6A);
  .avatar { width: 120rpx; height: 120rpx; border-radius: 60rpx; border: 4rpx solid rgba(255,255,255,0.3); margin-right: 24rpx; }
  .user-info { flex: 1; }
  .nickname { font-size: 36rpx; color: #fff; font-weight: bold; display: block; }
  .phone { font-size: 24rpx; color: rgba(255,255,255,0.7); margin-top: 8rpx; display: block; }
}

.section-card {
  background: #fff; margin: 16rpx 20rpx; border-radius: 12rpx; padding: 24rpx;
  .section-title { display: flex; justify-content: space-between; font-size: 28rpx; color: #333; font-weight: bold; }
  .more { font-size: 24rpx; color: #999; font-weight: normal; }
}
.order-entries { display: flex; margin-top: 24rpx; }
.entry { flex: 1; display: flex; flex-direction: column; align-items: center; }
.entry-text { font-size: 24rpx; color: #666; margin-top: 8rpx; }

.menu-card { background: #fff; margin: 16rpx 20rpx; border-radius: 12rpx; overflow: hidden; }
.menu-item {
  display: flex; align-items: center; justify-content: space-between; padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  &:last-child { border-bottom: none; }
  .menu-left { display: flex; align-items: center; }
  .menu-text { font-size: 28rpx; color: #333; margin-left: 16rpx; }
}
</style>
