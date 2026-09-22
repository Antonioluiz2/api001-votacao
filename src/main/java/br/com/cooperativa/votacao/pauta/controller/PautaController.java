package br.com.cooperativa.votacao.pauta.controller;

import br.com.cooperativa.votacao.pauta.dto.CriarPautaRequest;
import br.com.cooperativa.votacao.pauta.dto.PautaResponse;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Pautas", description = "Cadastro de pautas de votacao")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova pauta")
    @ApiResponse(responseCode = "201", description = "Pauta criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody CriarPautaRequest request) {
        var pauta = pautaService.criarPauta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PautaResponse.from(pauta));
    }

    @GetMapping("/{pautaId}")
    @Operation(summary = "Consulta uma pauta pelo id")
    @ApiResponse(responseCode = "200", description = "Pauta encontrada")
    @ApiResponse(responseCode = "404", description = "Pauta nao encontrada")
    public ResponseEntity<PautaResponse> buscar(@PathVariable Long pautaId) {
        var pauta = pautaService.buscarPautaOuFalhar(pautaId);
        return ResponseEntity.ok(PautaResponse.from(pauta));
    }
}
