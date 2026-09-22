package br.com.cooperativa.votacao.mobile.controller;

import br.com.cooperativa.votacao.mobile.dto.FormularioScreen;
import br.com.cooperativa.votacao.mobile.dto.SelecaoScreen;
import br.com.cooperativa.votacao.mobile.service.MobileScreenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de navegacao mobile. Retornam exclusivamente telas do protocolo
 * do Anexo 1 (FORMULARIO/SELECAO), permitindo que o cliente mobile monte e
 * navegue pelo fluxo completo do aplicativo usando apenas estas respostas.
 */
@RestController
@RequestMapping("/api/v1/mobile")
@Tag(name = "Mobile", description = "Protocolo de telas mobile (FORMULARIO/SELECAO)")
public class MobileController {

    private final MobileScreenService mobileScreenService;

    public MobileController(MobileScreenService mobileScreenService) {
        this.mobileScreenService = mobileScreenService;
    }

    @PostMapping("/inicio")
    @Operation(summary = "Tela inicial com a lista de pautas (SELECAO)")
    public SelecaoScreen inicio() {
        return mobileScreenService.telaInicio();
    }

    @PostMapping("/pautas/nova")
    @Operation(summary = "Tela de cadastro de nova pauta (FORMULARIO)")
    public FormularioScreen novaPauta() {
        return mobileScreenService.telaNovaPauta();
    }

    @PostMapping("/pautas/{pautaId}/acoes")
    @Operation(summary = "Tela de acoes disponiveis para a pauta (SELECAO)")
    public SelecaoScreen acoes(@PathVariable Long pautaId) {
        return mobileScreenService.telaAcoesPauta(pautaId);
    }

    @PostMapping("/pautas/{pautaId}/abrir-sessao/formulario")
    @Operation(summary = "Tela de abertura de sessao de votacao (FORMULARIO)")
    public FormularioScreen abrirSessaoFormulario(@PathVariable Long pautaId) {
        return mobileScreenService.telaAbrirSessaoFormulario(pautaId);
    }

    @PostMapping("/pautas/{pautaId}/votar/formulario")
    @Operation(summary = "Tela de votacao do associado (FORMULARIO)")
    public FormularioScreen votarFormulario(@PathVariable Long pautaId) {
        return mobileScreenService.telaVotarFormulario(pautaId);
    }

    @PostMapping("/pautas/{pautaId}/resultado")
    @Operation(summary = "Tela de resultado da votacao (FORMULARIO)")
    public FormularioScreen resultado(@PathVariable Long pautaId) {
        return mobileScreenService.telaResultado(pautaId);
    }
}
