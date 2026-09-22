package br.com.cooperativa.votacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuracao da URL base usada para montar callbacks
 * (URLs absolutas) enviados ao cliente mobile. Nunca deve ser hardcoded.
 */
@ConfigurationProperties(prefix = "app.callback")
public class CallbackProperties {

    private String baseUrl = "http://localhost:8080";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
