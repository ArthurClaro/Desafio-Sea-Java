import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  async function enviar(e) {
    e.preventDefault()
    setErro('')
    setCarregando(true)
    try {
      await login(username, password)
      navigate('/')
    } catch (err) {
      setErro(err.response?.data?.mensagem || 'Falha ao autenticar. Tente novamente.')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="login">
      <form className="card login__card" onSubmit={enviar}>
        <h1 className="login__titulo">
          SEA <span>· Gestão de Clientes</span>
        </h1>
        <p className="login__subtitulo">Entre com sua conta para acessar o sistema</p>

        {erro && <div className="alerta alerta--erro" role="alert">{erro}</div>}

        <label className="campo">
          <span>Usuário</span>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="admin ou user"
            autoFocus
            required
          />
        </label>

        <label className="campo">
          <span>Senha</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Sua senha"
            required
          />
        </label>

        <button type="submit" className="btn btn--primario btn--bloco" disabled={carregando}>
          {carregando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </div>
  )
}
