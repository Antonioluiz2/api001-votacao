package br.com.cooperativa.votacao.integration.userinfo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import br.com.cooperativa.votacao.exception.AssociadoNaoPodeVotarException;
import br.com.cooperativa.votacao.exception.RequisicaoInvalidaException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

    private static final String CPF = "12345678909";

    @Mock
    private UserInfoClient userInfoClient;

    @Test
    void devePermitirAssociadoQuandoCpfPodeVotar() {
        when(userInfoClient.consultar(CPF))
                .thenReturn(Optional.of(new UserInfoResponse(StatusVotacao.ABLE_TO_VOTE)));

        UserInfoService service = new UserInfoService(userInfoClient, true);

        service.validarAssociadoPodeVotar(CPF);

        verify(userInfoClient).consultar(CPF);
        verifyNoMoreInteractions(userInfoClient);
    }

    @Test
    void deveRejeitarAssociadoQuandoCpfNaoPodeVotar() {
        when(userInfoClient.consultar(CPF))
                .thenReturn(Optional.of(new UserInfoResponse(StatusVotacao.UNABLE_TO_VOTE)));

        UserInfoService service = new UserInfoService(userInfoClient, true);

        assertThatThrownBy(() -> service.validarAssociadoPodeVotar(CPF))
                .isInstanceOf(AssociadoNaoPodeVotarException.class);
    }

    @Test
    void deveRejeitarQuandoCpfNaoForEncontrado() {
        when(userInfoClient.consultar(CPF)).thenReturn(Optional.empty());

        UserInfoService service = new UserInfoService(userInfoClient, true);

        assertThatThrownBy(() -> service.validarAssociadoPodeVotar(CPF))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }
}