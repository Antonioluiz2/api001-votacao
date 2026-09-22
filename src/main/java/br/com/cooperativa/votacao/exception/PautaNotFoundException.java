package br.com.cooperativa.votacao.exception;

public class PautaNotFoundException extends RuntimeException {

    public PautaNotFoundException(Long pautaId) {
        super("Pauta nao encontrada: " + pautaId);
    }
}
