CREATE TABLE pauta (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_pauta_data_criacao ON pauta (data_criacao);
