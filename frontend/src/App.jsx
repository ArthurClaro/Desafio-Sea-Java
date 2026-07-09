import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import ClientesLista from './pages/ClientesLista'
import ClienteForm from './pages/ClienteForm'

function Privada({ children, somenteAdmin = false }) {
  const { autenticado, isAdmin } = useAuth()
  if (!autenticado) return <Navigate to="/login" replace />
  if (somenteAdmin && !isAdmin) return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<Layout />}>
        <Route
          path="/"
          element={
            <Privada>
              <ClientesLista />
            </Privada>
          }
        />
        <Route
          path="/clientes/novo"
          element={
            <Privada somenteAdmin>
              <ClienteForm />
            </Privada>
          }
        />
        <Route
          path="/clientes/:id/editar"
          element={
            <Privada somenteAdmin>
              <ClienteForm />
            </Privada>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
