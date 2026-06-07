<template>
  <!-- ================================================
  收货地址列表页 — 管理收货地址
  ================================================ -->
  <view class="page-address">
    <!-- 加载中 -->
    <view class="status-box" v-if="loading">
      <u-loading-icon size="40" text="加载中..." />
    </view>

    <!-- 未登录 / 登录过期（401） -->
    <view class="status-box" v-else-if="authError">
      <u-icon name="account" size="60" color="#ccc" />
      <text class="status-text">登录已过期</text>
      <text class="status-sub">请重新登录后查看收货地址</text>
      <button class="btn-go-login" @click="goLogin">去登录</button>
    </view>

    <!-- 网络错误 / 其他异常（含手动重试） -->
    <view class="status-box" v-else-if="hasError">
      <u-icon name="wifi-off" size="60" color="#ccc" />
      <text class="status-text">加载失败</text>
      <text class="status-sub">{{ errorMsg || '请检查网络连接或稍后重试' }}</text>
      <button class="btn-retry" @click="loadAddressList">重新加载</button>
    </view>

    <!-- 正常地址列表 -->
    <template v-else>
      <!-- 地址卡片列表 -->
      <view class="address-item" v-for="addr in addressList" :key="addr.id"
            @click="selectAddress(addr)">
        <view class="addr-top">
          <text class="addr-name">{{ addr.receiverName }}</text>
          <text class="addr-phone">{{ addr.receiverPhone }}</text>
          <text class="addr-default" v-if="addr.isDefault === 1">默认</text>
        </view>
        <text class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</text>
        <view class="addr-actions">
          <text class="action-text" @click.stop="goEdit(addr.id)">编辑</text>
          <text class="action-text delete" @click.stop="handleDelete(addr.id)">删除</text>
        </view>
      </view>

      <!-- 空状态 -->
      <u-empty v-if="addressList.length === 0" text="暂无收货地址" mode="address" />
    </template>

    <!-- 底部添加按钮 -->
    <view class="bottom-btn safe-bottom" v-if="!hasError && !authError && isLogin">
      <button class="btn-add" @click="goEdit()">
        <u-icon name="plus" size="20" color="#fff" />
        <text>新增收货地址</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAddressList, deleteAddress } from '@/services/index.js'
import { useUserStore } from '@/store/user.js'

const userStore = useUserStore()
const addressList = ref([])
const loading = ref(true)
const hasError = ref(false)
const authError = ref(false)
const errorMsg = ref('')
let isSelect = false

const isLogin = computed(() => userStore.isLogin)

onShow(() => {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  if (current && current.options) {
    isSelect = current.options.select === 'true'
  }

  if (isLogin.value) {
    loadAddressList()
  } else {
    loading.value = false
    authError.value = true
  }
})

/** 加载地址列表（区分认证错误和网络错误） */
const loadAddressList = async () => {
  loading.value = true
  hasError.value = false
  authError.value = false
  errorMsg.value = ''
  try {
    const data = await getAddressList()
    addressList.value = Array.isArray(data) ? data : []
  } catch (e) {
    addressList.value = []
    if (e && e.statusCode === 401) {
      // 认证失败 — 显示登录提示
      authError.value = true
    } else {
      // 网络/其他错误 — 显示重试提示
      hasError.value = true
      errorMsg.value = e.message || '加载失败'
    }
  } finally {
    loading.value = false
  }
}

/** 选择地址（订单确认页回传） */
const selectAddress = (addr) => {
  if (!isSelect) return
  const pages = getCurrentPages()
  const prevPage = pages[pages.length - 2]
  if (prevPage) {
    prevPage.$vm.selectedAddress = addr
  }
  uni.navigateBack()
}

const goEdit = (id) => {
  const url = id ? `/pages/address/edit?id=${id}` : '/pages/address/edit'
  uni.navigateTo({ url })
}

/** 删除地址 */
const handleDelete = (id) => {
  uni.showModal({
    title: '提示', content: '确定删除该地址？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteAddress(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadAddressList()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const goLogin = () => uni.reLaunch({ url: '/pages/login/login' })
</script>

<style lang="scss" scoped>
.page-address { min-height: 100vh; background: #f5f5f5; padding-bottom: 120rpx; }

/* 加载态 / 错误态 居中容器 */
.status-box { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
.status-text { font-size: 30rpx; color: #666; margin-top: 20rpx; }
.status-sub { font-size: 24rpx; color: #999; margin-top: 10rpx; }
.btn-retry {
  width: 240rpx; height: 72rpx; line-height: 72rpx;
  margin-top: 30rpx; background: linear-gradient(135deg, #FF6034, #FF8A6A);
  color: #fff; font-size: 26rpx; border-radius: 36rpx; text-align: center;
}
.btn-go-login {
  width: 240rpx; height: 72rpx; line-height: 72rpx;
  background: #FF6034; color: #fff; border-radius: 36rpx;
  font-size: 26rpx; text-align: center; margin-top: 20rpx;
}

.address-item {
  background: #fff; margin: 12rpx 20rpx; padding: 24rpx; border-radius: 12rpx;
  .addr-top { display: flex; align-items: center; margin-bottom: 12rpx; }
  .addr-name { font-size: 30rpx; color: #333; font-weight: bold; margin-right: 16rpx; }
  .addr-phone { font-size: 26rpx; color: #666; flex: 1; }
  .addr-default { font-size: 20rpx; color: #FF6034; background: #FFF4F0; padding: 2rpx 12rpx; border-radius: 8rpx; }
  .addr-detail { font-size: 26rpx; color: #666; display: block; }
  .addr-actions { display: flex; justify-content: flex-end; gap: 30rpx; margin-top: 16rpx; }
  .action-text { font-size: 24rpx; color: #FF6034; }
  .delete { color: #999; }
}

.bottom-btn {
  position: fixed; bottom: 0; left: 0; right: 0; padding: 16rpx 24rpx; background: #fff;
  .btn-add {
    width: 100%; height: 88rpx; line-height: 88rpx;
    background: linear-gradient(135deg, #FF6034, #FF8A6A);
    color: #fff; font-size: 32rpx; border-radius: 44rpx;
    text-align: center; display: flex; align-items: center; justify-content: center; gap: 10rpx;
  }
}
</style>
