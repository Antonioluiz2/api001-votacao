package br.com.cooperativa.votacao.integration.userinfo;

/**
 * Resposta do servico externo de verificacao de CPF.
 */
public record UserInfoResponse(StatusVotacao status) {
}
