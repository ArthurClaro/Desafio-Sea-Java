-- Esquema inicial do desafio: clientes, telefones, e-mails e usuários de acesso.
-- CPF, CEP e telefones são persistidos SEM máscara (apenas dígitos).

CREATE TABLE clientes (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(100) NOT NULL,
    cpf         VARCHAR(11)  NOT NULL,
    cep         VARCHAR(8)   NOT NULL,
    logradouro  VARCHAR(255) NOT NULL,
    bairro      VARCHAR(255) NOT NULL,
    cidade      VARCHAR(255) NOT NULL,
    uf          VARCHAR(2)   NOT NULL,
    complemento VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_clientes_cpf UNIQUE (cpf)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE telefones (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    tipo       VARCHAR(20) NOT NULL,
    numero     VARCHAR(11) NOT NULL,
    cliente_id BIGINT      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_telefones_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE emails (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    endereco   VARCHAR(255) NOT NULL,
    cliente_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_emails_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE usuarios (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_username UNIQUE (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
