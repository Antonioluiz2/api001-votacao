package br.com.cooperativa.votacao.exception;

/**
 * Erro de validacao/regra de negocio para requisicoes invalidas (400).
 */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String message) {
        super(message);
    }
}
