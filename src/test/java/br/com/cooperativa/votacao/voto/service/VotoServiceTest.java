package br.com.cooperativa.votacao.voto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.cooperativa.votacao.exception.SessaoEncerradaException;
import br.com.cooperativa.votacao.exception.VotoDuplicadoException;
import br.com.cooperativa.votacao.integration.userinfo.UserInfoService;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import br.com.cooperativa.votacao.sessao.service.SessaoService;
import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.domain.Voto;
import br.com.cooperativa.votacao.voto.dto.RegistrarVotoRequest;
import br.com.cooperativa.votacao.voto.repository.VotoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private UserInfoService userInfoService;

    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    private VotoService service;
    private Pauta pauta;

    @BeforeEach
    void setUp() {
        service = new VotoService(votoRepository, pautaService, sessaoService, userInfoService, clock);
        pauta = new Pauta("Descricao", AGORA);
        ReflectionTestUtils.setField(pauta, "id", 1L);
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pauta);
    }

    private SessaoVotacao sessaoAberta() {
        return new SessaoVotacao(pauta, AGORA.minusSeconds(10), AGORA.plusSeconds(50));
    }

    @Test
    void deveRegistrarVotoSim() {
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessaoAberta());
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "12345678909")).thenReturn(false);
        when(votoRepository.saveAndFlush(any(Voto.class))).thenAnswer(inv -> inv.getArgument(0));

        Voto voto = service.registrarVoto(1L, new RegistrarVotoRequest("12345678909", TipoVoto.SIM));

        assertThat(voto.getTipo()).isEqualTo(TipoVoto.SIM);
        assertThat(voto.getAssociadoId()).isEqualTo("12345678909");
    }

    @Test
    void deveRegistrarVotoNao() {
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessaoAberta());
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "12345678909")).thenReturn(false);
        when(votoRepository.saveAndFlush(any(Voto.class))).thenAnswer(inv -> inv.getArgument(0));

        Voto voto = service.registrarVoto(1L, new RegistrarVotoRequest("12345678909", TipoVoto.NAO));

        assertThat(voto.getTipo()).isEqualTo(TipoVoto.NAO);
    }

    @Test
    void deveRejeitarVotoQuandoSessaoAindaNaoIniciou() {
        SessaoVotacao sessaoFutura = new SessaoVotacao(pauta, AGORA.plusSeconds(10), AGORA.plusSeconds(70));
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessaoFutura);

        assertThatThrownBy(() -> service.registrarVoto(1L, new RegistrarVotoRequest("123", TipoVoto.SIM)))
                .isInstanceOf(SessaoEncerradaException.class);
    }

    @Test
    void deveRejeitarVotoQuandoSessaoEncerrada() {
        SessaoVotacao sessaoEncerrada = new SessaoVotacao(pauta, AGORA.minusSeconds(120), AGORA.minusSeconds(60));
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessaoEncerrada);

        assertThatThrownBy(() -> service.registrarVoto(1L, new RegistrarVotoRequest("123", TipoVoto.SIM)))
                .isInstanceOf(SessaoEncerradaException.class);
    }

    @Test
    void deveRejeitarVotoDuplicadoPreventivamente() {
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessaoAberta());
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "123")).thenReturn(true);

        assertThatThrownBy(() -> service.registrarVoto(1L, new RegistrarVotoRequest("123", TipoVoto.SIM)))
                .isInstanceOf(VotoDuplicadoException.class);
    }

    @Test
    void deveConverterViolacaoDeConstraintEmVotoDuplicado() {
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessaoAberta());
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "123")).thenReturn(false);
        when(votoRepository.saveAndFlush(any(Voto.class)))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));

        assertThatThrownBy(() -> service.registrarVoto(1L, new RegistrarVotoRequest("123", TipoVoto.SIM)))
                .isInstanceOf(VotoDuplicadoException.class);
    }

    @Test
    void deveAceitarVotoNoLimiteExatoDoInicio() {
        SessaoVotacao sessao = new SessaoVotacao(pauta, AGORA, AGORA.plusSeconds(60));
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessao);
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "123")).thenReturn(false);
        when(votoRepository.saveAndFlush(any(Voto.class))).thenAnswer(inv -> inv.getArgument(0));

        Voto voto = service.registrarVoto(1L, new RegistrarVotoRequest("123", TipoVoto.SIM));

        assertThat(voto).isNotNull();
    }

    @Test
    void deveRejeitarVotoNoLimiteExatoDoFim() {
        SessaoVotacao sessao = new SessaoVotacao(pauta, AGORA.minusSeconds(60), AGORA);
        when(sessaoService.buscarSessaoOuFalhar(1L)).thenReturn(sessao);

        assertThatThrownBy(() -> service.registrarVoto(1L, new RegistrarVotoRequest("123", TipoVoto.SIM)))
                .isInstanceOf(SessaoEncerradaException.class);
    }
}
