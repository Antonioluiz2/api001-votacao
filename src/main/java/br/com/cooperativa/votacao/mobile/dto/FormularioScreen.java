package br.com.cooperativa.votacao.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Tela do tipo FORMULARIO, conforme protocolo do Anexo 1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FormularioScreen(
        String tipo,
        String titulo,
        List<FormularioItem> itens,
        ActionButton botaoOk,
        ActionButton botaoCancelar) {

    public FormularioScreen(String titulo, List<FormularioItem> itens, ActionButton botaoOk,
            ActionButton botaoCancelar) {
        this("FORMULARIO", titulo, itens, botaoOk, botaoCancelar);
    }
}
