package br.com.cooperativa.votacao.sessao.dto;

import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import java.time.Instant;

public record SessaoResponse(Long id, Long pautaId, Instant inicio, Instant fim) {

    public static SessaoResponse from(SessaoVotacao sessao) {
        return new SessaoResponse(
                sessao.getId(), sessao.getPauta().getId(), sessao.getInicio(), sessao.getFim());
    }
}
