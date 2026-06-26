-- pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 文档分块表（向量存储在 PostgreSQL）
CREATE TABLE IF NOT EXISTS ai_document_chunk (
    id BIGINT NOT NULL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    kb_id BIGINT DEFAULT NULL,
    content TEXT NOT NULL,
    embedding TEXT DEFAULT NULL,
    chunk_index INT DEFAULT 0,
    metadata TEXT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chunk_document_id ON ai_document_chunk (document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_kb_id ON ai_document_chunk (kb_id);

COMMENT ON TABLE ai_document_chunk IS '文档分块表';
COMMENT ON COLUMN ai_document_chunk.id IS '主键ID';
COMMENT ON COLUMN ai_document_chunk.document_id IS '文档ID';
COMMENT ON COLUMN ai_document_chunk.kb_id IS '知识库ID';
COMMENT ON COLUMN ai_document_chunk.content IS '分块内容';
COMMENT ON COLUMN ai_document_chunk.embedding IS '向量数据(JSON数组)';
COMMENT ON COLUMN ai_document_chunk.chunk_index IS '分块序号';
COMMENT ON COLUMN ai_document_chunk.metadata IS '元数据';
COMMENT ON COLUMN ai_document_chunk.create_time IS '创建时间';
