package br.com.sea.desafio.exception;

public class CpfDuplicadoException extends RuntimeException {

    public CpfDuplicadoException() {
        super("Já existe um cliente cadastrado com este CPF");
    }
}
