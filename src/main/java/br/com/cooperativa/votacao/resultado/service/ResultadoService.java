package br.com.cooperativa.votacao.resultado.service;

import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.resultado.dto.ResultadoResponse;
import br.com.cooperativa.votacao.voto.domain.TipoVoto;
import br.com.cooperativa.votacao.voto.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultadoService {

    private static final Logger log = LoggerFactory.getLogger(ResultadoService.class);

    private final VotoRepository votoRepository;
    private final PautaService pautaService;

    public ResultadoService(VotoRepository votoRepository, PautaService pautaService) {
        this.votoRepository = votoRepository;
        this.pautaService = pautaService;
    }

    @Transactional(readOnly = true)
    public ResultadoResponse obterResultado(Long pautaId) {
        pautaService.buscarPautaOuFalhar(pautaId);

        long sim = votoRepository.countByPautaIdAndTipo(pautaId, TipoVoto.SIM);
        long nao = votoRepository.countByPautaIdAndTipo(pautaId, TipoVoto.NAO);

        log.info("Resultado consultado: pautaId={}, sim={}, nao={}", pautaId, sim, nao);
        return new ResultadoResponse(pautaId, sim, nao, sim + nao);
    }
}
