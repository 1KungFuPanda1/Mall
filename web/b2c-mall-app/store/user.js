// ============================================
// B2C 移动端商城 — 用户状态管理 Store（Pinia）
// 管理：登录状态、用户信息、Token、登录/退出操作
// ============================================
import { defineStore } from 'pinia' // Pinia 创建 Store 的方法
import { login as loginApi, getUserInfo } from '@/services/index.js' // 导入 API 函数

export const useUserStore = defineStore('user', {
  // ==================== 状态（响应式数据） ====================
  state: () => ({
    token: uni.getStorageSync('token') || '', // 从本地缓存恢复 Token（应用重启后保持登录状态）
    userInfo: uni.getStorageSync('userInfo') ? JSON.parse(uni.getStorageSync('userInfo')) : null // 用户信息
  }),

  // ==================== 计算属性（派生状态） ====================
  getters: {
    /** 是否已登录（Token 不为空即为已登录） */
    isLogin: (state) => !!state.token, // !! 将字符串转为布尔值

    /** 用户 ID */
    userId: (state) => state.userInfo ? state.userInfo.userId : null, // 从用户信息中提取

    /** 用户手机号 */
    phone: (state) => state.userInfo ? state.userInfo.phone : '', // 手机号脱敏显示用

    /** 用户昵称 */
    nickname: (state) => state.userInfo ? state.userInfo.nickname : '未登录' // 个人中心显示用

    /** 购物车商品总数 */
    // cartCount 单独在 cart store 中管理
  },

  // ==================== 操作方法（异步业务逻辑） ====================
  actions: {
    /**
     * 用户登录
     * 调用登录 API → 保存 Token 和用户信息到本地缓存 → 更新 Store 状态
     *
     * @param {Object} loginData - 登录表单数据 { account: '手机号', password: '密码' }
     * @returns {Promise} 登录结果
     */
    async loginAction(loginData) {
      try {
        // 调用后端登录接口，返回 { token, userId, phone, nickname, role }
        const res = await loginApi(loginData) // 发起登录请求

        // 保存 Token 到本地缓存（应用关闭后仍保留）
        uni.setStorageSync('token', res.token) // 持久化 Token
        this.token = res.token // 更新 Store 中的 Token

        // 保存用户信息
        const userInfo = {
          userId: res.userId, // 用户ID
          phone: res.phone, // 手机号
          nickname: res.nickname, // 昵称
          role: res.role // 角色
        }
        uni.setStorageSync('userInfo', JSON.stringify(userInfo)) // 持久化用户信息
        this.userInfo = userInfo // 更新 Store 中的用户信息

        return res // 返回登录响应（调用方可能需要角色等信息）
      } catch (error) {
        // 登录失败，清除可能存在的旧数据
        this.clearUserData() // 清除登录数据
        throw error // 向上抛出异常，让页面层处理提示
      }
    },

    /**
     * 获取用户信息（从后端刷新）
     * 用于：登录后获取完整用户信息、个人中心刷新
     */
    async fetchUserInfo() {
      try {
        const res = await getUserInfo() // 调用后端获取当前用户信息
        const userInfo = {
          userId: res.id, // 注意后端返回的是 id 不是 userId
          phone: res.phone,
          nickname: res.nickname,
          avatar: res.avatar,
          role: res.role
        }
        uni.setStorageSync('userInfo', JSON.stringify(userInfo)) // 更新缓存
        this.userInfo = userInfo // 更新 Store
      } catch (error) {
        console.error('获取用户信息失败', error) // 记录错误
      }
    },

    /**
     * 退出登录
     * 清除 Token、用户信息、本地缓存
     */
    logout() {
      this.clearUserData() // 清除数据
      // 跳转到首页（而非登录页，允许游客浏览）
      uni.switchTab({ url: '/pages/index/index' }) // 切换到首页 Tab
    },

    /**
     * 清除用户登录数据（内部方法）
     * 清除 Store 状态 + 本地缓存
     */
    clearUserData() {
      this.token = '' // 清空 Store 中的 Token
      this.userInfo = null // 清空 Store 中的用户信息
      uni.removeStorageSync('token') // 清除本地缓存 Token
      uni.removeStorageSync('userInfo') // 清除本地缓存用户信息
    }
  }
})
