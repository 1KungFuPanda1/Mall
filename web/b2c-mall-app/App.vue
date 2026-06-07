<script>
// ============================================
// App.vue — 应用根组件
// 全局生命周期钩子，用于应用初始化
// ============================================
export default {
  onLaunch() {
    // 应用启动时执行
    console.log('B2C商城应用启动') // 调试日志
    // 检查登录状态：从本地缓存读取 token，恢复登录态
    const token = uni.getStorageSync('token') // 从本地存储读取 JWT Token
    if (token) {
      // 如果有缓存的 Token，说明用户之前登录过
      console.log('检测到已登录用户') // 调试日志
    }
  },
  onShow() {
    // 应用从后台切换到前台时执行
    console.log('应用显示') // 调试日志
  },
  onHide() {
    // 应用从前台切换到后台时执行
    console.log('应用隐藏') // 调试日志
  }
}
</script>

<style>
/* ============================================
   全局样式 — 直接使用 CSS 定义（避免 sass 依赖问题）
   主题色 #FF6034 / 价格色 #FF4444 / 背景色 #F5F5F5
   ============================================ */

/* 页面全局背景和字体 */
page {
  background-color: #F5F5F5;
  font-size: 28rpx;
  color: #333333;
}

/* 全局重置样式 */
view, text, image, input, button {
  box-sizing: border-box;
}

/* 按钮去除默认样式 */
button {
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  line-height: inherit;
}
button::after {
  border: none;
}

/* 输入框全局样式 */
input {
  font-size: 28rpx;
  color: #333333;
}

/* 单行省略号 */
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 两行省略号 */
.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 价格红色样式 */
.price {
  color: #FF4444;
  font-weight: bold;
  font-size: 32rpx;
}

/* 安全区域底部适配 */
.safe-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
