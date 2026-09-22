package br.com.cooperativa.votacao.resultado.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.resultado.dto.ResultadoResponse;
import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.repository.VotoRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ResultadoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Test
    void deveCalcularResultadoComSimNaoETotal() {
        Pauta pauta = new Pauta("Descricao", Instant.parse("2026-01-01T10:00:00Z"));
        ReflectionTestUtils.setField(pauta, "id", 1L);
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pauta);
        when(votoRepository.countByPautaIdAndTipo(1L, TipoVoto.SIM)).thenReturn(10L);
        when(votoRepository.countByPautaIdAndTipo(1L, TipoVoto.NAO)).thenReturn(5L);

        ResultadoService service = new ResultadoService(votoRepository, pautaService);
        ResultadoResponse resultado = service.obterResultado(1L);

        assertThat(resultado.pautaId()).isEqualTo(1L);
        assertThat(resultado.sim()).isEqualTo(10L);
        assertThat(resultado.nao()).isEqualTo(5L);
        assertThat(resultado.total()).isEqualTo(15L);
    }

    @Test
    void deveRetornarZeroQuandoNaoHaVotos() {
        Pauta pauta = new Pauta("Descricao", Instant.parse("2026-01-01T10:00:00Z"));
        ReflectionTestUtils.setField(pauta, "id", 2L);
        when(pautaService.buscarPautaOuFalhar(2L)).thenReturn(pauta);
        when(votoRepository.countByPautaIdAndTipo(2L, TipoVoto.SIM)).thenReturn(0L);
        when(votoRepository.countByPautaIdAndTipo(2L, TipoVoto.NAO)).thenReturn(0L);

        ResultadoService service = new ResultadoService(votoRepository, pautaService);
        ResultadoResponse resultado = service.obterResultado(2L);

        assertThat(resultado.total()).isZero();
    }
}
