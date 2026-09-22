package br.com.cooperativa.votacao.voto.domain;

import br.com.cooperativa.votacao.pauta.domain.Pauta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "voto",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_voto_pauta_associado",
                columnNames = {"pauta_id", "associado_id"}))
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "associado_id", nullable = false, length = 60)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 3)
    private TipoVoto tipo;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected Voto() {
        // construtor exigido pelo JPA
    }

    public Voto(Pauta pauta, String associadoId, TipoVoto tipo, Instant dataHora) {
        this.pauta = pauta;
        this.associadoId = associadoId;
        this.tipo = tipo;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public TipoVoto getTipo() {
        return tipo;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Voto voto)) {
            return false;
        }
        return Objects.equals(id, voto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
