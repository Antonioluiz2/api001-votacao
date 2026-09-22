package br.com.cooperativa.votacao.voto.controller;

import br.com.cooperativa.votacao.voto.dto.RegistrarVotoRequest;
import br.com.cooperativa.votacao.voto.dto.VotoResponse;
import br.com.cooperativa.votacao.voto.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
@Tag(name = "Votos", description = "Registro de votos de associados")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    @Operation(summary = "Registra o voto de um associado em uma pauta")
    @ApiResponse(responseCode = "201", description = "Voto registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    @ApiResponse(responseCode = "404", description = "Pauta ou sessao nao encontrada")
    @ApiResponse(responseCode = "409", description = "Sessao encerrada ou voto duplicado")
    public ResponseEntity<VotoResponse> votar(
            @PathVariable Long pautaId, @Valid @RequestBody RegistrarVotoRequest request) {
        var voto = votoService.registrarVoto(pautaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VotoResponse.from(voto));
    }
}
