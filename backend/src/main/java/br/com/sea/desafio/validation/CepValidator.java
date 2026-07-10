package br.com.sea.desafio.validation;

import br.com.sea.desafio.util.Mascaras;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Aceita CEP com ou sem máscara; após normalização deve ter exatamente 8 dígitos.
 */
public class CepValidator implements ConstraintValidator<Cep, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.trim().isEmpty()) {
            return true; // obrigatoriedade fica a cargo do @NotBlank
        }
        String entrada = valor.trim();
        return Mascaras.CEP_FORMATO_ACEITO.matcher(entrada).matches()
                && Mascaras.somenteDigitos(entrada).length() == 8;
    }
}
