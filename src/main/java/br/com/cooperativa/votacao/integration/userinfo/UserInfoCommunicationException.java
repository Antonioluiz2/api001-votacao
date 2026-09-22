package br.com.cooperativa.votacao.integration.userinfo;

/**
 * Erro tecnico de comunicacao com o servico externo de verificacao de CPF.
 */
public class UserInfoCommunicationException extends RuntimeException {

    public UserInfoCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
