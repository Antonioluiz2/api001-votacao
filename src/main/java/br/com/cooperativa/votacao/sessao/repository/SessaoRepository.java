package br.com.cooperativa.votacao.sessao.repository;

import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<SessaoVotacao, Long> {

    Optional<SessaoVotacao> findByPautaId(Long pautaId);

    boolean existsByPautaId(Long pautaId);
}
