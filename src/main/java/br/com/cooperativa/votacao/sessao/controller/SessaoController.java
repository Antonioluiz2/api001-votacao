package br.com.cooperativa.votacao.sessao.controller;

import br.com.cooperativa.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.cooperativa.votacao.sessao.dto.SessaoResponse;
import br.com.cooperativa.votacao.sessao.service.SessaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/sessao")
@Tag(name = "Sessoes", description = "Abertura de sessoes de votacao")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    @Operation(summary = "Abre uma sessao de votacao para a pauta")
    @ApiResponse(responseCode = "201", description = "Sessao aberta com sucesso")
    @ApiResponse(responseCode = "404", description = "Pauta nao encontrada")
    @ApiResponse(responseCode = "409", description = "Pauta ja possui sessao")
    public ResponseEntity<SessaoResponse> abrir(
            @PathVariable Long pautaId, @RequestBody(required = false) AbrirSessaoRequest request) {
        AbrirSessaoRequest payload = request != null ? request : new AbrirSessaoRequest(null);
        var sessao = sessaoService.abrirSessao(pautaId, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(SessaoResponse.from(sessao));
    }
}
