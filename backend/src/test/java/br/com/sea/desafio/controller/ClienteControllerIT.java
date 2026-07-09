package br.com.sea.desafio.controller;

import br.com.sea.desafio.domain.Cliente;
import br.com.sea.desafio.repository.ClienteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerIT {

    private static final String CPF_VALIDO = "390.533.447-05";
    private static final String CPF_VALIDO_2 = "529.982.247-25";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    private String tokenAdmin;
    private String tokenUser;

    @BeforeEach
    void setUp() throws Exception {
        clienteRepository.deleteAll();
        tokenAdmin = login("admin", "123qwe!@#");
        tokenUser = login("user", "123qwe123");
    }

    @Test
    void endpointsPublicosNaoExigemToken() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void loginComCamposEmBrancoRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nomeAceitaLetrasComAcentosENumeros() throws Exception {
        Map<String, Object> cliente = clienteValido(CPF_VALIDO);
        cliente.put("nome", "José Antônio Conceição 3");

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("José Antônio Conceição 3"));
    }

    @Test
    void ufForaDaListaDeEstadosRetorna400() throws Exception {
        Map<String, Object> cliente = clienteValido(CPF_VALIDO);
        @SuppressWarnings("unchecked")
        Map<String, Object> endereco = (Map<String, Object>) cliente.get("endereco");
        endereco.put("uf", "XX");

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['endereco.uf']").exists());
    }

    @Test
    @Transactional
    void atualizarSubstituiTelefonesEEmailsSemDeixarOrfaos() throws Exception {
        long id = criarCliente(CPF_VALIDO); // cria com 2 telefones e 2 e-mails

        Map<String, Object> atualizado = clienteValido(CPF_VALIDO);
        Map<String, Object> unicoTelefone = new LinkedHashMap<>();
        unicoTelefone.put("tipo", "RESIDENCIAL");
        unicoTelefone.put("numero", "(61) 3555-7777");
        atualizado.put("telefones", Collections.singletonList(unicoTelefone));
        atualizado.put("emails", Collections.singletonList("novo@cliente.com"));

        mockMvc.perform(put("/api/clientes/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefones.length()").value(1))
                .andExpect(jsonPath("$.telefones[0].numero").value("(61) 3555-7777"))
                .andExpect(jsonPath("$.emails.length()").value(1));

        Cliente salvo = clienteRepository.findById(id).orElseThrow(IllegalStateException::new);
        assertThat(salvo.getTelefones()).hasSize(1);
        assertThat(salvo.getEmails()).hasSize(1);
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requisicaoSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requisicaoComTokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void usuarioPadraoPodeApenasVisualizar() throws Exception {
        long id = criarCliente(CPF_VALIDO);

        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido(CPF_VALIDO))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/clientes/" + id)
                        .header("Authorization", "Bearer " + tokenUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido(CPF_VALIDO))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/clientes/1").header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void adminCriaClienteComMascaraEPersisteSemMascara() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido(CPF_VALIDO))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("390.533.447-05"))
                .andExpect(jsonPath("$.endereco.cep").value("70714-900"))
                .andExpect(jsonPath("$.telefones[0].numero").value("(61) 98765-4321"))
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Persistido sem máscara
        Cliente salvo = clienteRepository.findById(id).orElseThrow(IllegalStateException::new);
        assertThat(salvo.getCpf()).isEqualTo("39053344705");
        assertThat(salvo.getEndereco().getCep()).isEqualTo("70714900");
        assertThat(salvo.getTelefones().get(0).getNumero()).isEqualTo("61987654321");

        // Listagem exibida com máscara
        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].cpf").value("390.533.447-05"));
    }

    @Test
    void adminAtualizaEExcluiCliente() throws Exception {
        long id = criarCliente(CPF_VALIDO);

        Map<String, Object> atualizado = clienteValido(CPF_VALIDO);
        atualizado.put("nome", "Nome Atualizado 2");
        mockMvc.perform(put("/api/clientes/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado 2"));

        mockMvc.perform(delete("/api/clientes/" + id).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/clientes/" + id).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void cpfDuplicadoRetorna409() throws Exception {
        criarCliente(CPF_VALIDO);

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido("39053344705"))))
                .andExpect(status().isConflict());
    }

    @Test
    void payloadInvalidoRetorna400ComErrosPorCampo() throws Exception {
        Map<String, Object> invalido = clienteValido("123.456.789-00"); // CPF com dígito verificador errado
        invalido.put("nome", "a!");                                     // curto e com caractere proibido
        invalido.put("emails", Collections.singletonList("nao-e-email"));

        Map<String, Object> telefoneInvalido = new LinkedHashMap<>();
        telefoneInvalido.put("tipo", "CELULAR");
        telefoneInvalido.put("numero", "(61) 3333-4444"); // 10 dígitos para celular
        invalido.put("telefones", Collections.singletonList(telefoneInvalido));

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").exists())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos['emails[0]']").exists())
                .andExpect(jsonPath("$.campos['telefones[0].numero']").exists());
    }

    @Test
    void payloadComFormatosPoluidosRetorna400() throws Exception {
        Map<String, Object> invalido = clienteValido("abc390.533.447-05xyz");
        invalido.put("nome", " a ");

        @SuppressWarnings("unchecked")
        Map<String, Object> endereco = (Map<String, Object>) invalido.get("endereco");
        endereco.put("cep", "cep70714-900");

        Map<String, Object> telefoneInvalido = new LinkedHashMap<>();
        telefoneInvalido.put("tipo", "CELULAR");
        telefoneInvalido.put("numero", "tel(61) 98765-4321");
        invalido.put("telefones", Collections.singletonList(telefoneInvalido));

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").exists())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos['endereco.cep']").exists())
                .andExpect(jsonPath("$.campos['telefones[0].numero']").exists());
    }

    @Test
    void clienteSemTelefoneOuSemEmailRetorna400() throws Exception {
        Map<String, Object> semTelefone = clienteValido(CPF_VALIDO_2);
        semTelefone.put("telefones", Collections.emptyList());

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semTelefone)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.telefones").exists());

        Map<String, Object> semEmail = clienteValido(CPF_VALIDO_2);
        semEmail.put("emails", Collections.emptyList());

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semEmail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.emails").exists());
    }

    private long criarCliente(String cpf) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido(cpf))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private Map<String, Object> clienteValido(String cpf) {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "70714-900");
        endereco.put("logradouro", "SCN Quadra 02 Bloco A");
        endereco.put("bairro", "Asa Norte");
        endereco.put("cidade", "Brasília");
        endereco.put("uf", "DF");
        endereco.put("complemento", "Sala 501");

        Map<String, Object> celular = new LinkedHashMap<>();
        celular.put("tipo", "CELULAR");
        celular.put("numero", "(61) 98765-4321");

        Map<String, Object> comercial = new LinkedHashMap<>();
        comercial.put("tipo", "COMERCIAL");
        comercial.put("numero", "(61) 3333-4444");

        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("nome", "Cliente de Teste 123");
        cliente.put("cpf", cpf);
        cliente.put("endereco", endereco);
        cliente.put("telefones", Arrays.asList(celular, comercial));
        cliente.put("emails", Arrays.asList("contato@cliente.com", "financeiro@cliente.com"));
        return cliente;
    }
}
