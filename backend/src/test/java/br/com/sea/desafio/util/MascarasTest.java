package br.com.sea.desafio.util;

import br.com.sea.desafio.domain.TipoTelefone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MascarasTest {

    @Test
    void somenteDigitosRemoveMascaras() {
        assertThat(Mascaras.somenteDigitos("390.533.447-05")).isEqualTo("39053344705");
        assertThat(Mascaras.somenteDigitos("70714-900")).isEqualTo("70714900");
        assertThat(Mascaras.somenteDigitos("(61) 98765-4321")).isEqualTo("61987654321");
        assertThat(Mascaras.somenteDigitos(null)).isNull();
    }

    @Test
    void formataCpfComMascara() {
        assertThat(Mascaras.formatarCpf("39053344705")).isEqualTo("390.533.447-05");
    }

    @Test
    void formataCepComMascara() {
        assertThat(Mascaras.formatarCep("70714900")).isEqualTo("70714-900");
    }

    @Test
    void formataCelularCom11Digitos() {
        assertThat(Mascaras.formatarTelefone(TipoTelefone.CELULAR, "61987654321"))
                .isEqualTo("(61) 98765-4321");
    }

    @Test
    void formataFixoCom10Digitos() {
        assertThat(Mascaras.formatarTelefone(TipoTelefone.RESIDENCIAL, "6133334444"))
                .isEqualTo("(61) 3333-4444");
        assertThat(Mascaras.formatarTelefone(TipoTelefone.COMERCIAL, "6133334444"))
                .isEqualTo("(61) 3333-4444");
    }
}
