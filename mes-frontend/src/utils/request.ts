import axios, { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

// 请求拦截：注入 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('mes-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理业务码与 HTTP 错误
request.interceptors.response.use(
  (response) => {
    // 文件下载（blob）直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res && res.code === 200) {
      return res.data
    }
    if (res && res.code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error(res.msg || '未认证'))
    }
    ElMessage.error(res?.msg || '请求失败')
    return Promise.reject(new Error(res?.msg || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      handleUnauthorized()
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

function handleUnauthorized() {
  localStorage.removeItem('mes-token')
  if (router.currentRoute.value.path !== '/login') {
    ElMessage.warning('登录已过期，请重新登录')
    router.push('/login')
  }
}

export function get<T = any>(url: string, params?: any): Promise<T> {
  return request.get(url, { params }) as unknown as Promise<T>
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request.post(url, data, config) as unknown as Promise<T>
}

export function put<T = any>(url: string, data?: any): Promise<T> {
  return request.put(url, data) as unknown as Promise<T>
}

export function del<T = any>(url: string): Promise<T> {
  return request.delete(url) as unknown as Promise<T>
}

export default request
