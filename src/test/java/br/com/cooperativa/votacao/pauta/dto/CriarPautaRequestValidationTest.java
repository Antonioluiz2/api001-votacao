package br.com.cooperativa.votacao.pauta.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CriarPautaRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void deveAceitarDescricaoValida() {
        Set<ConstraintViolation<CriarPautaRequest>> violacoes =
                validator.validate(new CriarPautaRequest("Aprovacao do estatuto"));

        assertThat(violacoes).isEmpty();
    }

    @Test
    void deveRejeitarDescricaoNula() {
        Set<ConstraintViolation<CriarPautaRequest>> violacoes = validator.validate(new CriarPautaRequest(null));

        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void deveRejeitarDescricaoVazia() {
        Set<ConstraintViolation<CriarPautaRequest>> violacoes = validator.validate(new CriarPautaRequest(""));

        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void deveRejeitarDescricaoComApenasEspacos() {
        Set<ConstraintViolation<CriarPautaRequest>> violacoes = validator.validate(new CriarPautaRequest("   "));

        assertThat(violacoes).isNotEmpty();
    }
}
