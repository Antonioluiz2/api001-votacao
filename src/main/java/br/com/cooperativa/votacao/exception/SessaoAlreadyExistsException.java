package br.com.cooperativa.votacao.exception;

public class SessaoAlreadyExistsException extends RuntimeException {

    public SessaoAlreadyExistsException(Long pautaId) {
        super("Ja existe uma sessao de votacao para a pauta: " + pautaId);
    }
}
