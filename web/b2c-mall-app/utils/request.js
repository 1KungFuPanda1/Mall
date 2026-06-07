// ============================================
// B2C 移动端商城 — API 请求封装
//
// 核心功能：处理 Snowflake ID（19位）精度问题
// JavaScript 安全整数上限：9007199254740991（约16位）
// 后端 Snowflake ID 通常为 18-19 位，超出 JS 安全范围
//
// 解决方案（H5模式）：
//   1. 用 fetch 获取原始响应文本（response.text()）
//   2. 在 JSON.parse 前用正则将大数字替换为字符串
//      这样可以保留完整的原始数值精度
//
// 小程序模式：使用 uni.request（保持原有逻辑）
// ============================================
const BASE_URL = ''

/**
 * 在原始 JSON 文本中，将所有超过安全整数上限的数字转为字符串
 * 这必须在 JSON.parse() 之前执行，否则精度已经丢失
 *
 * 示例：
 *   输入: '{"id":2062127568859062300,"name":"test"}'
 *   输出: '{"id":"2062127568859062300","name":"test"}'
 */
/**
 * 修复后端返回的非法 JSON 字段值
 * 问题：后端某些字段（如 addressJson）内部包含未转义的 JSON 字符串
 * 例如："addressJson":"{"id":123,"city":"xx"}"  → 外层和内层引号冲突
 * 修复：将字段值内部的 " 替换为 \"
 */
function fixUnescapedJsonFields(text) {
  if (!text || typeof text !== 'string') return text
  // 匹配 "fieldName":"{...}" 模式，其中 {...} 内部包含未转义的引号
  // 策略：找到 "xxxJson":" 开头，然后定位到对应的结尾 "，对其中的内容做转义
  return text.replace(/"(addressJson|json|dataJson|extra)"\s*:\s*"((?:[^"\\]|\\.)*)"/g, (match, fieldName, fieldValue) => {
    // 如果字段值内部包含未转义的 { 或 "，说明是嵌套 JSON 需要转义
    if (fieldValue.includes('"') && (fieldValue.includes('{') || fieldValue.includes('['))) {
      const escaped = fieldValue.replace(/"/g, '\\"')
      return `"${fieldName}":"${escaped}"`
    }
    return match
  })
}

/**
 * 保护 Snowflake ID（19位大数字）不被 JavaScript 截断
 * 策略：只匹配已知 Snowflake ID 字段名后面的 15+ 位数字
 * 避免误伤 addressJson 等嵌套 JSON 字符串内部的数字
 */
function preserveBigNumbersInRawText(text) {
  if (!text || typeof text !== 'string') return text
  // 只保护已知 ID 字段：id, userId, orderId, addressId, productId, orderItemId 等
  // 匹配 "fieldName": 15位以上数字 的模式，将数字转为字符串
  return text.replace(
    /"(id|userId|orderId|addressId|productId|orderItemId|cartItemId)"\s*:\s*(\d{15,})([,\}\]\s]|$)/g,
    '"$1":"$2"$3'
  )
}

/** 是否运行在 H5/浏览器环境 */
const isH5 = typeof window !== 'undefined' && typeof document !== 'undefined'

/**
 * H5 模式请求 — 使用 fetch + 原始文本正则处理大数字
 */
