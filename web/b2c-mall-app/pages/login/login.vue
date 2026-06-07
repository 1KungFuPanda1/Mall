<template>
  <!-- ================================================
  登录页 — 手机号/邮箱 + 密码登录
  ================================================ -->
  <view class="page-login">
    <!-- Logo 区域 -->
    <view class="logo-area">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">B2C 移动商城</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-card">
      <!-- 账号输入 -->
      <view class="input-group">
        <u-icon name="account" size="22" color="#999" />
        <input class="input" v-model="form.account" placeholder="请输入手机号/邮箱"
               placeholder-style="color:#ccc" maxlength="50" />
      </view>

      <!-- 密码输入 -->
      <view class="input-group">
        <u-icon name="lock" size="22" color="#999" />
        <input class="input" v-model="form.password" type="password"
               placeholder="请输入密码" placeholder-style="color:#ccc" maxlength="20" />
      </view>

      <!-- 登录按钮 -->
      <button class="btn-login" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </button>
    </view>

    <!-- 底部注册入口 -->
    <view class="bottom-link">
      <text>还没有账号？</text>
      <text class="link" @click="goRegister">立即注册</text>
    </view>
  </view>
</template>

<script setup>
// ================================================
// 登录页逻辑
// ================================================
import { ref, reactive } from 'vue'
import { useUserStore } from '@/store/user.js' // 导入用户 Store

const userStore = useUserStore() // 获取用户 Store 实例
const loading = ref(false) // 加载状态

// 登录表单数据
const form = reactive({
  account: '', // 手机号或邮箱
  password: '' // 密码
})

/** 执行登录 */
const handleLogin = async () => {
  // 表单校验
  if (!form.account) return uni.showToast({ title: '请输入账号', icon: 'none' })
  if (!form.password) return uni.showToast({ title: '请输入密码', icon: 'none' })

  loading.value = true // 显示加载状态
  try {
    // 调用 Store 中的登录方法（内部调用后端 API + 保存 Token）
    await userStore.loginAction({ account: form.account, password: form.password })
    uni.showToast({ title: '登录成功', icon: 'success' })
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' }) // 跳转首页
    }, 500)
  } catch (e) {
    // 显示具体错误信息（密码错误 / 账号不存在 / 等）
    const msg = (e && e.message) || '登录失败，请重试'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    loading.value = false // 恢复按钮状态
  }
}

/** 跳转注册页 */
const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/register' })
}
</script>

<style lang="scss" scoped>
.page-login {
  min-height: 100vh; background: linear-gradient(180deg, #FF6034 0%, #FFF 40%);
  display: flex; flex-direction: column; align-items: center; padding-top: 120rpx;
}
.logo-area { display: flex; flex-direction: column; align-items: center; margin-bottom: 60rpx; }
.logo { width: 120rpx; height: 120rpx; border-radius: 24rpx; }
.app-name { font-size: 36rpx; color: #fff; font-weight: bold; margin-top: 16rpx; }
.form-card {
  width: 90%; background: #fff; border-radius: 16rpx; padding: 40rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}
.input-group {
  display: flex; align-items: center; border-bottom: 1rpx solid #eee;
  padding: 24rpx 0; margin-bottom: 16rpx;
  .input { flex: 1; margin-left: 16rpx; font-size: 28rpx; }
}
.btn-login {
  width: 100%; height: 88rpx; line-height: 88rpx; background: linear-gradient(135deg, #FF6034, #FF8A6A);
  color: #fff; font-size: 32rpx; border-radius: 44rpx; margin-top: 40rpx; text-align: center;
}
.bottom-link { margin-top: 40rpx; font-size: 28rpx; color: #666; }
.link { color: #FF6034; margin-left: 10rpx; }
</style>
