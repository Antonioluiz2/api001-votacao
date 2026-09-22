package br.com.cooperativa.votacao.integration.userinfo;

import br.com.cooperativa.votacao.exception.AssociadoNaoPodeVotarException;
import br.com.cooperativa.votacao.exception.RequisicaoInvalidaException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Regras de negocio do bonus de integracao com o servico externo de
 * verificacao de CPF. Desabilitado por padrao ({@code app.userinfo.enabled=false})
 * para nao impactar o fluxo obrigatorio de votacao, que aceita qualquer
 * identificador de associado (nao necessariamente um CPF).
 */
@Service
public class UserInfoService {

    private static final Logger log = LoggerFactory.getLogger(UserInfoService.class);

    private final UserInfoClient userInfoClient;
    private final boolean enabled;

    public UserInfoService(
            UserInfoClient userInfoClient,
            @Value("${app.userinfo.enabled:false}") boolean enabled) {
        this.userInfoClient = userInfoClient;
        this.enabled = enabled;
    }

    public void validarAssociadoPodeVotar(String associadoId) {
        if (!enabled) {
            return;
        }

        Optional<UserInfoResponse> resposta = userInfoClient.consultar(associadoId);
        if (resposta.isEmpty()) {
            log.warn("CPF invalido segundo servico externo: associadoId={}", associadoId);
            throw new RequisicaoInvalidaException("CPF invalido para o associado informado");

        }

        if (resposta.get().status() == StatusVotacao.UNABLE_TO_VOTE) {
            log.warn("Associado impedido de votar segundo servico externo: associadoId={}", associadoId);
            throw new AssociadoNaoPodeVotarException("Associado nao esta habilitado a votar");
        }
    }
}
