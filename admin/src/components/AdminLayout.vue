<template>
  <el-container class="admin-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-text">B2C 商城管理</span>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9" active-text-color="#FF6034">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon><span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Grid /></el-icon><span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/banners">
          <el-icon><Picture /></el-icon><span>轮播图管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="header-title">{{ $route.meta.title }}</span>
        </div>
        <div class="header-right">
          <span class="admin-name">{{ adminStore.adminInfo?.nickname || '管理员' }}</span>
          <el-button type="danger" size="small" @click="handleLogout" plain>退出</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/store/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const activeMenu = computed(() => route.path)

const handleLogout = () => {
  adminStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-container { height: 100vh; }
.sidebar { background: #304156; overflow-y: auto; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-text { color: #fff; font-size: 18px; font-weight: bold; }
.sidebar .el-menu { border-right: none; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e8e8e8; box-shadow: 0 1px 4px rgba(0,0,0,0.05); padding: 0 24px; }
.header-title { font-size: 18px; font-weight: bold; color: #333; }
.header-right { display: flex; align-items: center; gap: 12px; }
.admin-name { color: #666; font-size: 14px; }
.main-content { background: #f0f2f5; min-height: calc(100vh - 60px); padding: 20px; }
</style>
