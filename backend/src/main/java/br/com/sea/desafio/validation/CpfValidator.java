package br.com.sea.desafio.validation;

import br.com.sea.desafio.util.Mascaras;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Valida o CPF pelos dígitos verificadores. Aceita entrada com ou sem máscara.
 */
public class CpfValidator implements ConstraintValidator<Cpf, String> {

    private static final Pattern FORMATO_ACEITO =
            Pattern.compile("^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.trim().isEmpty()) {
            return true; // obrigatoriedade fica a cargo do @NotBlank
        }

        String entrada = valor.trim();
        if (!FORMATO_ACEITO.matcher(entrada).matches()) {
            return false;
        }

        String cpf = Mascaras.somenteDigitos(entrada);
        if (cpf.length() != 11) {
            return false;
        }

        // CPFs com todos os dígitos iguais (ex.: 111.111.111-11) passam no cálculo, mas são inválidos
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        return digitoVerificador(cpf, 9) == cpf.charAt(9) - '0'
                && digitoVerificador(cpf, 10) == cpf.charAt(10) - '0';
    }

    private int digitoVerificador(String cpf, int posicao) {
        int soma = 0;
        int peso = posicao + 1;
        for (int i = 0; i < posicao; i++) {
            soma += (cpf.charAt(i) - '0') * (peso - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
