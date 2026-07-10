import { createContext, useContext, useState } from 'react'
import http from '../api/http'

const AuthContext = createContext(null)

// Decodifica o payload do JWT sem validar assinatura (a validação real é do backend);
// serve apenas para não tratar como logado um token que já expirou.
function tokenValido() {
  const token = localStorage.getItem('token')
  if (!token) return false
  try {
    const { exp } = JSON.parse(atob(token.split('.')[1]))
    return typeof exp === 'number' && exp * 1000 > Date.now()
  } catch {
    return false
  }
}

function usuarioSalvo() {
  if (!tokenValido()) return null
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
    autenticado: Boolean(usuario) && tokenValido(),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
