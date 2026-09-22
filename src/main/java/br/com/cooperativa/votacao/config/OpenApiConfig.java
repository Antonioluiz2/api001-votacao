package br.com.cooperativa.votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI votacaoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Votacao Cooperativista")
                        .version("v1")
                        .description("API REST para gerenciamento de pautas, sessoes de votacao e votos de "
                                + "associados em uma cooperativa. Inclui protocolo de telas mobile "
                                + "(FORMULARIO/SELECAO).")
                        .contact(new Contact().name("Equipe de Desenvolvimento")));
    }
}
