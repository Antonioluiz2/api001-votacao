package br.com.cooperativa.votacao.mobile.service;

import br.com.cooperativa.votacao.config.CallbackProperties;
import br.com.cooperativa.votacao.mobile.dto.ActionButton;
import br.com.cooperativa.votacao.mobile.dto.FormularioItem;
import br.com.cooperativa.votacao.mobile.dto.FormularioScreen;
import br.com.cooperativa.votacao.mobile.dto.ScreenItem;
import br.com.cooperativa.votacao.mobile.dto.SelecaoScreen;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.resultado.dto.ResultadoResponse;
import br.com.cooperativa.votacao.resultado.service.ResultadoService;
import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import br.com.cooperativa.votacao.sessao.service.SessaoService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Monta as telas de navegacao mobile (FORMULARIO/SELECAO) conforme o
 * protocolo do Anexo 1, delegando as regras de negocio aos services de
 * dominio e usando {@link CallbackProperties} para montar URLs absolutas.
 */
@Service
public class MobileScreenService {

    private static final Logger log = LoggerFactory.getLogger(MobileScreenService.class);

    private final PautaService pautaService;
    private final SessaoService sessaoService;
    private final ResultadoService resultadoService;
    private final CallbackProperties callbackProperties;
    private final Clock clock;

    public MobileScreenService(
            PautaService pautaService,
            SessaoService sessaoService,
            ResultadoService resultadoService,
            CallbackProperties callbackProperties,
            Clock clock) {
        this.pautaService = pautaService;
        this.sessaoService = sessaoService;
        this.resultadoService = resultadoService;
        this.callbackProperties = callbackProperties;
        this.clock = clock;
    }

    public SelecaoScreen telaInicio() {
        log.debug("Renderizando tela mobile: inicio");
        List<ScreenItem> itens = pautaService.listarPautas().stream()
                .map(pauta -> new ScreenItem(
                        "Pauta #" + pauta.getId() + " - " + pauta.getDescricao(),
                        url("/api/v1/mobile/pautas/" + pauta.getId() + "/acoes"),
                        Map.of()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        itens.add(new ScreenItem("Cadastrar nova pauta", url("/api/v1/mobile/pautas/nova"), Map.of()));

        return new SelecaoScreen("Pautas da cooperativa", itens);
    }

    public FormularioScreen telaNovaPauta() {
        log.debug("Renderizando tela mobile: nova pauta");
        List<FormularioItem> itens = List.of(
                new FormularioItem("descricao", "TEXTO", "Descricao da pauta", null, true));

        ActionButton botaoOk = new ActionButton("Cadastrar", url("/api/v1/pautas"), Map.of());
        ActionButton botaoCancelar = new ActionButton("Cancelar", url("/api/v1/mobile/inicio"), Map.of());

        return new FormularioScreen("Nova pauta", itens, botaoOk, botaoCancelar);
    }

    public SelecaoScreen telaAcoesPauta(Long pautaId) {
        log.debug("Renderizando tela mobile: acoes da pauta {}", pautaId);
        Pauta pauta = pautaService.buscarPautaOuFalhar(pautaId);

        List<ScreenItem> itens = new java.util.ArrayList<>();

        if (!sessaoService.existeSessao(pautaId)) {
            itens.add(new ScreenItem(
                    "Abrir sessao de votacao",
                    url("/api/v1/mobile/pautas/" + pautaId + "/abrir-sessao/formulario"),
                    Map.of()));
        } else {
            SessaoVotacao sessao = sessaoService.buscarSessaoOuFalhar(pautaId);
            if (sessao.estaAberta(Instant.now(clock))) {
                itens.add(new ScreenItem(
                        "Votar",
                        url("/api/v1/mobile/pautas/" + pautaId + "/votar/formulario"),
                        Map.of()));
            }
        }

        itens.add(new ScreenItem(
                "Ver resultado", url("/api/v1/mobile/pautas/" + pautaId + "/resultado"), Map.of()));
        itens.add(new ScreenItem("Voltar", url("/api/v1/mobile/inicio"), Map.of()));

        return new SelecaoScreen("Pauta #" + pauta.getId() + " - " + pauta.getDescricao(), itens);
    }

    public FormularioScreen telaAbrirSessaoFormulario(Long pautaId) {
        log.debug("Renderizando tela mobile: abrir sessao da pauta {}", pautaId);
        pautaService.buscarPautaOuFalhar(pautaId);

        List<FormularioItem> itens = List.of(
                new FormularioItem(
                        "duracaoEmSegundos",
                        "TEXTO",
                        "Duracao em segundos (opcional, padrao 60)",
                        null,
                        false));

        ActionButton botaoOk = new ActionButton(
                "Abrir sessao", url("/api/v1/pautas/" + pautaId + "/sessao"), Map.of());
        ActionButton botaoCancelar = new ActionButton(
                "Cancelar", url("/api/v1/mobile/pautas/" + pautaId + "/acoes"), Map.of());

        return new FormularioScreen("Abrir sessao de votacao", itens, botaoOk, botaoCancelar);
    }

    public FormularioScreen telaVotarFormulario(Long pautaId) {
        log.debug("Renderizando tela mobile: votar na pauta {}", pautaId);
        pautaService.buscarPautaOuFalhar(pautaId);

        List<FormularioItem> itens = List.of(
                new FormularioItem("associadoId", "TEXTO", "Identificacao do associado", null, true));

        String urlVoto = url("/api/v1/pautas/" + pautaId + "/votos");
        ActionButton botaoOk = new ActionButton("Sim", urlVoto, Map.of("tipo", "SIM"));
        ActionButton botaoCancelar = new ActionButton("Nao", urlVoto, Map.of("tipo", "NAO"));

        return new FormularioScreen("Votar na pauta", itens, botaoOk, botaoCancelar);
    }

    public FormularioScreen telaResultado(Long pautaId) {
        log.debug("Renderizando tela mobile: resultado da pauta {}", pautaId);
        ResultadoResponse resultado = resultadoService.obterResultado(pautaId);

        List<FormularioItem> itens = List.of(
                new FormularioItem(null, "TEXTO", "Votos SIM", String.valueOf(resultado.sim()), null),
                new FormularioItem(null, "TEXTO", "Votos NAO", String.valueOf(resultado.nao()), null),
                new FormularioItem(null, "TEXTO", "Total de votos", String.valueOf(resultado.total()), null));

        ActionButton botaoOk = new ActionButton(
                "Voltar", url("/api/v1/mobile/pautas/" + pautaId + "/acoes"), Map.of());

        return new FormularioScreen("Resultado da votacao", itens, botaoOk, null);
    }

    private String url(String path) {
        return callbackProperties.getBaseUrl() + path;
    }
}
