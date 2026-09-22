package br.com.cooperativa.votacao.sessao.service;

import br.com.cooperativa.votacao.exception.RequisicaoInvalidaException;
import br.com.cooperativa.votacao.exception.SessaoAlreadyExistsException;
import br.com.cooperativa.votacao.exception.SessaoNotFoundException;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import br.com.cooperativa.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.cooperativa.votacao.sessao.repository.SessaoRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessaoService {

    private static final int DURACAO_PADRAO_SEGUNDOS = 60;

    private static final Logger log = LoggerFactory.getLogger(SessaoService.class);

    private final SessaoRepository sessaoRepository;
    private final PautaService pautaService;
    private final Clock clock;

    public SessaoService(SessaoRepository sessaoRepository, PautaService pautaService, Clock clock) {
        this.sessaoRepository = sessaoRepository;
        this.pautaService = pautaService;
        this.clock = clock;
    }

    @Transactional
    public SessaoVotacao abrirSessao(Long pautaId, AbrirSessaoRequest request) {
        Pauta pauta = pautaService.buscarPautaOuFalhar(pautaId);

        if (sessaoRepository.existsByPautaId(pautaId)) {
            throw new SessaoAlreadyExistsException(pautaId);
        }

        int duracaoEmSegundos = request.duracaoEmSegundos() != null
                ? request.duracaoEmSegundos()
                : DURACAO_PADRAO_SEGUNDOS;

        if (duracaoEmSegundos <= 0) {
            throw new RequisicaoInvalidaException("duracaoEmSegundos deve ser maior que zero");
        }

        Instant inicio = Instant.now(clock);
        Instant fim = inicio.plusSeconds(duracaoEmSegundos);

        SessaoVotacao sessao = new SessaoVotacao(pauta, inicio, fim);
        SessaoVotacao salva = sessaoRepository.save(sessao);
        log.info("Sessao aberta: pautaId={}, duracaoEmSegundos={}", pautaId, duracaoEmSegundos);
        return salva;
    }

    @Transactional(readOnly = true)
    public SessaoVotacao buscarSessaoOuFalhar(Long pautaId) {
        return sessaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new SessaoNotFoundException(pautaId));
    }

    @Transactional(readOnly = true)
    public boolean existeSessao(Long pautaId) {
        return sessaoRepository.existsByPautaId(pautaId);
    }
}
