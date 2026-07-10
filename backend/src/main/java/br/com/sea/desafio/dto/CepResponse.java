package br.com.sea.desafio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CepResponse {

    private final String cep;
    private final String logradouro;
    private final String bairro;
    private final String cidade;
    private final String uf;
    private final String complemento;
}
