package br.com.cooperativa.votacao.exception;

public class SessaoNotFoundException extends RuntimeException {

    public SessaoNotFoundException(Long pautaId) {
        super("Sessao de votacao nao encontrada para a pauta: " + pautaId);
    }
}
