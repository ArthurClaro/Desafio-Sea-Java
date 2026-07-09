package br.com.sea.desafio.controller;

import br.com.sea.desafio.service.CepService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CepControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"123qwe123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void consultaDeCepExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/cep/70714900"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultaCepRetornaDadosComCepMascarado() throws Exception {
        CepService.ViaCepResponse viaCep = new CepService.ViaCepResponse();
        viaCep.setLogradouro("SCN Quadra 4 Bloco B");
        viaCep.setBairro("Asa Norte");
        viaCep.setLocalidade("Brasília");
        viaCep.setUf("DF");
        Mockito.when(restTemplate.getForObject(anyString(), eq(CepService.ViaCepResponse.class)))
                .thenReturn(viaCep);

        mockMvc.perform(get("/api/cep/70714900").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("70714-900"))
                .andExpect(jsonPath("$.cidade").value("Brasília"))
                .andExpect(jsonPath("$.uf").value("DF"));
    }

    @Test
    void cepInexistenteRetorna404() throws Exception {
        CepService.ViaCepResponse viaCep = new CepService.ViaCepResponse();
        viaCep.setErro(true);
        Mockito.when(restTemplate.getForObject(anyString(), eq(CepService.ViaCepResponse.class)))
                .thenReturn(viaCep);

        mockMvc.perform(get("/api/cep/99999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void falhaNoViaCepRetorna502() throws Exception {
        Mockito.when(restTemplate.getForObject(anyString(), eq(CepService.ViaCepResponse.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        mockMvc.perform(get("/api/cep/70714900").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.mensagem").value("Serviço de consulta de CEP indisponível no momento"));
    }

    @Test
    void cepMalformadoRetorna400() throws Exception {
        mockMvc.perform(get("/api/cep/123").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/cep/cep70714900").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
