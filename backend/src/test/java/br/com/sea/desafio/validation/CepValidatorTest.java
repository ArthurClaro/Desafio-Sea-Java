package br.com.sea.desafio.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CepValidatorTest {

    private final CepValidator validator = new CepValidator();

    @ParameterizedTest
    @ValueSource(strings = {"70714900", "70714-900"})
    void aceitaCepComOuSemMascara(String cep) {
        assertThat(validator.isValid(cep, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "7071-4900", "cep70714-900", "70714-900xyz"})
    void rejeitaCepMalformado(String cep) {
        assertThat(validator.isValid(cep, null)).isFalse();
    }
}
