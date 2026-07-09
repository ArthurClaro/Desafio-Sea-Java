package br.com.sea.desafio.dto;

import br.com.sea.desafio.domain.TipoTelefone;
import br.com.sea.desafio.validation.TelefoneValido;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@TelefoneValido
public class TelefoneRequest {

    @NotNull(message = "Tipo de telefone é obrigatório (RESIDENCIAL, COMERCIAL ou CELULAR)")
    private TipoTelefone tipo;

    @NotBlank(message = "Número de telefone é obrigatório")
    private String numero;
}
