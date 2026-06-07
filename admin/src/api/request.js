import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// ==================== Snowflake ID 精度保护 ====================
/** 匹配 JSON 中 id/userId/orderId 等字段的大数字值，转为字符串防止精度丢失 */
const BIG_ID_PATTERN = /"(id|userId|orderId|addressId|productId|orderItemId|cartItemId|categoryId)"\s*:\s*(\d{15,})/g

function preserveBigIds(jsonText) {
  if (typeof jsonText !== 'string') return jsonText
  return jsonText.replace(BIG_ID_PATTERN, '"$1": "$2"')
}

const service = axios.create({
  baseURL: '',
  timeout: 15000,
  // 替换默认 transformResponse：先保护大数字精度，再解析 JSON
  transformResponse: [(data) => {
    if (typeof data === 'string' && data) {
      try {
        const parsed = JSON.parse(preserveBigIds(data))
        // 验证：检查第一条记录的 id 类型
        if (parsed?.records?.[0]?.id) console.log('[ID-FIX] id:', parsed.records[0].id, 'type:', typeof parsed.records[0].id)
        return parsed
      } catch { return data }
    }
    return data
  }]
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (response.status === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('admin_token')
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('无权限访问')
      } else if (status === 400) {
        ElMessage.error(data.message || '请求参数错误')
      } else {
        ElMessage.error('服务器异常')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default service
