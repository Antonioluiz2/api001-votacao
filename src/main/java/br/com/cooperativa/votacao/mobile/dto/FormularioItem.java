package br.com.cooperativa.votacao.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Item de uma tela do tipo FORMULARIO. Pode representar um campo de entrada
 * (com {@code id} correspondente a chave usada no body final) ou um item de
 * exibicao apenas de texto (tipo TEXTO), usado por exemplo na tela de
 * resultado.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FormularioItem(String id, String tipo, String label, String valor, Boolean obrigatorio) {
}
