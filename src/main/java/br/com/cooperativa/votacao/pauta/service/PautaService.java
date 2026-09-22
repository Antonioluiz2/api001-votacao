package br.com.cooperativa.votacao.pauta.service;

import br.com.cooperativa.votacao.exception.PautaNotFoundException;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.dto.CriarPautaRequest;
import br.com.cooperativa.votacao.pauta.repository.PautaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PautaService {

    private static final Logger log = LoggerFactory.getLogger(PautaService.class);

    private final PautaRepository pautaRepository;
    private final Clock clock;

    public PautaService(PautaRepository pautaRepository, Clock clock) {
        this.pautaRepository = pautaRepository;
        this.clock = clock;
    }

    @Transactional
    public Pauta criarPauta(CriarPautaRequest request) {
        Pauta pauta = new Pauta(request.descricao().trim(), Instant.now(clock));
        Pauta salva = pautaRepository.save(pauta);
        log.info("Pauta criada: id={}", salva.getId());
        return salva;
    }

    @Transactional(readOnly = true)
    public Pauta buscarPautaOuFalhar(Long pautaId) {
        return pautaRepository.findById(pautaId).orElseThrow(() -> new PautaNotFoundException(pautaId));
    }

    @Transactional(readOnly = true)
    public List<Pauta> listarPautas() {
        return pautaRepository.findAll();
    }
}
