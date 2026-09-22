package br.com.cooperativa.votacao.sessao.dto;

/**
 * Payload para abertura de sessao de votacao. Quando duracaoEmSegundos for
 * nulo, sera aplicado o padrao de 60 segundos.
 */
public record AbrirSessaoRequest(Integer duracaoEmSegundos) {
}
