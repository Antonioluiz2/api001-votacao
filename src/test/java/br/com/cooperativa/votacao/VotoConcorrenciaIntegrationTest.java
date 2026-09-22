package br.com.cooperativa.votacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cooperativa.votacao.pauta.dto.CriarPautaRequest;
import br.com.cooperativa.votacao.pauta.dto.PautaResponse;
import br.com.cooperativa.votacao.resultado.dto.ResultadoResponse;
import br.com.cooperativa.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.dto.RegistrarVotoRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Garante que, sob concorrencia, no maximo um voto do mesmo associado na
 * mesma pauta seja persistido, mesmo quando duas requisicoes chegam
 * praticamente ao mesmo tempo.
 */
class VotoConcorrenciaIntegrationTest extends IntegrationTestBase {

    @Test
    void deveAceitarApenasUmVotoQuandoDoisVotosConcorrentesDoMesmoAssociado() throws Exception {
        String body = mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarPautaRequest("Pauta para teste de concorrencia"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long pautaId = objectMapper.readValue(body, PautaResponse.class).id();

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(60))))
                .andExpect(status().isCreated());

        String associadoId = "99999999999";
        int totalRequisicoes = 2;
        ExecutorService executor = Executors.newFixedThreadPool(totalRequisicoes);
        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger sucesso = new AtomicInteger(0);
        AtomicInteger conflito = new AtomicInteger(0);

        String votoJson = objectMapper.writeValueAsString(new RegistrarVotoRequest(associadoId, TipoVoto.SIM));

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < totalRequisicoes; i++) {
            futures.add(executor.submit(() -> {
                partida.await();
                return mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(votoJson))
                        .andReturn().getResponse().getStatus();
            }));
        }

        partida.countDown();

        for (Future<Integer> future : futures) {
            int status = future.get(15, TimeUnit.SECONDS);
            if (status == 201) {
                sucesso.incrementAndGet();
            } else if (status == 409) {
                conflito.incrementAndGet();
            }
        }
        executor.shutdown();

        assertThat(sucesso.get()).isEqualTo(1);
        assertThat(conflito.get()).isEqualTo(1);

        String resultadoBody = mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ResultadoResponse resultado = objectMapper.readValue(resultadoBody, ResultadoResponse.class);
        assertThat(resultado.total()).isEqualTo(1L);
    }
}
