package br.com.cooperativa.votacao.voto.dto;

import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload para registro de voto de um associado em uma pauta.
 */
public record RegistrarVotoRequest(
        @NotBlank(message = "associadoId e obrigatorio") String associadoId,
        @NotNull(message = "tipo e obrigatorio, deve ser SIM ou NAO") TipoVoto tipo) {
}
