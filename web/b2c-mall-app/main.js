// ============================================
// main.js — 应用入口文件
// 负责：创建Vue应用 → 注册Pinia状态管理 → 注册uView Plus组件库 → 挂载应用
// ============================================
import { createSSRApp } from 'vue' // Vue 3 的 SSR 兼容创建方法（uni-app 要求）
import App from './App.vue' // 根组件
import * as Pinia from 'pinia' // Pinia 状态管理库
import uviewPlus from 'uview-plus' // uView Plus UI 组件库

/**
 * 导出 createApp 函数
 * uni-app 框架会调用此函数创建 Vue 应用实例
 * 使用 createSSRApp 而非 createApp 是为了兼容 SSR 场景
 */
export function createApp() {
  // 步骤1：创建 Vue 应用实例
  const app = createSSRApp(App) // 基于根组件创建 SSR 应用

  // 步骤2：注册 Pinia 状态管理
  // Pinia 用于全局状态管理（登录用户信息、购物车计数等）
  app.use(Pinia.createPinia()) // 安装 Pinia 插件

  // 步骤3：注册 uView Plus UI 组件库
  // uView Plus 提供丰富的移动端 UI 组件（按钮、输入框、轮播图等）
  app.use(uviewPlus) // 安装 uView Plus

  // 步骤4：返回应用实例
  return { app } // 返回对象供 uni-app 框架接管
}
