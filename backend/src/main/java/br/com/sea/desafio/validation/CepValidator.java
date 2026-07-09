package br.com.sea.desafio.validation;

import br.com.sea.desafio.util.Mascaras;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Aceita CEP com ou sem máscara; após normalização deve ter exatamente 8 dígitos.
 */
public class CepValidator implements ConstraintValidator<Cep, String> {

    private static final Pattern FORMATO_ACEITO = Pattern.compile("^(\\d{8}|\\d{5}-\\d{3})$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.trim().isEmpty()) {
            return true; // obrigatoriedade fica a cargo do @NotBlank
        }
        String entrada = valor.trim();
        return FORMATO_ACEITO.matcher(entrada).matches()
                && Mascaras.somenteDigitos(entrada).length() == 8;
    }
}
