package br.com.cooperativa.votacao.exception;

import java.time.Instant;

/**
 * Formato consistente de erro retornado pela API, sem expor detalhes
 * internos (stack trace, SQL, etc.).
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
