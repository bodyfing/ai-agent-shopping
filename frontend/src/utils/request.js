import axios from 'axios'
import router from '@/router'
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8084',
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 统一后端返回格式
    if (res.code !== 200) {
      // token 失效
      if (res.code === 401) {
        localStorage.removeItem('token')
      }
      return Promise.reject(res.msg || 'Error')
    }
    return res.data
  },
  error => {
    console.error('请求错误：', error)
    alert("请登录！");
    router.push('/login')
    return Promise.reject(error)
  }
)

export default request