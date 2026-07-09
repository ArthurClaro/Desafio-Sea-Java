import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { usuario, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  function sair() {
    logout()
    navigate('/login')
  }

  return (
    <div className="app">
      <header className="topbar">
        <Link to="/" className="topbar__brand">
          SEA <span>· Gestão de Clientes</span>
        </Link>
        {usuario && (
          <div className="topbar__user">
            <span className={`badge ${isAdmin ? 'badge--admin' : 'badge--user'}`}>
              {isAdmin ? 'Administrador' : 'Visualização'}
            </span>
            <span className="topbar__username">{usuario.username}</span>
            <button type="button" className="btn btn--ghost" onClick={sair}>
              Sair
            </button>
          </div>
        )}
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
    </div>
  )
}
