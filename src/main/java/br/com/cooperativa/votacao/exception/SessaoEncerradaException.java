package br.com.cooperativa.votacao.exception;

public class SessaoEncerradaException extends RuntimeException {

    public SessaoEncerradaException(String message) {
        super(message);
    }
}
