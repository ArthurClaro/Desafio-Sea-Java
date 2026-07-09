package br.com.sea.desafio.dto;

import br.com.sea.desafio.validation.Cpf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ClienteRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Pattern(regexp = "^[\\p{L}0-9](?:[\\p{L}0-9 ]*[\\p{L}0-9])?$",
             message = "Nome permite apenas letras, espaços e números e não pode começar ou terminar com espaço")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Cpf
    private String cpf;

    @NotNull(message = "Endereço é obrigatório")
    @Valid
    private EnderecoRequest endereco;

    @NotEmpty(message = "Pelo menos um telefone deve ser cadastrado")
    @Valid
    private List<TelefoneRequest> telefones;

    @NotEmpty(message = "Pelo menos um e-mail deve ser cadastrado")
    private List<@NotBlank(message = "E-mail não pode ser vazio")
                 @Email(message = "E-mail inválido") String> emails;
}
