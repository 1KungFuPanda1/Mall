import { defineStore } from 'pinia'
import { adminLogin as loginApi } from '@/api/admin'
import { ref } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref(null)

  const isLogin = () => !!token.value

  const login = async (account, password) => {
    const res = await loginApi({ account, password })
    token.value = res.token
    localStorage.setItem('admin_token', res.token)
    adminInfo.value = {
      userId: res.userId,
      phone: res.phone,
      nickname: res.nickname,
      role: res.role
    }
    return res
  }

  const fetchAdminInfo = () => {
    if (!token.value) return
    adminInfo.value = { nickname: '管理员' }
  }

  const logout = () => {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
  }

  return { token, adminInfo, isLogin, login, fetchAdminInfo, logout }
})
