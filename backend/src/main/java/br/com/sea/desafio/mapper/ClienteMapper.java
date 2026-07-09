package br.com.sea.desafio.mapper;

import br.com.sea.desafio.domain.Cliente;
import br.com.sea.desafio.domain.Email;
import br.com.sea.desafio.domain.Endereco;
import br.com.sea.desafio.domain.Telefone;
import br.com.sea.desafio.dto.ClienteRequest;
import br.com.sea.desafio.dto.ClienteResponse;
import br.com.sea.desafio.dto.EnderecoRequest;
import br.com.sea.desafio.dto.EnderecoResponse;
import br.com.sea.desafio.dto.TelefoneResponse;
import br.com.sea.desafio.util.Mascaras;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Conversão DTO <-> entidade aplicando a regra de máscaras do desafio:
 * entrada é normalizada (sem máscara) antes de persistir; saída é formatada (com máscara).
 */
@Component
public class ClienteMapper {

    public void aplicarDados(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.getNome().trim());
        cliente.setCpf(Mascaras.somenteDigitos(request.getCpf()));
        cliente.setEndereco(toEndereco(request.getEndereco()));

        cliente.getTelefones().clear();
        request.getTelefones().forEach(t ->
                cliente.getTelefones().add(new Telefone(t.getTipo(), Mascaras.somenteDigitos(t.getNumero()))));

        cliente.getEmails().clear();
        request.getEmails().forEach(e -> cliente.getEmails().add(new Email(e.trim())));
    }

    private Endereco toEndereco(EnderecoRequest request) {
        Endereco endereco = new Endereco();
        endereco.setCep(Mascaras.somenteDigitos(request.getCep()));
        endereco.setLogradouro(request.getLogradouro().trim());
        endereco.setBairro(request.getBairro().trim());
        endereco.setCidade(request.getCidade().trim());
        endereco.setUf(request.getUf().trim().toUpperCase(Locale.ROOT));
        endereco.setComplemento(request.getComplemento() == null ? null : request.getComplemento().trim());
        return endereco;
    }

    public ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setNome(cliente.getNome());
        response.setCpf(Mascaras.formatarCpf(cliente.getCpf()));
        response.setEndereco(toEnderecoResponse(cliente.getEndereco()));

        List<TelefoneResponse> telefones = cliente.getTelefones().stream()
                .map(t -> new TelefoneResponse(t.getTipo(), Mascaras.formatarTelefone(t.getTipo(), t.getNumero())))
                .collect(Collectors.toList());
        response.setTelefones(telefones);

        response.setEmails(cliente.getEmails().stream()
                .map(Email::getEndereco)
                .collect(Collectors.toList()));
        return response;
    }

    private EnderecoResponse toEnderecoResponse(Endereco endereco) {
        EnderecoResponse response = new EnderecoResponse();
        response.setCep(Mascaras.formatarCep(endereco.getCep()));
        response.setLogradouro(endereco.getLogradouro());
        response.setBairro(endereco.getBairro());
        response.setCidade(endereco.getCidade());
        response.setUf(endereco.getUf());
        response.setComplemento(endereco.getComplemento());
        return response;
    }
}
