<template>
  <router-view />
</template>

<script setup>
import { useAdminStore } from '@/store/admin'
import { onMounted } from 'vue'

const adminStore = useAdminStore()

onMounted(() => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    adminStore.token = token
    adminStore.fetchAdminInfo()
  }
})
</script>

<style>
body { margin: 0; padding: 0; }
#app { font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif; }
</style>
