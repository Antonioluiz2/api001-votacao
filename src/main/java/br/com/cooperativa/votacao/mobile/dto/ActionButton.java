package br.com.cooperativa.votacao.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Botao de acao usado nas telas mobile. A url deve ser sempre absoluta,
 * construida a partir de {@code app.callback.base-url}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActionButton(String texto, String url, Map<String, Object> body) {
}
