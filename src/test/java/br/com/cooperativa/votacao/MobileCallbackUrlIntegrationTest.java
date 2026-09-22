package br.com.cooperativa.votacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Confirma que as URLs de callback retornadas nas telas mobile refletem a
 * configuracao externa {@code app.callback.base-url} (via
 * {@code CALLBACK_BASE_URL}), e nao valores hardcoded.
 */
@TestPropertySource(properties = "app.callback.base-url=http://dominio-customizado.exemplo.com")
class MobileCallbackUrlIntegrationTest extends IntegrationTestBase {

    @Test
    void telaInicioDeveUsarBaseUrlConfigurada() throws Exception {
        mockMvc.perform(post("/api/v1/mobile/inicio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].url")
                        .value(org.hamcrest.Matchers.startsWith("http://dominio-customizado.exemplo.com")));
    }

    @Test
    void telaNovaPautaDeveUsarBaseUrlConfigurada() throws Exception {
        mockMvc.perform(post("/api/v1/mobile/pautas/nova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.botaoOk.url")
                        .value(org.hamcrest.Matchers.startsWith("http://dominio-customizado.exemplo.com")))
                .andExpect(jsonPath("$.botaoCancelar.url")
                        .value(org.hamcrest.Matchers.startsWith("http://dominio-customizado.exemplo.com")));
    }
}
