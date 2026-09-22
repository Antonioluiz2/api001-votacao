package br.com.cooperativa.votacao.sessao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.cooperativa.votacao.exception.RequisicaoInvalidaException;
import br.com.cooperativa.votacao.exception.SessaoAlreadyExistsException;
import br.com.cooperativa.votacao.exception.SessaoNotFoundException;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import br.com.cooperativa.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.cooperativa.votacao.sessao.repository.SessaoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PautaService pautaService;

    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    private SessaoService service() {
        return new SessaoService(sessaoRepository, pautaService, clock);
    }

    private Pauta pautaComId(long id) {
        Pauta pauta = new Pauta("Descricao", AGORA);
        ReflectionTestUtils.setField(pauta, "id", id);
        return pauta;
    }

    @Test
    void deveAbrirSessaoComDuracaoPadraoDe60SegundosQuandoNaoInformada() {
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pautaComId(1L));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SessaoVotacao sessao = service().abrirSessao(1L, new AbrirSessaoRequest(null));

        assertThat(sessao.getInicio()).isEqualTo(AGORA);
        assertThat(sessao.getFim()).isEqualTo(AGORA.plusSeconds(60));
    }

    @Test
    void deveAbrirSessaoComDuracaoCustomizada() {
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pautaComId(1L));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SessaoVotacao sessao = service().abrirSessao(1L, new AbrirSessaoRequest(120));

        assertThat(sessao.getFim()).isEqualTo(AGORA.plusSeconds(120));
    }

    @Test
    void deveRejeitarDuracaoZero() {
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pautaComId(1L));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(false);

        assertThatThrownBy(() -> service().abrirSessao(1L, new AbrirSessaoRequest(0)))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }

    @Test
    void deveRejeitarDuracaoNegativa() {
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pautaComId(1L));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(false);

        assertThatThrownBy(() -> service().abrirSessao(1L, new AbrirSessaoRequest(-10)))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }

    @Test
    void devePropagarErroQuandoPautaNaoExiste() {
        when(pautaService.buscarPautaOuFalhar(99L))
                .thenThrow(new br.com.cooperativa.votacao.exception.PautaNotFoundException(99L));

        assertThatThrownBy(() -> service().abrirSessao(99L, new AbrirSessaoRequest(null)))
                .isInstanceOf(br.com.cooperativa.votacao.exception.PautaNotFoundException.class);
    }

    @Test
    void deveRejeitarAberturaDeSegundaSessaoParaMesmaPauta() {
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pautaComId(1L));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service().abrirSessao(1L, new AbrirSessaoRequest(null)))
                .isInstanceOf(SessaoAlreadyExistsException.class);
    }

    @Test
    void deveLancarExcecaoQuandoSessaoNaoExisteAoBuscar() {
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().buscarSessaoOuFalhar(1L))
                .isInstanceOf(SessaoNotFoundException.class);
    }
}
