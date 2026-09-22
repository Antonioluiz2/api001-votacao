package br.com.cooperativa.votacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cooperativa.votacao.pauta.dto.CriarPautaRequest;
import br.com.cooperativa.votacao.pauta.dto.PautaResponse;
import br.com.cooperativa.votacao.sessao.dto.AbrirSessaoRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Cobre a navegacao completa pelas telas mobile do Anexo 1, garantindo que
 * cada endpoint retorne a estrutura correta (FORMULARIO ou SELECAO).
 */
class MobileFluxoIntegrationTest extends IntegrationTestBase {

    @Test
    void telaInicioDeveRetornarSelecaoComOpcaoDeNovaPauta() throws Exception {
        mockMvc.perform(post("/api/v1/mobile/inicio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SELECAO"))
                .andExpect(jsonPath("$.itens[*].texto", Matchers.hasItem(Matchers.containsString("Cadastrar nova pauta"))));
    }

    @Test
    void telaNovaPautaDeveRetornarFormularioComCampoDescricao() throws Exception {
        mockMvc.perform(post("/api/v1/mobile/pautas/nova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.itens[0].id").value("descricao"))
                .andExpect(jsonPath("$.botaoOk.url", Matchers.endsWith("/api/v1/pautas")));
    }

    @Test
    void fluxoCompletoMobileDeveNavegarPorTodasAsTelas() throws Exception {
        String body = mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarPautaRequest("Pauta navegada via mobile"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long pautaId = objectMapper.readValue(body, PautaResponse.class).id();

        mockMvc.perform(post("/api/v1/mobile/pautas/{pautaId}/acoes", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].url",
                        Matchers.hasItem(Matchers.containsString("/abrir-sessao/formulario"))));

        mockMvc.perform(post("/api/v1/mobile/pautas/{pautaId}/abrir-sessao/formulario", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.itens[0].id").value("duracaoEmSegundos"));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(60))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/mobile/pautas/{pautaId}/votar/formulario", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.botaoOk.body.tipo").value("SIM"))
                .andExpect(jsonPath("$.botaoCancelar.body.tipo").value("NAO"));

        mockMvc.perform(post("/api/v1/mobile/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.itens.length()").value(3));
    }

    @Test
    void telaAcoesDevolveNotFoundParaPautaInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/mobile/pautas/{pautaId}/acoes", 777777))
                .andExpect(status().isNotFound());
    }
}
