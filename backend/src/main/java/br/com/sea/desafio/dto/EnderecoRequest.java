package br.com.sea.desafio.dto;

import br.com.sea.desafio.validation.Cep;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class EnderecoRequest {

    @NotBlank(message = "CEP é obrigatório")
    @Cep
    private String cep;

    @NotBlank(message = "Logradouro é obrigatório")
    @Size(max = 255)
    private String logradouro;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 255)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 255)
    private String cidade;

    @NotBlank(message = "UF é obrigatória")
    @Pattern(regexp = "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
             flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "UF inválida: informe uma unidade federativa brasileira (ex.: DF, SP)")
    private String uf;

    @Size(max = 255)
    private String complemento;
}
