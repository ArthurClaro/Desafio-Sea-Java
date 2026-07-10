import axios from 'axios'

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const rotaLogin = window.location.pathname === '/login'
    if (status === 401 && !rotaLogin) {
      localStorage.removeItem('token')
      localStorage.removeItem('usuario')
      // Reload completo proposital: descarta qualquer estado em memória de um
      // usuário cuja sessão expirou, em vez de navegar via router
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default http
