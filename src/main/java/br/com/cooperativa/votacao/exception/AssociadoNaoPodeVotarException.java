package br.com.cooperativa.votacao.exception;

/**
 * Erro usado no bonus de integracao com o servico externo de CPF quando o
 * associado nao pode votar.
 */
public class AssociadoNaoPodeVotarException extends RuntimeException {

    public AssociadoNaoPodeVotarException(String message) {
        super(message);
    }
}
