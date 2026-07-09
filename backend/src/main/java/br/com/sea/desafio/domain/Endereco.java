package br.com.sea.desafio.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Endereco {

    /** CEP persistido sem máscara (apenas dígitos). */
    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 255)
    private String logradouro;

    @Column(nullable = false, length = 255)
    private String bairro;

    @Column(nullable = false, length = 255)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(length = 255)
    private String complemento;
}
