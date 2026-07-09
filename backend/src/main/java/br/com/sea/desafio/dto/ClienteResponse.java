package br.com.sea.desafio.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Resposta da API: CPF, CEP e telefones sempre COM máscara.
 */
@Getter
@Setter
public class ClienteResponse {

    private Long id;
    private String nome;
    private String cpf;
    private EnderecoResponse endereco;
    private List<TelefoneResponse> telefones;
    private List<String> emails;
}