const h5Request = (options) => {
  let url = BASE_URL + options.url
  const token = uni.getStorageSync('token')

  const headers = {
    'Content-Type': 'application/json',
    ...options.header
  }
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }

  // GET 请求参数拼接到 URL
  if ((!options.method || options.method.toUpperCase() === 'GET') && options.data) {
    const params = new URLSearchParams(options.data).toString()
    if (params) url = url + '?' + params
  }

  const fetchOptions = {
    method: options.method || 'GET',
    headers,
    credentials: 'include'
  }

  // 非 GET 请求带 body
  if (options.method && options.method.toUpperCase() !== 'GET' && options.data) {
    fetchOptions.body = typeof options.data === 'string'
      ? options.data
      : JSON.stringify(options.data)
  }

  return fetch(url, fetchOptions)
    .then(async (response) => {
      const status = response.status

      if (status >= 200 && status < 300) {
        // 获取原始响应文本
        const rawText = await response.text()
        console.log('[Request API]', options.url, '| 状态:', status, '| 原始长度:', rawText?.length)

        // 解析策略：按优先级依次尝试
        let json = null
        const tryParse = (text, label) => {
          try { return text ? JSON.parse(text) : null }
          catch(e) { console.warn(`[Request API] ${label} 失败:`, e.message?.substring(0, 80)); return null }
        }

        // 尝试1：保护 Snowflake ID 后解析（默认，覆盖大多数接口）
        json = tryParse(preserveBigNumbersInRawText(rawText), '尝试1-保护ID')

        // 尝试2：直接解析（某些接口不需要ID保护）
        if (!json) json = tryParse(rawText, '尝试2-直接解析')

        // 尝试3：修复未转义的嵌套JSON字段后 + 保护ID（兜底）
        if (!json) {
          console.warn('[Request API] 前两次解析失败，尝试修复嵌套JSON...')
          json = tryParse(preserveBigNumbersInRawText(fixUnescapedJsonFields(rawText)), '尝试3-修复嵌套')
        }

        if (!json) {
          const method = (options.method || 'GET').toUpperCase()
          console.error('[Request API] 所有解析方式均失败 |', method, options.url, '| 原始前300字:', rawText?.substring(0, 300))
          // GET 请求必须返回数据才能渲染页面，解析失败则抛异常
          if (method === 'GET') {
            throw new Error('服务器返回数据格式异常')
          }
          // PUT/POST/DELETE 写操作：HTTP 状态码已是 2xx 表示成功，无需响应体数据
          console.warn('[Request API] 写操作状态码正常，返回空对象')
          json = {}
        }
        // 自动解包后端统一响应格式 { code, data, message/msg }
        if (json && typeof json === 'object' && ('code' in json || 'msg' in json)) {
          const result = json.data !== undefined ? json.data : json
          console.log('[Request API] 解包后类型:', typeof result, '| keys:', result ? Object.keys(result).slice(0, 10) : null, '| 是否数组:', Array.isArray(result), '| data是否null:', json.data === null)
          return result
        }
        console.log('[Request API] 未匹配解包格式, 直接返回 | keys:', json ? Object.keys(json).slice(0, 10) : null)
        return json
      } else if (status === 401) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        let msg = '登录已过期'
        try { const b = await response.json(); msg = (b && b.message) || msg } catch(e) {}
        throw Object.assign(new Error(msg), { statusCode: 401 })
      } else {
        let msg = '请求失败'
        try { const b = await response.json(); msg = (b && b.message) || msg } catch(e) {}
        throw Object.assign(new Error(msg), { statusCode: status })
      }
    })
    .catch((err) => {
      if (err.statusCode) throw err
      throw new Error('网络连接失败')
    })
}

/**
 * 小程序模式请求 — 使用 uni.request
 */
const mpRequest = (options) => {
  return new Promise((resolve, reject) => {
    const url = BASE_URL + options.url
    const token = uni.getStorageSync('token')

    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    uni.request({
      url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      timeout: 15000,

      success: (res) => {
        const sc = res.statusCode
        if (sc >= 200 && sc < 300) {
          let result = res.data
          if (result && typeof result === 'object' && ('code' in result || 'msg' in result)) {
            result = result.data !== undefined ? result.data : result
          }
          resolve(result)
        } else if (sc === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          reject(Object.assign(new Error((res.data && res.data.message) || '登录已过期'), { statusCode: 401 }))
        } else {
          reject(Object.assign(new Error((res.data && res.data.message) || '请求失败'), { statusCode: sc }))
        }
      },
      fail: () => reject(new Error('网络连接失败'))
    })
  })
}

const request = isH5 ? h5Request : mpRequest

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

export default request
