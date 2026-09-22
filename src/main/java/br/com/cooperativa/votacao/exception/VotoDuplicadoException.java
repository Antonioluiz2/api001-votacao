package br.com.cooperativa.votacao.exception;

public class VotoDuplicadoException extends RuntimeException {

    public VotoDuplicadoException(String associadoId, Long pautaId) {
        super("Associado " + associadoId + " ja votou na pauta " + pautaId);
    }
}
