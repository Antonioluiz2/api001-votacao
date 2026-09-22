package br.com.cooperativa.votacao.sessao.domain;

import br.com.cooperativa.votacao.pauta.domain.Pauta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sessao_votacao")
public class SessaoVotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "inicio", nullable = false)
    private Instant inicio;

    @Column(name = "fim", nullable = false)
    private Instant fim;

    protected SessaoVotacao() {
        // construtor exigido pelo JPA
    }

    public SessaoVotacao(Pauta pauta, Instant inicio, Instant fim) {
        this.pauta = pauta;
        this.inicio = inicio;
        this.fim = fim;
    }

    /**
     * Sessao esta aberta quando: agora &gt;= inicio &amp;&amp; agora &lt; fim.
     */
    public boolean estaAberta(Instant agora) {
        return !agora.isBefore(inicio) && agora.isBefore(fim);
    }

    public boolean encerrada(Instant agora) {
        return !agora.isBefore(fim);
    }

    public boolean aindaNaoIniciada(Instant agora) {
        return agora.isBefore(inicio);
    }

    public Long getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public Instant getInicio() {
        return inicio;
    }

    public Instant getFim() {
        return fim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SessaoVotacao that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
