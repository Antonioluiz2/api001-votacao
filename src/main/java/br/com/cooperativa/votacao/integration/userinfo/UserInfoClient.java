package br.com.cooperativa.votacao.integration.userinfo;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client responsavel exclusivamente pela comunicacao HTTP com o servico
 * externo de verificacao de CPF (bonus). Nenhuma chamada externa deve ser
 * feita diretamente pelo controller ou service de dominio.
 */
@Component
public class UserInfoClient {

    private static final Logger log = LoggerFactory.getLogger(UserInfoClient.class);

    private final RestClient restClient;

    public UserInfoClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.userinfo.base-url}") String baseUrl,
            @Value("${app.userinfo.timeout-ms}") int timeoutMs) {
        ClientHttpRequestFactory requestFactory = criarRequestFactory(timeoutMs);
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    private ClientHttpRequestFactory criarRequestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return factory;
    }

    /**
     * Consulta o status de um CPF junto ao servico externo.
     *
     * @return o status retornado pelo servico, ou {@code Optional.empty()}
     *         caso o servico responda 404 (CPF invalido/nao encontrado).
     * @throws UserInfoCommunicationException em caso de falha de
     *         comunicacao (timeout, erro de rede, erro 5xx).
     */
    public java.util.Optional<UserInfoResponse> consultar(String cpf) {
        try {
            UserInfoResponse response = restClient.get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .body(UserInfoResponse.class);
            return java.util.Optional.ofNullable(response);
        } catch (HttpClientErrorException.NotFound e) {
            return java.util.Optional.empty();
        } catch (RestClientException e) {
            log.error("Falha ao consultar servico de verificacao de CPF", e);
            throw new UserInfoCommunicationException("Falha na comunicacao com o servico de verificacao de CPF", e);
        }
    }
}
