package br.com.sea.desafio.service;

import br.com.sea.desafio.dto.CepResponse;
import br.com.sea.desafio.exception.CepInvalidoException;
import br.com.sea.desafio.exception.RecursoNaoEncontradoException;
import br.com.sea.desafio.exception.ViaCepIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CepServiceTest {

    private static final String URL = "https://viacep.com.br/ws";

    @Mock
    private RestTemplate restTemplate;

    private CepService service;

    @BeforeEach
    void setUp() {
        service = new CepService(restTemplate, URL);
    }

    @Test
    void consultaCepComOuSemMascaraERetornaCepMascarado() {
        CepService.ViaCepResponse viaCep = new CepService.ViaCepResponse();
        viaCep.setLogradouro("SCN Quadra 2 Bloco A");
        viaCep.setBairro("Asa Norte");
        viaCep.setLocalidade("Brasília");
        viaCep.setUf("DF");
        when(restTemplate.getForObject(eq(URL + "/70714900/json/"), eq(CepService.ViaCepResponse.class)))
                .thenReturn(viaCep);

        CepResponse response = service.consultar("70714-900");

        assertThat(response.getCep()).isEqualTo("70714-900");
        assertThat(response.getLogradouro()).isEqualTo("SCN Quadra 2 Bloco A");
        assertThat(response.getBairro()).isEqualTo("Asa Norte");
        assertThat(response.getCidade()).isEqualTo("Brasília");
        assertThat(response.getUf()).isEqualTo("DF");
    }

    @Test
    void cepComQuantidadeErradaDeDigitosEInvalido() {
        assertThatThrownBy(() -> service.consultar("1234"))
                .isInstanceOf(CepInvalidoException.class);
    }

    @Test
    void cepComLetrasEInvalidoMesmoComOitoDigitos() {
        assertThatThrownBy(() -> service.consultar("cep70714900"))
                .isInstanceOf(CepInvalidoException.class);
    }

    @Test
    void cepInexistenteRetornaNaoEncontrado() {
        CepService.ViaCepResponse viaCep = new CepService.ViaCepResponse();
        viaCep.setErro(true);
        when(restTemplate.getForObject(eq(URL + "/99999999/json/"), eq(CepService.ViaCepResponse.class)))
                .thenReturn(viaCep);

        assertThatThrownBy(() -> service.consultar("99999-999"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void falhaDeComunicacaoViraServicoIndisponivel() {
        when(restTemplate.getForObject(eq(URL + "/70714900/json/"), eq(CepService.ViaCepResponse.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> service.consultar("70714900"))
                .isInstanceOf(ViaCepIndisponivelException.class);
    }
}
