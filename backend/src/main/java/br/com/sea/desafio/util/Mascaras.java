package br.com.sea.desafio.util;

import br.com.sea.desafio.domain.TipoTelefone;

import java.util.regex.Pattern;

/**
 * Regra do desafio: dados persistidos SEM máscara e exibidos COM máscara.
 * Este utilitário concentra a normalização (remoção) e a formatação (aplicação) das máscaras.
 */
public final class Mascaras {

    /** CEP com máscara (00000-000) ou sem (8 dígitos). */
    public static final Pattern CEP_FORMATO_ACEITO = Pattern.compile("^(\\d{8}|\\d{5}-\\d{3})$");

    private Mascaras() {
    }

    /** Remove tudo que não for dígito. Retorna null se a entrada for null. */
    public static String somenteDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }

    /** 12345678901 -> 123.456.789-01 */
    public static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    /** 70714900 -> 70714-900 */
    public static String formatarCep(String cep) {
        if (cep == null || cep.length() != 8) {
            return cep;
        }
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    /**
     * Celular (11 dígitos): 61987654321 -> (61) 98765-4321
     * Fixo (10 dígitos):    6133334444  -> (61) 3333-4444
     */
    public static String formatarTelefone(TipoTelefone tipo, String numero) {
        if (numero == null) {
            return null;
        }
        if (tipo == TipoTelefone.CELULAR && numero.length() == 11) {
            return "(" + numero.substring(0, 2) + ") " + numero.substring(2, 7) + "-" + numero.substring(7);
        }
        if (numero.length() == 10) {
            return "(" + numero.substring(0, 2) + ") " + numero.substring(2, 6) + "-" + numero.substring(6);
        }
        return numero;
    }
}
