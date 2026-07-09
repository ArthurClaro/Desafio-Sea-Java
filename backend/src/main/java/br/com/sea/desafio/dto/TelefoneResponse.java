package br.com.sea.desafio.dto;

import br.com.sea.desafio.domain.TipoTelefone;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TelefoneResponse {

    private final TipoTelefone tipo;
    private final String numero;
}
