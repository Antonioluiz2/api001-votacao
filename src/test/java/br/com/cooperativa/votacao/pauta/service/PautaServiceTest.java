package br.com.cooperativa.votacao.pauta.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cooperativa.votacao.exception.PautaNotFoundException;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.dto.CriarPautaRequest;
import br.com.cooperativa.votacao.pauta.repository.PautaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private PautaRepository pautaRepository;

    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    @Test
    void deveCriarPautaComDataDeCriacaoDoRelogioConfigurado() {
        PautaService service = new PautaService(pautaRepository, clock);
        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pauta pauta = service.criarPauta(new CriarPautaRequest("Aprovacao do estatuto"));

        assertThat(pauta.getDescricao()).isEqualTo("Aprovacao do estatuto");
        assertThat(pauta.getDataCriacao()).isEqualTo(AGORA);

        ArgumentCaptor<Pauta> captor = ArgumentCaptor.forClass(Pauta.class);
        verify(pautaRepository).save(captor.capture());
        assertThat(captor.getValue().getDescricao()).isEqualTo("Aprovacao do estatuto");
    }

    @Test
    void deveRemoverEspacosEmBrancoDaDescricao() {
        PautaService service = new PautaService(pautaRepository, clock);
        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pauta pauta = service.criarPauta(new CriarPautaRequest("  Reforma do estatuto  "));

        assertThat(pauta.getDescricao()).isEqualTo("Reforma do estatuto");
    }

    @Test
    void deveLancarExcecaoQuandoPautaNaoExiste() {
        PautaService service = new PautaService(pautaRepository, clock);
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                PautaNotFoundException.class, () -> service.buscarPautaOuFalhar(99L));
    }

    @Test
    void deveRetornarPautaQuandoExiste() {
        PautaService service = new PautaService(pautaRepository, clock);
        Pauta pauta = new Pauta("Descricao", AGORA);
        ReflectionTestUtils.setField(pauta, "id", 1L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        Pauta encontrada = service.buscarPautaOuFalhar(1L);

        assertThat(encontrada.getId()).isEqualTo(1L);
    }
}
