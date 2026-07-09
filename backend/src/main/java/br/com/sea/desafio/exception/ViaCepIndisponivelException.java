package br.com.sea.desafio.exception;

public class ViaCepIndisponivelException extends RuntimeException {

    public ViaCepIndisponivelException(Throwable causa) {
        super("Serviço de consulta de CEP indisponível no momento", causa);
    }
}
