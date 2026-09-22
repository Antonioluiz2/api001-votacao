package br.com.cooperativa.votacao.voto.repository;

import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    long countByPautaIdAndTipo(Long pautaId, TipoVoto tipo);

    boolean existsByPautaIdAndAssociadoId(Long pautaId, String associadoId);
}
