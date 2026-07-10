package br.com.sea.desafio.service;

import br.com.sea.desafio.dto.CepResponse;
import br.com.sea.desafio.exception.CepInvalidoException;
import br.com.sea.desafio.exception.RecursoNaoEncontradoException;
import br.com.sea.desafio.exception.ViaCepIndisponivelException;
import br.com.sea.desafio.util.Mascaras;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CepService {

    private final RestTemplate restTemplate;
    private final String viaCepUrl;

    public CepService(RestTemplate restTemplate, @Value("${app.viacep.url}") String viaCepUrl) {
        this.restTemplate = restTemplate;
        this.viaCepUrl = viaCepUrl;
    }

    public CepResponse consultar(String cep) {
        String entrada = cep == null ? null : cep.trim();
        if (entrada == null || !Mascaras.CEP_FORMATO_ACEITO.matcher(entrada).matches()) {
            throw new CepInvalidoException("CEP inválido: deve conter 8 dígitos");
        }
        String cepNormalizado = Mascaras.somenteDigitos(entrada);

        ViaCepResponse viaCep;
        try {
            viaCep = restTemplate.getForObject(viaCepUrl + "/" + cepNormalizado + "/json/", ViaCepResponse.class);
        } catch (RestClientException e) {
            throw new ViaCepIndisponivelException(e);
        }

        if (viaCep == null || viaCep.isErro()) {
            throw new RecursoNaoEncontradoException("CEP não encontrado: " + Mascaras.formatarCep(cepNormalizado));
        }

        return new CepResponse(
                Mascaras.formatarCep(cepNormalizado),
                viaCep.getLogradouro(),
                viaCep.getBairro(),
                viaCep.getLocalidade(),
                viaCep.getUf(),
                viaCep.getComplemento());
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ViaCepResponse {
        private String logradouro;
        private String complemento;
        private String bairro;
        private String localidade;
        private String uf;
        private boolean erro;
    }
}
