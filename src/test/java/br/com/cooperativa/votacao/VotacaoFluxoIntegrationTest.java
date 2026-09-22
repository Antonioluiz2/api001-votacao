package br.com.cooperativa.votacao;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cooperativa.votacao.pauta.dto.CriarPautaRequest;
import br.com.cooperativa.votacao.pauta.dto.PautaResponse;
import br.com.cooperativa.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.dto.RegistrarVotoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Cobre o fluxo principal ponta a ponta: criar pauta, abrir sessao,
 * registrar votos SIM e NAO, e consultar o resultado. Tambem cobre os
 * principais cenarios de erro exigidos pelo enunciado.
 */
class VotacaoFluxoIntegrationTest extends IntegrationTestBase {

    @Test
    void deveExecutarFluxoCompletoDeVotacaoComSucesso() throws Exception {
        Long pautaId = criarPauta("Aprovacao da reforma do estatuto");

        abrirSessao(pautaId, 60);

        votar(pautaId, "11111111111", TipoVoto.SIM)
                .andExpect(status().isCreated());
        votar(pautaId, "22222222222", TipoVoto.NAO)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sim", is(1)))
                .andExpect(jsonPath("$.nao", is(1)))
                .andExpect(jsonPath("$.total", is(2)));
    }

    @Test
    void deveRetornar404AoAbrirSessaoParaPautaInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409AoAbrirSegundaSessaoParaMesmaPauta() throws Exception {
        Long pautaId = criarPauta("Pauta com sessao unica");
        abrirSessao(pautaId, 60);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(null))))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409AoRegistrarVotoDuplicado() throws Exception {
        Long pautaId = criarPauta("Pauta com voto duplicado");
        abrirSessao(pautaId, 60);
        votar(pautaId, "33333333333", TipoVoto.SIM).andExpect(status().isCreated());

        votar(pautaId, "33333333333", TipoVoto.NAO).andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409AoVotarEmSessaoEncerrada() throws Exception {
        Long pautaId = criarPauta("Pauta com sessao curta");
        abrirSessao(pautaId, 1);

        Thread.sleep(1500);

        votar(pautaId, "44444444444", TipoVoto.SIM).andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400AoRegistrarVotoComTipoInvalido() throws Exception {
        Long pautaId = criarPauta("Pauta com voto invalido");
        abrirSessao(pautaId, 60);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"associadoId\": \"555\", \"tipo\": \"TALVEZ\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoRegistrarVotoSemAssociadoId() throws Exception {
        Long pautaId = criarPauta("Pauta com associadoId ausente");
        abrirSessao(pautaId, 60);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\": \"SIM\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404AoConsultarResultadoDePautaInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", 888888))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoCriarPautaComDescricaoVazia() throws Exception {
        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarPautaRequest("   "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dadosDevemPersistirEPermanecerConsistentesAposNovaConsulta() throws Exception {
        Long pautaId = criarPauta("Pauta persistente");
        abrirSessao(pautaId, 60);
        votar(pautaId, "66666666666", TipoVoto.SIM).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/pautas/{pautaId}", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", is("Pauta persistente")));

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sim", is(1)));
    }

    private Long criarPauta(String descricao) throws Exception {
        String body = mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarPautaRequest(descricao))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(body, PautaResponse.class).id();
    }

    private void abrirSessao(Long pautaId, Integer duracaoEmSegundos) throws Exception {
        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(duracaoEmSegundos))))
                .andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.ResultActions votar(Long pautaId, String associadoId, TipoVoto tipo)
            throws Exception {
        return mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegistrarVotoRequest(associadoId, tipo))));
    }
}
