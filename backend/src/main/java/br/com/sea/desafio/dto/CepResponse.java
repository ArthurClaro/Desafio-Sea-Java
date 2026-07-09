package br.com.sea.desafio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resposta do endpoint de consulta de CEP (CEP com máscara).
 */
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
