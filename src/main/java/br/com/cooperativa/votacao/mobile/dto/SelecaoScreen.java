package br.com.cooperativa.votacao.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Tela do tipo SELECAO, conforme protocolo do Anexo 1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SelecaoScreen(String tipo, String titulo, List<ScreenItem> itens) {

    public SelecaoScreen(String titulo, List<ScreenItem> itens) {
        this("SELECAO", titulo, itens);
    }
}
