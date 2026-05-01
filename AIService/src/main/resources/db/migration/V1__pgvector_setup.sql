CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS vector_store (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    content     TEXT         NOT NULL,
    metadata    JSONB        DEFAULT '{}',
    embedding   vector(384)
    );

CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
    ON vector_store
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

CREATE INDEX IF NOT EXISTS idx_vector_store_metadata
    ON vector_store
    USING GIN (metadata);