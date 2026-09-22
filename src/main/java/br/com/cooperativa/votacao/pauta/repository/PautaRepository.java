package br.com.cooperativa.votacao.pauta.repository;

import br.com.cooperativa.votacao.pauta.domain.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<Pauta, Long> {
}
