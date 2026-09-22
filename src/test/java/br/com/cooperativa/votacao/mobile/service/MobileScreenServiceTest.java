package br.com.cooperativa.votacao.mobile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.cooperativa.votacao.config.CallbackProperties;
import br.com.cooperativa.votacao.mobile.dto.FormularioScreen;
import br.com.cooperativa.votacao.mobile.dto.SelecaoScreen;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.resultado.dto.ResultadoResponse;
import br.com.cooperativa.votacao.resultado.service.ResultadoService;
import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import br.com.cooperativa.votacao.sessao.service.SessaoService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MobileScreenServiceTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private PautaService pautaService;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private ResultadoService resultadoService;

    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    private MobileScreenService service(String baseUrl) {
        CallbackProperties properties = new CallbackProperties();
        properties.setBaseUrl(baseUrl);
        return new MobileScreenService(pautaService, sessaoService, resultadoService, properties, clock);
    }

    private Pauta pautaComId(long id) {
        Pauta pauta = new Pauta("Descricao da pauta", AGORA);
        ReflectionTestUtils.setField(pauta, "id", id);
        return pauta;
    }

    @Test
    void telaInicioDeveSerDoTipoSelecao() {
        when(pautaService.listarPautas()).thenReturn(List.of(pautaComId(1L)));

        SelecaoScreen tela = service("http://localhost:8080").telaInicio();

        assertThat(tela.tipo()).isEqualTo("SELECAO");
        assertThat(tela.itens()).hasSize(2);
        assertThat(tela.itens().get(0).url()).isEqualTo("http://localhost:8080/api/v1/mobile/pautas/1/acoes");
    }

    @Test
    void telaNovaPautaDeveSerDoTipoFormularioComCampoDescricao() {
        FormularioScreen tela = service("http://localhost:8080").telaNovaPauta();

        assertThat(tela.tipo()).isEqualTo("FORMULARIO");
        assertThat(tela.itens()).hasSize(1);
        assertThat(tela.itens().get(0).id()).isEqualTo("descricao");
        assertThat(tela.botaoOk().url()).isEqualTo("http://localhost:8080/api/v1/pautas");
    }

    @Test
    void urlsDevemRespeitarCallbackBaseUrlConfigurado() {
        FormularioScreen tela = service("http://dominio-customizado.com").telaNovaPauta();

        assertThat(tela.botaoOk().url()).startsWith("http://dominio-customizado.com");
    }

    @Test
    void telaVotarDeveTerDoisBotoesComBodyFixo() {
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pautaComId(1L));

        FormularioScreen tela = service("http://localhost:8080").telaVotarFormulario(1L);

        assertThat(tela.itens().get(0).id()).isEqualTo("associadoId");
        assertThat(tela.botaoOk().texto()).isEqualTo("Sim");
        assertThat(tela.botaoOk().body()).containsEntry("tipo", "SIM");
        assertThat(tela.botaoCancelar().texto()).isEqualTo("Nao");
        assertThat(tela.botaoCancelar().body()).containsEntry("tipo", "NAO");
        assertThat(tela.botaoOk().url()).isEqualTo(tela.botaoCancelar().url());
    }

    @Test
    void telaResultadoDeveExibirSimNaoETotal() {
        when(resultadoService.obterResultado(1L)).thenReturn(new ResultadoResponse(1L, 10L, 5L, 15L));

        FormularioScreen tela = service("http://localhost:8080").telaResultado(1L);

        assertThat(tela.itens()).extracting("valor").containsExactly("10", "5", "15");
    }

    @Test
    void telaAcoesDeveOferecerAbrirSessaoQuandoNaoExisteSessao() {
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pautaComId(1L));
        when(sessaoService.existeSessao(1L)).thenReturn(false);

        SelecaoScreen tela = service("http://localhost:8080").telaAcoesPauta(1L);

        assertThat(tela.itens()).anySatisfy(item ->
                assertThat(item.url()).contains("/abrir-sessao/formulario"));
    }

    @Test
    void telaAcoesDeveOferecerVotarQuandoSessaoAberta() {
        Pauta pauta = pautaComId(1L);
        when(pautaService.buscarPautaOuFalhar(1L)).thenReturn(pauta);
        when(sessaoService.existeSessao(1L)).thenReturn(true);
        when(sessaoService.buscarSessaoOuFalhar(1L))
                .thenReturn(new SessaoVotacao(pauta, AGORA.minusSeconds(10), AGORA.plusSeconds(50)));

        SelecaoScreen tela = service("http://localhost:8080").telaAcoesPauta(1L);

        assertThat(tela.itens()).anySatisfy(item -> assertThat(item.url()).contains("/votar/formulario"));
    }
}
