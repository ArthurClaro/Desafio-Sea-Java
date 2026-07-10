import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import http from '../api/http'
import { maskCep, maskCpf, maskTelefone, somenteDigitos } from '../utils/mascaras'

const TIPOS_TELEFONE = [
  { valor: 'CELULAR', rotulo: 'Celular' },
  { valor: 'RESIDENCIAL', rotulo: 'Residencial' },
  { valor: 'COMERCIAL', rotulo: 'Comercial' },
]

const enderecoVazio = { cep: '', logradouro: '', bairro: '', cidade: '', uf: '', complemento: '' }

// Ids estáveis para as linhas dinâmicas — key={indice} desloca o estado dos
// inputs quando um item é removido do meio da lista
let proximoIdLinha = 1
const novaLinhaId = () => proximoIdLinha++

export default function ClienteForm() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navigate = useNavigate()

  const [nome, setNome] = useState('')
  const [cpf, setCpf] = useState('')
  const [endereco, setEndereco] = useState(enderecoVazio)
  const [telefones, setTelefones] = useState(() => [{ id: novaLinhaId(), tipo: 'CELULAR', numero: '' }])
  const [emails, setEmails] = useState(() => [{ id: novaLinhaId(), valor: '' }])

  const [buscandoCep, setBuscandoCep] = useState(false)
  const [avisoCep, setAvisoCep] = useState('')
  const [erro, setErro] = useState('')
  const [errosCampos, setErrosCampos] = useState({})
  const [salvando, setSalvando] = useState(false)
  const [carregando, setCarregando] = useState(editando)

  useEffect(() => {
    if (!editando) return
    http
      .get(`/clientes/${id}`)
      .then(({ data }) => {
        setNome(data.nome)
        setCpf(data.cpf)
        setEndereco({ ...data.endereco, complemento: data.endereco.complemento || '' })
        setTelefones(data.telefones.map((t) => ({ id: novaLinhaId(), tipo: t.tipo, numero: t.numero })))
        setEmails(data.emails.map((email) => ({ id: novaLinhaId(), valor: email })))
      })
      .catch(() => setErro('Não foi possível carregar o cliente.'))
      .finally(() => setCarregando(false))
  }, [editando, id])

  async function aoMudarCep(valor) {
    const mascarado = maskCep(valor)
    setEndereco((atual) => ({ ...atual, cep: mascarado }))
    setAvisoCep('')

    const digitos = somenteDigitos(mascarado)
    if (digitos.length !== 8) return

    setBuscandoCep(true)
    try {
      const { data } = await http.get(`/cep/${digitos}`)
      setEndereco((atual) => ({
        ...atual,
        cep: data.cep,
        logradouro: data.logradouro || atual.logradouro,
        bairro: data.bairro || atual.bairro,
        cidade: data.cidade || atual.cidade,
        uf: data.uf || atual.uf,
      }))
    } catch (err) {
      setAvisoCep(
        err.response?.status === 404
          ? 'CEP não encontrado. Preencha o endereço manualmente.'
          : 'Não foi possível consultar o CEP. Preencha o endereço manualmente.',
      )
    } finally {
      setBuscandoCep(false)
    }
  }

  function mudarTelefone(id, campo, valor) {
    setTelefones((atuais) =>
      atuais.map((t) => {
        if (t.id !== id) return t
        if (campo === 'tipo') return { ...t, tipo: valor, numero: maskTelefone(valor, t.numero) }
        return { ...t, numero: maskTelefone(t.tipo, valor) }
      }),
    )
  }

  async function enviar(e) {
    e.preventDefault()
    setErro('')
    setErrosCampos({})
    setSalvando(true)

    const payload = {
      nome: nome.trim(),
      cpf,
      endereco: { ...endereco, complemento: endereco.complemento.trim() || null },
      telefones: telefones.map(({ tipo, numero }) => ({ tipo, numero })),
      emails: emails.map(({ valor }) => valor.trim()).filter(Boolean),
    }

    try {
      if (editando) {
        await http.put(`/clientes/${id}`, payload)
      } else {
        await http.post('/clientes', payload)
      }
      navigate('/')
    } catch (err) {
      const dados = err.response?.data
      setErro(dados?.mensagem || 'Não foi possível salvar o cliente.')
      setErrosCampos(dados?.campos || {})
    } finally {
      setSalvando(false)
    }
  }

  if (carregando) return <p className="texto-suave" role="status" aria-live="polite">Carregando…</p>

  return (
    <section className="form-pagina">
      <div className="pagina__cabecalho">
        <h2>{editando ? 'Editar cliente' : 'Novo cliente'}</h2>
        <Link to="/" className="btn btn--ghost">
          ← Voltar
        </Link>
      </div>

      {erro && (
        <div className="alerta alerta--erro" role="alert">
          <strong>{erro}</strong>
          {Object.keys(errosCampos).length > 0 && (
            <ul>
              {Object.entries(errosCampos).map(([campo, mensagem]) => (
                <li key={campo}>
                  <code>{campo}</code>: {mensagem}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <form onSubmit={enviar}>
        <div className="card form-secao">
          <h3>Dados pessoais</h3>
          <div className="grade grade--2">
            <label className="campo">
              <span>Nome *</span>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Nome do cliente"
                minLength={3}
                maxLength={100}
                required
              />
              <small className="texto-suave">3 a 100 caracteres — apenas letras, espaços e números</small>
            </label>
            <label className="campo">
              <span>CPF *</span>
              <input
                type="text"
                className="mono"
                value={cpf}
                onChange={(e) => setCpf(maskCpf(e.target.value))}
                placeholder="000.000.000-00"
                required
              />
            </label>
          </div>
        </div>

        <div className="card form-secao">
          <h3>Endereço</h3>
          <div className="grade grade--3">
            <label className="campo">
              <span>CEP *</span>
              <input
                type="text"
                className="mono"
                value={endereco.cep}
                onChange={(e) => aoMudarCep(e.target.value)}
                placeholder="00000-000"
                required
              />
              <small className="texto-suave">
                {buscandoCep ? 'Buscando CEP…' : 'Preenchimento automático via ViaCEP'}
              </small>
            </label>
            <label className="campo grade__span2">
              <span>Logradouro *</span>
              <input
                type="text"
                value={endereco.logradouro}
                onChange={(e) => setEndereco({ ...endereco, logradouro: e.target.value })}
                required
              />
            </label>
          </div>
          {avisoCep && <div className="alerta alerta--aviso" role="status" aria-live="polite">{avisoCep}</div>}
          <div className="grade grade--3">
            <label className="campo">
              <span>Bairro *</span>
              <input
                type="text"
                value={endereco.bairro}
                onChange={(e) => setEndereco({ ...endereco, bairro: e.target.value })}
                required
              />
            </label>
            <label className="campo">
              <span>Cidade *</span>
              <input
                type="text"
                value={endereco.cidade}
                onChange={(e) => setEndereco({ ...endereco, cidade: e.target.value })}
                required
              />
            </label>
            <label className="campo">
              <span>UF *</span>
              <input
                type="text"
                value={endereco.uf}
                onChange={(e) =>
                  setEndereco({ ...endereco, uf: e.target.value.replace(/[^A-Za-z]/g, '').toUpperCase().slice(0, 2) })
                }
                placeholder="DF"
                maxLength={2}
                required
              />
            </label>
          </div>
          <label className="campo">
            <span>Complemento</span>
            <input
              type="text"
              value={endereco.complemento}
              onChange={(e) => setEndereco({ ...endereco, complemento: e.target.value })}
              placeholder="Opcional"
            />
          </label>
        </div>

        <div className="card form-secao">
          <div className="form-secao__cabecalho">
            <h3>Telefones *</h3>
            <button
              type="button"
              className="btn btn--pequeno"
              onClick={() => setTelefones([...telefones, { id: novaLinhaId(), tipo: 'CELULAR', numero: '' }])}
            >
              + Adicionar telefone
            </button>
          </div>
          {telefones.map((telefone) => (
            <div className="linha-dinamica" key={telefone.id}>
              <label className="campo">
                <span>Tipo</span>
                <select
                  value={telefone.tipo}
                  onChange={(e) => mudarTelefone(telefone.id, 'tipo', e.target.value)}
                >
                  {TIPOS_TELEFONE.map((tipo) => (
                    <option key={tipo.valor} value={tipo.valor}>
                      {tipo.rotulo}
                    </option>
                  ))}
                </select>
              </label>
              <label className="campo linha-dinamica__principal">
                <span>Número</span>
                <input
                  type="text"
                  className="mono"
                  value={telefone.numero}
                  onChange={(e) => mudarTelefone(telefone.id, 'numero', e.target.value)}
                  placeholder={telefone.tipo === 'CELULAR' ? '(00) 00000-0000' : '(00) 0000-0000'}
                  required
                />
              </label>
              <button
                type="button"
                className="btn btn--pequeno btn--perigo"
                onClick={() => setTelefones(telefones.filter((t) => t.id !== telefone.id))}
                disabled={telefones.length === 1}
                title={telefones.length === 1 ? 'Pelo menos um telefone é obrigatório' : 'Remover'}
              >
                Remover
              </button>
            </div>
          ))}
        </div>

        <div className="card form-secao">
          <div className="form-secao__cabecalho">
            <h3>E-mails *</h3>
            <button
              type="button"
              className="btn btn--pequeno"
              onClick={() => setEmails([...emails, { id: novaLinhaId(), valor: '' }])}
            >
              + Adicionar e-mail
            </button>
          </div>
          {emails.map((email) => (
            <div className="linha-dinamica" key={email.id}>
              <label className="campo linha-dinamica__principal">
                <span>E-mail</span>
                <input
                  type="email"
                  value={email.valor}
                  onChange={(e) =>
                    setEmails(emails.map((atual) => (atual.id === email.id ? { ...atual, valor: e.target.value } : atual)))
                  }
                  placeholder="contato@exemplo.com"
                  required
                />
              </label>
              <button
                type="button"
                className="btn btn--pequeno btn--perigo"
                onClick={() => setEmails(emails.filter((atual) => atual.id !== email.id))}
                disabled={emails.length === 1}
                title={emails.length === 1 ? 'Pelo menos um e-mail é obrigatório' : 'Remover'}
              >
                Remover
              </button>
            </div>
          ))}
        </div>

        <div className="form-acoes">
          <Link to="/" className="btn">
            Cancelar
          </Link>
          <button type="submit" className="btn btn--primario" disabled={salvando}>
            {salvando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Cadastrar cliente'}
          </button>
        </div>
      </form>
    </section>
  )
}
