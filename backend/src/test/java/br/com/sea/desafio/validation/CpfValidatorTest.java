package br.com.sea.desafio.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @ParameterizedTest
    @ValueSource(strings = {"39053344705", "390.533.447-05", "11144477735", "529.982.247-25"})
    void aceitaCpfValidoComOuSemMascara(String cpf) {
        assertThat(validator.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "39053344700",
            "123.456.789-00",
            "123",
            "111.111.111-11",
            "00000000000",
            "abcdefghijk",
            "abc390.533.447-05xyz",
            "390.533.44705",
            "390533447-05"
    })
    void rejeitaCpfInvalido(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    @Test
    void delegaObrigatoriedadeParaNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("  ", null)).isTrue();
    }
}
