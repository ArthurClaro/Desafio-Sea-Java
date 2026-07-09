package br.com.sea.desafio.validation;

import br.com.sea.desafio.domain.TipoTelefone;
import br.com.sea.desafio.dto.TelefoneRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

class TelefoneValidoValidatorTest {

    private final TelefoneValidoValidator validator = new TelefoneValidoValidator();
    private final ConstraintValidatorContext context =
            Mockito.mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

    @Test
    void celularExige11Digitos() {
        assertThat(validator.isValid(telefone(TipoTelefone.CELULAR, "(61) 98765-4321"), context)).isTrue();
        assertThat(validator.isValid(telefone(TipoTelefone.CELULAR, "61987654321"), context)).isTrue();
        assertThat(validator.isValid(telefone(TipoTelefone.CELULAR, "(61) 3333-4444"), context)).isFalse();
        assertThat(validator.isValid(telefone(TipoTelefone.CELULAR, "tel(61) 98765-4321"), context)).isFalse();
    }

    @Test
    void residencialEComercialExigem10Digitos() {
        assertThat(validator.isValid(telefone(TipoTelefone.RESIDENCIAL, "(61) 3333-4444"), context)).isTrue();
        assertThat(validator.isValid(telefone(TipoTelefone.COMERCIAL, "6133334444"), context)).isTrue();
        assertThat(validator.isValid(telefone(TipoTelefone.RESIDENCIAL, "61987654321"), context)).isFalse();
        assertThat(validator.isValid(telefone(TipoTelefone.COMERCIAL, "(61) 98765-4321"), context)).isFalse();
        assertThat(validator.isValid(telefone(TipoTelefone.RESIDENCIAL, "abc6133334444"), context)).isFalse();
    }

    @Test
    void delegaObrigatoriedadeParaOutrasConstraints() {
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid(telefone(null, "6133334444"), context)).isTrue();
        assertThat(validator.isValid(telefone(TipoTelefone.CELULAR, null), context)).isTrue();
        assertThat(validator.isValid(telefone(TipoTelefone.CELULAR, "  "), context)).isTrue();
    }

    private TelefoneRequest telefone(TipoTelefone tipo, String numero) {
        TelefoneRequest request = new TelefoneRequest();
        request.setTipo(tipo);
        request.setNumero(numero);
        return request;
    }
}
