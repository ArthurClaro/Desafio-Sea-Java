# Desafio Backend SEA - Gestao de Clientes

Sistema de cadastro de clientes com autenticacao por perfil, validacoes de dados, persistencia sem mascaras, respostas com mascaras e integracao com o servico [ViaCEP](https://viacep.com.br/).

O repositorio esta dividido em dois projetos:

| Projeto | Pasta | Stack |
|---|---|---|
| Servico API REST | `backend/` | Java 8 target, Spring Boot 2.7, Spring Security JWT, Hibernate/JPA, Flyway, Maven, MySQL, Swagger/OpenAPI |
| Frontend PLUS | `frontend/` | React 18, Vite, React Router, Axios |

## Pre-requisitos

- JDK 8+ e Maven 3.6+ (validado localmente com JDK 21, compilando `source/target` 1.8)
- Docker, caso use o MySQL do `docker-compose.yml`
- Node.js 18+ para o frontend

## Variaveis de ambiente

O backend exige `JWT_SECRET`; sem essa variavel a aplicacao nao inicia. Use uma chave privada com pelo menos 32 caracteres.

| Variavel | Padrao | Uso |
|---|---:|---|
| `DB_HOST` | `localhost` | Host do MySQL |
| `DB_PORT` | `3307` | Porta do MySQL no host |
| `DB_NAME` | `sea_clientes` | Database usado pela API |
| `DB_USER` | `sea` | Usuario do MySQL |
| `DB_PASSWORD` | `sea123` | Senha do MySQL |
| `JWT_SECRET` | sem padrao | Chave HS256 para assinar tokens JWT |
| `JWT_EXPIRATION_MS` | `3600000` | Tempo de expiracao do token |
| `SEED_SENHA_ADMIN` | `123qwe!@#` | Senha inicial do usuario `admin` (padrao exigido pelo desafio) |
| `SEED_SENHA_USER` | `123qwe123` | Senha inicial do usuario `user` (padrao exigido pelo desafio) |
| `VITE_API_URL` | `http://localhost:8080` | Target do proxy do Vite em desenvolvimento |

Veja `.env.example` como referencia. O Spring Boot nao carrega `.env` automaticamente; exporte as variaveis no terminal, configure a IDE ou use outro mecanismo do seu ambiente.

PowerShell:

```powershell
$env:JWT_SECRET="troque-por-uma-chave-privada-com-32-caracteres-ou-mais"
```

Bash:

```bash
export JWT_SECRET="troque-por-uma-chave-privada-com-32-caracteres-ou-mais"
```

## Como executar

### 1. Banco de dados

```bash
docker compose up -d
```

O compose sobe MySQL 8 na porta `3307` e cria o database `sea_clientes` com usuario `sea`.

Sem Docker, crie um MySQL acessivel por `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e `DB_PASSWORD`.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

API: `http://localhost:8080`

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

Na inicializacao, o Flyway cria/valida as tabelas em `db/migration`. Os usuarios do desafio sao criados separadamente pelo `UsuarioSeeder`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Interface: `http://localhost:5173`

Em desenvolvimento, o Vite faz proxy de `/api` para `VITE_API_URL`. Em `npm run build`/preview/producao, o Axios continua chamando `/api`; portanto, sirva frontend e backend sob o mesmo dominio ou configure um reverse proxy.

## Usuarios de acesso

| Usuario | Senha | Permissao |
|---|---|---|
| `admin` | `123qwe!@#` | Total: criar, editar, excluir e visualizar |
| `user` | `123qwe123` | Somente visualizacao |

## Endpoints principais

| Metodo | Rota | Acesso | Descricao |
|---|---|---|---|
| `POST` | `/api/auth/login` | Publico | Autentica e retorna JWT |
| `GET` | `/api/clientes?page=0&size=10` | ADMIN e USER | Lista clientes |
| `GET` | `/api/clientes/{id}` | ADMIN e USER | Detalha cliente |
| `POST` | `/api/clientes` | ADMIN | Cadastra cliente |
| `PUT` | `/api/clientes/{id}` | ADMIN | Atualiza cliente |
| `DELETE` | `/api/clientes/{id}` | ADMIN | Exclui cliente |
| `GET` | `/api/cep/{cep}` | Autenticado | Consulta endereco no ViaCEP |

## Regras implementadas

- Autenticacao stateless com JWT e senhas BCrypt.
- `ADMIN` tem acesso total; `USER` apenas visualiza clientes.
- CPF, CEP e telefones sao aceitos com ou sem mascara oficial.
- CPF, CEP e telefones sao persistidos apenas com digitos.
- Respostas exibem CPF, CEP e telefones sempre com mascara.
- Nome obrigatorio, 3 a 100 caracteres, letras, numeros e espacos, sem espacos nas bordas.
- CPF obrigatorio, unico e validado por digitos verificadores.
- Endereco com CEP, logradouro, bairro, cidade e UF obrigatorios; complemento opcional.
- UF validada contra unidades federativas brasileiras.
- Telefones multiplos, minimo de um, com tipo `RESIDENCIAL`, `COMERCIAL` ou `CELULAR`.
- Celular exige 11 digitos; residencial/comercial exigem 10 digitos.
- E-mails multiplos, minimo de um, com formato valido.
- ViaCEP integrado pelo endpoint `/api/cep/{cep}`; dados retornados podem ser editados pelo usuario antes de salvar.
- Erros principais padronizados: `400`, `401`, `403`, `404`, `409`, `500` e `502`.

## Exemplo de uso

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123qwe!@#"}'
```

```bash
curl -X POST http://localhost:8080/api/clientes \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Silva",
    "cpf": "390.533.447-05",
    "endereco": {
      "cep": "70714-900",
      "logradouro": "SCN Quadra 02 Bloco A",
      "bairro": "Asa Norte",
      "cidade": "Brasilia",
      "uf": "DF",
      "complemento": "Sala 501"
    },
    "telefones": [
      { "tipo": "CELULAR", "numero": "(61) 98765-4321" },
      { "tipo": "COMERCIAL", "numero": "(61) 3333-4444" }
    ],
    "emails": ["maria@exemplo.com"]
  }'
```

## Testes e verificacao

Backend:

```bash
cd backend
mvn clean test
```

A suite cobre testes unitarios e de integracao com H2, incluindo autenticacao, autorizacao, validacoes, mascaras, persistencia sem mascara, ViaCEP, Swagger/health e tratamento de erros. O relatorio JaCoCo fica em `backend/target/site/jacoco/index.html`.

Frontend:

```bash
cd frontend
npm run build
```

O frontend nao possui suite automatizada propria. A verificacao minima e o build de producao, complementado por smoke manual: login admin/user, CRUD como admin, visualizacao como user, consulta de CEP e exibicao de erros do backend.

## Estrutura do backend

```text
backend/src/main/java/br/com/sea/desafio/
|-- config/        # Seguranca, CORS, OpenAPI, RestTemplate e seed de usuarios
|-- controller/    # AuthController, ClienteController, CepController
|-- domain/        # Entidades JPA
|-- dto/           # Requests e responses
|-- exception/     # Excecoes de negocio e handler global
|-- mapper/        # Conversao DTO <-> entidade com mascaras
|-- repository/    # Spring Data JPA
|-- security/      # JWT e UserDetailsService
|-- service/       # Regras de negocio e ViaCEP
|-- util/          # Utilitario de mascaras
`-- validation/    # Validadores customizados
```
