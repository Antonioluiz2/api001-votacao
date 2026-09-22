package br.com.cooperativa.votacao.voto.dto;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegistrarVotoRequestValidationTest {

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
    void deveAceitarVotoValido() {
        Set<ConstraintViolation<RegistrarVotoRequest>> violacoes =
                validator.validate(new RegistrarVotoRequest("12345678909", TipoVoto.SIM));

        assertThat(violacoes).isEmpty();
    }

    @Test
    void deveRejeitarAssociadoIdVazio() {
        Set<ConstraintViolation<RegistrarVotoRequest>> violacoes =
                validator.validate(new RegistrarVotoRequest("", TipoVoto.SIM));

        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void deveRejeitarTipoNulo() {
        Set<ConstraintViolation<RegistrarVotoRequest>> violacoes =
                validator.validate(new RegistrarVotoRequest("123", null));

        assertThat(violacoes).isNotEmpty();
    }
}
