package br.com.cooperativa.votacao.pauta.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload para criacao de pauta.
 */
public record CriarPautaRequest(
        @NotBlank(message = "descricao e obrigatoria") String descricao) {
}
