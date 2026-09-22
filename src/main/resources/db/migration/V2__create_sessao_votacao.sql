CREATE TABLE sessao_votacao (
    id BIGSERIAL PRIMARY KEY,
    pauta_id BIGINT NOT NULL,
    inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    fim TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_sessao_pauta FOREIGN KEY (pauta_id) REFERENCES pauta (id),
    CONSTRAINT uq_sessao_pauta UNIQUE (pauta_id)
);

CREATE INDEX idx_sessao_pauta_id ON sessao_votacao (pauta_id);
