package br.com.cooperativa.votacao.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Item de uma tela do tipo SELECAO. Representa uma opcao navegavel.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScreenItem(String texto, String url, Map<String, Object> body) {
}
