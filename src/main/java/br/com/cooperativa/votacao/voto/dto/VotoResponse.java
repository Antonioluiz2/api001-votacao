package br.com.cooperativa.votacao.voto.dto;

import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.domain.Voto;
import java.time.Instant;

public record VotoResponse(Long id, Long pautaId, String associadoId, TipoVoto tipo, Instant dataHora) {

    public static VotoResponse from(Voto voto) {
        return new VotoResponse(
                voto.getId(),
                voto.getPauta().getId(),
                voto.getAssociadoId(),
                voto.getTipo(),
                voto.getDataHora());
    }
}
