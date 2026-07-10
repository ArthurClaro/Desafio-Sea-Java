package br.com.sea.desafio.service;

import br.com.sea.desafio.domain.Cliente;
import br.com.sea.desafio.domain.TipoTelefone;
import br.com.sea.desafio.dto.ClienteRequest;
import br.com.sea.desafio.dto.ClienteResponse;
import br.com.sea.desafio.dto.EnderecoRequest;
import br.com.sea.desafio.dto.TelefoneRequest;
import br.com.sea.desafio.exception.CpfDuplicadoException;
import br.com.sea.desafio.exception.DadosInvalidosException;
import br.com.sea.desafio.exception.RecursoNaoEncontradoException;
import br.com.sea.desafio.mapper.ClienteMapper;
import br.com.sea.desafio.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService service;

    @BeforeEach
    void setUp() {
        service = new ClienteService(clienteRepository, new ClienteMapper());
    }

    @Test
    void criarNormalizaMascarasAntesDePersistir() {
        when(clienteRepository.existsByCpf("39053344705")).thenReturn(false);
        when(clienteRepository.saveAndFlush(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = service.criar(requestValido());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).saveAndFlush(captor.capture());
        Cliente salvo = captor.getValue();

        // persistido SEM máscara
        assertThat(salvo.getCpf()).isEqualTo("39053344705");
        assertThat(salvo.getEndereco().getCep()).isEqualTo("70714900");
        assertThat(salvo.getTelefones().get(0).getNumero()).isEqualTo("61987654321");
        assertThat(salvo.getEndereco().getUf()).isEqualTo("DF");

        // resposta COM máscara
        assertThat(response.getCpf()).isEqualTo("390.533.447-05");
        assertThat(response.getEndereco().getCep()).isEqualTo("70714-900");
        assertThat(response.getTelefones().get(0).getNumero()).isEqualTo("(61) 98765-4321");
    }

    @Test
    void criarRejeitaCpfDuplicado() {
        when(clienteRepository.existsByCpf("39053344705")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(requestValido()))
                .isInstanceOf(CpfDuplicadoException.class);
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void criarMapeiaViolacaoDeIntegridadeParaCpfDuplicado() {
        when(clienteRepository.existsByCpf("39053344705")).thenReturn(false);
        when(clienteRepository.saveAndFlush(any(Cliente.class)))
                .thenThrow(new DataIntegrityViolationException("uk_clientes_cpf"));

        assertThatThrownBy(() -> service.criar(requestValido()))
                .isInstanceOf(CpfDuplicadoException.class);
    }

    @Test
    void atualizarRejeitaCpfDeOutroCliente() {
        Cliente existente = new Cliente();
        existente.setId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByCpfAndIdNot("39053344705", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, requestValido()))
                .isInstanceOf(CpfDuplicadoException.class);
    }

    @Test
    void buscarPorIdInexistenteLancaNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void excluirInexistenteLancaNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void criarRejeitaTelefonesDuplicadosMesmoComMascaraDiferente() {
        ClienteRequest request = requestValido();
        TelefoneRequest repetido = new TelefoneRequest();
        repetido.setTipo(TipoTelefone.CELULAR);
        repetido.setNumero("61987654321"); // mesmo número do requestValido, sem máscara
        request.setTelefones(Arrays.asList(request.getTelefones().get(0), repetido));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessageContaining("Telefone duplicado");
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void criarRejeitaEmailsDuplicadosIgnorandoCaixa() {
        ClienteRequest request = requestValido();
        request.setEmails(Arrays.asList("contato@teste.com", "CONTATO@teste.com"));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessageContaining("E-mail duplicado");
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    private ClienteRequest requestValido() {
        EnderecoRequest endereco = new EnderecoRequest();
        endereco.setCep("70714-900");
        endereco.setLogradouro("SCN Quadra 02 Bloco A");
        endereco.setBairro("Asa Norte");
        endereco.setCidade("Brasília");
        endereco.setUf("df");
        endereco.setComplemento("Sala 501");

        TelefoneRequest telefone = new TelefoneRequest();
        telefone.setTipo(TipoTelefone.CELULAR);
        telefone.setNumero("(61) 98765-4321");

        ClienteRequest request = new ClienteRequest();
        request.setNome("Cliente Teste 1");
        request.setCpf("390.533.447-05");
        request.setEndereco(endereco);
        request.setTelefones(Collections.singletonList(telefone));
        request.setEmails(Arrays.asList("contato@teste.com", "outro@teste.com"));
        return request;
    }
}
