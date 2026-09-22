package br.com.cooperativa.votacao.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centraliza o relogio da aplicacao para permitir testes deterministicos
 * de regras de negocio sensiveis a tempo (abertura/encerramento de sessao).
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
