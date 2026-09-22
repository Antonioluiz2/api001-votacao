package br.com.cooperativa.votacao.voto.service;

import br.com.cooperativa.votacao.exception.SessaoEncerradaException;
import br.com.cooperativa.votacao.exception.VotoDuplicadoException;
import br.com.cooperativa.votacao.integration.userinfo.UserInfoService;
import br.com.cooperativa.votacao.pauta.domain.Pauta;
import br.com.cooperativa.votacao.pauta.service.PautaService;
import br.com.cooperativa.votacao.sessao.domain.SessaoVotacao;
import br.com.cooperativa.votacao.sessao.service.SessaoService;
import br.com.cooperativa.votacao.voto.domain.Voto;
import br.com.cooperativa.votacao.voto.dto.RegistrarVotoRequest;
import br.com.cooperativa.votacao.voto.repository.VotoRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotoService {

    private static final Logger log = LoggerFactory.getLogger(VotoService.class);

    private final VotoRepository votoRepository;
    private final PautaService pautaService;
    private final SessaoService sessaoService;
    private final UserInfoService userInfoService;
    private final Clock clock;

    public VotoService(
            VotoRepository votoRepository,
            PautaService pautaService,
            SessaoService sessaoService,
            UserInfoService userInfoService,
            Clock clock) {
        this.votoRepository = votoRepository;
        this.pautaService = pautaService;
        this.sessaoService = sessaoService;
        this.userInfoService = userInfoService;
        this.clock = clock;
    }

    @Transactional
    public Voto registrarVoto(Long pautaId, RegistrarVotoRequest request) {
        Pauta pauta = pautaService.buscarPautaOuFalhar(pautaId);
        SessaoVotacao sessao = sessaoService.buscarSessaoOuFalhar(pautaId);

        Instant agora = Instant.now(clock);
        validarPeriodoDaSessao(pautaId, sessao, agora);

        userInfoService.validarAssociadoPodeVotar(request.associadoId());

        // Verificacao preventiva (nao substitui a constraint unica do banco,
        // mas evita uma escrita desnecessaria no caso comum sem concorrencia).
        if (votoRepository.existsByPautaIdAndAssociadoId(pautaId, request.associadoId())) {
            log.warn("Voto rejeitado, associado ja votou: pautaId={}", pautaId);
            throw new VotoDuplicadoException(request.associadoId(), pautaId);
        }

        Voto voto = new Voto(pauta, request.associadoId(), request.tipo(), agora);
        try {
            Voto salvo = votoRepository.saveAndFlush(voto);
            log.info("Voto registrado: pautaId={}, tipo={}", pautaId, request.tipo());
            return salvo;
        } catch (DataIntegrityViolationException e) {
            // Camada de seguranca contra condicoes de corrida: a constraint
            // UNIQUE(pauta_id, associado_id) garante que apenas um voto seja
            // persistido mesmo com requisicoes concorrentes.
            log.warn("Voto duplicado detectado pela constraint unica: pautaId={}", pautaId);
            throw new VotoDuplicadoException(request.associadoId(), pautaId);
        }
    }

    private void validarPeriodoDaSessao(Long pautaId, SessaoVotacao sessao, Instant agora) {
        if (sessao.aindaNaoIniciada(agora)) {
            log.warn("Voto rejeitado, sessao ainda nao iniciada: pautaId={}", pautaId);
            throw new SessaoEncerradaException("Sessao de votacao ainda nao foi iniciada");
        }
        if (sessao.encerrada(agora)) {
            log.warn("Voto rejeitado, sessao encerrada: pautaId={}", pautaId);
            throw new SessaoEncerradaException("Sessao de votacao ja foi encerrada");
        }
    }
}
