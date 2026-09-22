package br.com.cooperativa.votacao.pauta.dto;

import br.com.cooperativa.votacao.pauta.domain.Pauta;
import java.time.Instant;

public record PautaResponse(Long id, String descricao, Instant dataCriacao) {

    public static PautaResponse from(Pauta pauta) {
        return new PautaResponse(pauta.getId(), pauta.getDescricao(), pauta.getDataCriacao());
    }
}
