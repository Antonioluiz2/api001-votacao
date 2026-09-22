CREATE TABLE voto (
    id BIGSERIAL PRIMARY KEY,
    pauta_id BIGINT NOT NULL,
    associado_id VARCHAR(60) NOT NULL,
    tipo VARCHAR(3) NOT NULL,
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_voto_pauta FOREIGN KEY (pauta_id) REFERENCES pauta (id),
    CONSTRAINT uq_voto_pauta_associado UNIQUE (pauta_id, associado_id),
    CONSTRAINT ck_voto_tipo CHECK (tipo IN ('SIM', 'NAO'))
);

CREATE INDEX idx_voto_pauta_id ON voto (pauta_id);
CREATE INDEX idx_voto_associado_id ON voto (associado_id);
