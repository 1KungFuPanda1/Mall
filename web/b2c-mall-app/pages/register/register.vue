<template>
  <!-- ================================================
  注册页 — 手机号 + 验证码 + 密码注册
  ================================================ -->
  <view class="page-register">
    <view class="form-card">
      <!-- 手机号输入 -->
      <view class="input-group">
        <u-icon name="phone" size="22" color="#999" />
        <input class="input" v-model="form.phone" placeholder="请输入手机号"
               type="number" maxlength="11" placeholder-style="color:#ccc" />
      </view>

      <!-- 验证码输入（带发送按钮） -->
      <view class="input-group">
        <u-icon name="chat" size="22" color="#999" />
        <input class="input" v-model="form.smsCode" placeholder="请输入验证码"
               type="number" maxlength="6" placeholder-style="color:#ccc" />
        <button class="btn-sms" :disabled="smsCountdown > 0" @click="sendSmsCode">
          {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
        </button>
      </view>

      <!-- 密码输入 -->
      <view class="input-group">
        <u-icon name="lock" size="22" color="#999" />
        <input class="input" v-model="form.password" type="password"
               placeholder="请设置密码（6-20位）" maxlength="20" placeholder-style="color:#ccc" />
      </view>

      <!-- 注册按钮 -->
      <button class="btn-register" :disabled="loading" @click="handleRegister">
        {{ loading ? '注册中...' : '注 册' }}
      </button>
    </view>

    <!-- 底部登录入口 -->
    <view class="bottom-link">
      <text>已有账号？</text>
      <text class="link" @click="goLogin">去登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onMounted } from '@dcloudio/uni-app'
import { sendSms, register } from '@/services/index.js'
import { useUserStore } from '@/store/user.js'

const userStore = useUserStore()
const loading = ref(false)
const loginLoading = ref(false)
const smsCountdown = ref(0)

const form = reactive({
  phone: '',
  smsCode: '',
  password: ''
})

// 已登录用户不应进入注册页，自动跳转到首页
onMounted(() => {
  if (userStore.isLogin) {
    uni.showToast({ title: '您已登录', icon: 'none' })
    setTimeout(() => { uni.switchTab({ url: '/pages/index/index' }) }, 500)
  }
})

/** 发送短信验证码 */
const sendSmsCode = async () => {
  if (!form.phone) return uni.showToast({ title: '请输入手机号', icon: 'none' })
  if (form.phone.length !== 11) return uni.showToast({ title: '手机号格式不正确', icon: 'none' })

  try {
    await sendSms({ phone: form.phone })
    uni.showToast({ title: '验证码已发送', icon: 'success' })

    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) { /* 错误已由拦截器处理 */ }
}

/** 执行注册 */
const handleRegister = async () => {
  if (!form.phone) return uni.showToast({ title: '请输入手机号', icon: 'none' })
  if (!form.smsCode) return uni.showToast({ title: '请输入验证码', icon: 'none' })
  if (!form.password || form.password.length < 6) return uni.showToast({ title: '密码至少6位', icon: 'none' })

  loading.value = true
  try {
    await register({ phone: form.phone, smsCode: form.smsCode, password: form.password })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    setTimeout(() => { uni.redirectTo({ url: '/pages/login/login' }) }, 1000)
  } catch (e) { /* 错误已处理 */ }
  finally { loading.value = false }
}

/** 跳转登录页 — 使用 redirectTo 确保独立栈，不依赖 navigateBack */
const goLogin = () => {
  loginLoading.value = true
  uni.showLoading({ title: '跳转中...', mask: true })
  // 使用 redirectTo 替换当前页面栈，确保跳转可靠
  setTimeout(() => {
    uni.redirectTo({ url: '/pages/login/login' })
  }, 150)
}
</script>

<style lang="scss" scoped>
.page-register {
  min-height: 100vh; background: linear-gradient(180deg, #FF6034 0%, #FFF 40%);
  display: flex; flex-direction: column; align-items: center; padding-top: 120rpx;
}
.form-card {
  width: 90%; background: #fff; border-radius: 16rpx; padding: 40rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}
.input-group {
  display: flex; align-items: center; border-bottom: 1rpx solid #eee;
  padding: 24rpx 0; margin-bottom: 16rpx;
  .input { flex: 1; margin-left: 16rpx; font-size: 28rpx; }
}
.btn-sms {
  width: 180rpx; height: 60rpx; line-height: 60rpx; background: #FF6034;
  color: #fff; font-size: 24rpx; border-radius: 30rpx; text-align: center; flex-shrink: 0;
}
.btn-register {
  width: 100%; height: 88rpx; line-height: 88rpx;
  background: linear-gradient(135deg, #FF6034, #FF8A6A);
  color: #fff; font-size: 32rpx; border-radius: 44rpx; margin-top: 40rpx; text-align: center;
}
.bottom-link { margin-top: 40rpx; font-size: 28rpx; color: #666; }
.link { color: #FF6034; margin-left: 10rpx; }
</style>
