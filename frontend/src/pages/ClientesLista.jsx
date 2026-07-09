import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import http from '../api/http'
import { useAuth } from '../auth/AuthContext'

export default function ClientesLista() {
  const { isAdmin } = useAuth()
  const [pagina, setPagina] = useState(null)
  const [numeroPagina, setNumeroPagina] = useState(0)
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(true)

  const carregar = useCallback(async (page) => {
    setCarregando(true)
    setErro('')
    try {
      const { data } = await http.get('/clientes', { params: { page, size: 10 } })
      setPagina(data)
    } catch {
      setErro('Não foi possível carregar os clientes.')
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregar(numeroPagina)
  }, [carregar, numeroPagina])

  async function excluir(cliente) {
    const confirma = window.confirm(`Excluir o cliente "${cliente.nome}"?`)
    if (!confirma) return
    try {
      await http.delete(`/clientes/${cliente.id}`)
      carregar(numeroPagina)
    } catch (err) {
      setErro(err.response?.data?.mensagem || 'Não foi possível excluir o cliente.')
    }
  }

  return (
    <section>
      <div className="pagina__cabecalho">
        <h2>Clientes</h2>
        {isAdmin && (
          <Link to="/clientes/novo" className="btn btn--primario">
            + Novo cliente
          </Link>
        )}
      </div>

      {erro && <div className="alerta alerta--erro">{erro}</div>}

      {carregando ? (
        <p className="texto-suave">Carregando…</p>
      ) : pagina && pagina.content.length === 0 ? (
        <div className="card vazio">
          <p>Nenhum cliente cadastrado ainda.</p>
          {isAdmin && <p className="texto-suave">Clique em “Novo cliente” para começar.</p>}
        </div>
      ) : (
        pagina && (
          <>
            <div className="card tabela-wrapper">
              <table className="tabela">
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>CPF</th>
                    <th>Cidade/UF</th>
                    <th>Telefones</th>
                    <th>E-mails</th>
                    {isAdmin && <th className="tabela__acoes">Ações</th>}
                  </tr>
                </thead>
                <tbody>
                  {pagina.content.map((cliente) => (
                    <tr key={cliente.id}>
                      <td>{cliente.nome}</td>
                      <td className="mono">{cliente.cpf}</td>
                      <td>
                        {cliente.endereco.cidade}/{cliente.endereco.uf}
                      </td>
                      <td className="mono">
                        {cliente.telefones.map((t) => (
                          <div key={`${t.tipo}-${t.numero}`}>
                            <span className="tag">{t.tipo.toLowerCase()}</span> {t.numero}
                          </div>
                        ))}
                      </td>
                      <td>
                        {cliente.emails.map((email) => (
                          <div key={email}>{email}</div>
                        ))}
                      </td>
                      {isAdmin && (
                        <td className="tabela__acoes">
                          <Link to={`/clientes/${cliente.id}/editar`} className="btn btn--pequeno">
                            Editar
                          </Link>
                          <button
                            type="button"
                            className="btn btn--pequeno btn--perigo"
                            onClick={() => excluir(cliente)}
                          >
                            Excluir
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="paginacao">
              <button
                type="button"
                className="btn"
                disabled={numeroPagina === 0}
                onClick={() => setNumeroPagina((n) => n - 1)}
              >
                ← Anterior
              </button>
              <span className="texto-suave">
                Página {pagina.page + 1} de {Math.max(pagina.totalPages, 1)} · {pagina.totalElements}{' '}
                cliente(s)
              </span>
              <button
                type="button"
                className="btn"
                disabled={pagina.page + 1 >= pagina.totalPages}
                onClick={() => setNumeroPagina((n) => n + 1)}
              >
                Próxima →
              </button>
            </div>
          </>
        )
      )}
    </section>
  )
}
