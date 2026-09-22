package br.com.cooperativa.votacao.integration.userinfo;

/**
 * Situacao de aptidao para votar retornada pelo servico externo de
 * verificacao de CPF (bonus).
 */
public enum StatusVotacao {
    ABLE_TO_VOTE,
    UNABLE_TO_VOTE
}
