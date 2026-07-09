import { createContext, useContext, useState } from 'react'
import http from '../api/http'

const AuthContext = createContext(null)

function usuarioSalvo() {
  try {
    return JSON.parse(localStorage.getItem('usuario'))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(usuarioSalvo)

  async function login(username, password) {
    const { data } = await http.post('/auth/login', { username, password })
    localStorage.setItem('token', data.token)
    const logado = { username: data.username, role: data.role }
    localStorage.setItem('usuario', JSON.stringify(logado))
    setUsuario(logado)
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('usuario')
    setUsuario(null)
  }

  const value = {
    usuario,
    isAdmin: usuario?.role === 'ADMIN',
    autenticado: Boolean(usuario && localStorage.getItem('token')),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
