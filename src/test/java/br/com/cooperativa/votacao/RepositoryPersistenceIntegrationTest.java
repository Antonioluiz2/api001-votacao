package br.com.cooperativa.votacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.repository.PautaRepository;
import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import br.com.cooperativa.votacao.sessao.repository.SessaoRepository;
import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.domain.Voto;
import br.com.cooperativa.votacao.voto.repository.VotoRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Valida, diretamente na camada de persistencia, que as migrations Flyway
 * criaram corretamente as tabelas e a constraint UNIQUE(pauta_id,
 * associado_id) em `voto`, independente da validacao feita na camada de
 * servico.
 */
class RepositoryPersistenceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoRepository sessaoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Test
    void devePersistirPautaSessaoEVotoAposMigrations() {
        Pauta pauta = pautaRepository.save(new Pauta("Pauta de teste de persistencia", Instant.now()));
        SessaoVotacao sessao = sessaoRepository.save(
                new SessaoVotacao(pauta, Instant.now(), Instant.now().plusSeconds(60)));
        Voto voto = votoRepository.save(new Voto(pauta, "12345678900", TipoVoto.SIM, Instant.now()));

        assertThat(pautaRepository.findById(pauta.getId())).isPresent();
        assertThat(sessaoRepository.findById(sessao.getId())).isPresent();
        assertThat(votoRepository.findById(voto.getId())).isPresent();
    }

    @Test
    void constraintUnicaDeveImpedirDoisVotosDoMesmoAssociadoNaMesmaPautaNoBanco() {
        Pauta pauta = pautaRepository.save(new Pauta("Pauta com constraint unica", Instant.now()));
        votoRepository.saveAndFlush(new Voto(pauta, "00000000000", TipoVoto.SIM, Instant.now()));

        assertThatThrownBy(() -> votoRepository.saveAndFlush(
                new Voto(pauta, "00000000000", TipoVoto.NAO, Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void constraintUnicaDeveImpedirDuasSessoesParaMesmaPautaNoBanco() {
        Pauta pauta = pautaRepository.save(new Pauta("Pauta com sessao unica no banco", Instant.now()));
        sessaoRepository.saveAndFlush(new SessaoVotacao(pauta, Instant.now(), Instant.now().plusSeconds(60)));

        assertThatThrownBy(() -> sessaoRepository.saveAndFlush(
                new SessaoVotacao(pauta, Instant.now(), Instant.now().plusSeconds(60))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
