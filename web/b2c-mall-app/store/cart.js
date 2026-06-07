// ============================================
// B2C 移动端商城 — 购物车状态管理 Store（Pinia）
// 管理：购物车商品列表、数量、选中状态、总价
// ============================================
import { defineStore } from 'pinia'
import {
  getCartList, addToCart, updateCartQuantity,
  removeFromCart, checkCartItem
} from '@/services/index.js'

export const useCartStore = defineStore('cart', {
  // ==================== 状态 ====================
  state: () => ({
    cartList: [], // 购物车商品列表 { productId, name, image, price, quantity, checked }
    loading: false // 加载状态
  }),

  // ==================== 计算属性 ====================
  getters: {
    /** 购物车商品总数 */
    cartCount: (state) => state.cartList.reduce((sum, item) => sum + item.quantity, 0),

    /** 已选中的商品列表 */
    checkedItems: (state) => state.cartList.filter(item => item.checked),

    /** 已选中商品总数 */
    checkedCount: (state) => state.cartList.filter(item => item.checked).length,

    /** 全选状态 */
    isAllChecked: (state) => {
      if (state.cartList.length === 0) return false
      return state.cartList.every(item => item.checked)
    },

    /** 已选商品总金额 */
    totalPrice: (state) => {
      return state.cartList
        .filter(item => item.checked)
        .reduce((sum, item) => sum + (item.price * item.quantity), 0)
        .toFixed(2)
    }
  },

  // ==================== 操作方法 ====================
  actions: {
    /** 获取购物车列表 */
    async fetchCartList() {
      this.loading = true
      try {
        const list = await getCartList()
        this.cartList = list || []
      } catch (e) {
        console.error('获取购物车失败', e)
      } finally {
        this.loading = false
      }
    },

    /** 添加商品到购物车 */
    async addAction(productId) {
      await addToCart(productId)
      await this.fetchCartList() // 刷新列表
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    },

    /** 修改数量 */
    async updateQuantityAction(productId, quantity) {
      if (quantity < 1) return // 最小为1
      await updateCartQuantity(productId, quantity)
      await this.fetchCartList()
    },

    /** 删除商品 */
    async removeAction(productId) {
      await removeFromCart(productId)
      await this.fetchCartList()
      uni.showToast({ title: '已删除', icon: 'success' })
    },

    /** 切换选中状态 — 只调接口不刷新列表（由组件层乐观更新UI） */
    async toggleCheckAction(productId, checked) {
      await checkCartItem(productId, checked)
    },

    /** 全选/取消全选 — 只调接口不刷新列表 */
    async toggleAllCheckAction(checked) {
      const promises = this.cartList.map(item => {
        if (item.checked !== checked) return checkCartItem(item.productId, checked)
      })
      await Promise.all(promises.filter(Boolean))
    }
  }
})
