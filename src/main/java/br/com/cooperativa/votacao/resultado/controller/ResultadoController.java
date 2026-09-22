package br.com.cooperativa.votacao.resultado.controller;

import br.com.cooperativa.votacao.resultado.dto.ResultadoResponse;
import br.com.cooperativa.votacao.resultado.service.ResultadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/resultado")
@Tag(name = "Resultado", description = "Consulta o resultado da votacao")
public class ResultadoController {

    private final ResultadoService resultadoService;

    public ResultadoController(ResultadoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @GetMapping
    @Operation(summary = "Consulta o resultado (parcial ou final) da votacao de uma pauta")
    @ApiResponse(responseCode = "200", description = "Resultado calculado com sucesso")
    @ApiResponse(responseCode = "404", description = "Pauta nao encontrada")
    public ResponseEntity<ResultadoResponse> obter(@PathVariable Long pautaId) {
        return ResponseEntity.ok(resultadoService.obterResultado(pautaId));
    }
}
