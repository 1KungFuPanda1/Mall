<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">B2C 商城后台管理</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="请输入管理员账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/store/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ account: '', password: '' })
const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await adminStore.login(form.account, form.password)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page { height: 100vh; background: linear-gradient(135deg, #303133 0%, #409EFF 100%); display: flex; align-items: center; justify-content: center; }
.login-card { width: 420px; background: #fff; border-radius: 8px; padding: 48px 40px; box-shadow: 0 8px 40px rgba(0,0,0,0.15); }
.login-title { text-align: center; margin-bottom: 36px; color: #303133; font-size: 22px; }
.login-btn { width: 100%; }
</style>
