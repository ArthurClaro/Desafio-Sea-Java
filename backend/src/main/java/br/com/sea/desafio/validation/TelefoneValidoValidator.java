package br.com.sea.desafio.validation;

import br.com.sea.desafio.domain.TipoTelefone;
import br.com.sea.desafio.dto.TelefoneRequest;
import br.com.sea.desafio.util.Mascaras;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class TelefoneValidoValidator implements ConstraintValidator<TelefoneValido, TelefoneRequest> {

    private static final Pattern CELULAR =
            Pattern.compile("^(\\d{11}|\\(\\d{2}\\) \\d{5}-\\d{4})$");
    private static final Pattern FIXO =
            Pattern.compile("^(\\d{10}|\\(\\d{2}\\) \\d{4}-\\d{4})$");

    @Override
    public boolean isValid(TelefoneRequest telefone, ConstraintValidatorContext context) {
        if (telefone == null || telefone.getTipo() == null
                || telefone.getNumero() == null || telefone.getNumero().trim().isEmpty()) {
            return true; // obrigatoriedade fica a cargo de @NotNull/@NotBlank nos campos
        }

        String numero = telefone.getNumero().trim();
        int digitos = Mascaras.somenteDigitos(numero).length();
        boolean formatoValido = telefone.getTipo() == TipoTelefone.CELULAR
                ? CELULAR.matcher(numero).matches()
                : FIXO.matcher(numero).matches();
        boolean valido = formatoValido && (telefone.getTipo() == TipoTelefone.CELULAR ? digitos == 11 : digitos == 10);

        if (!valido) {
            context.disableDefaultConstraintViolation();
            String mensagem = telefone.getTipo() == TipoTelefone.CELULAR
                    ? "Telefone celular deve ter 11 dígitos (DDD + 9 dígitos)"
                    : "Telefone " + telefone.getTipo().name().toLowerCase() + " deve ter 10 dígitos (DDD + 8 dígitos)";
            context.buildConstraintViolationWithTemplate(mensagem)
                    .addPropertyNode("numero")
                    .addConstraintViolation();
        }
        return valido;
    }
}
