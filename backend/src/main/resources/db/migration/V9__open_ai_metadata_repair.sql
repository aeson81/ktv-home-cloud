-- OpenAI-compatible configuration, metadata repair provenance and safe source cleanup.

CREATE TABLE app_secrets (
    key         TEXT PRIMARY KEY,
    ciphertext  BYTEA NOT NULL,
    nonce       BYTEA NOT NULL,
    key_version INTEGER NOT NULL DEFAULT 1,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE songs
    ALTER COLUMN language SET DEFAULT '未知',
    ADD COLUMN IF NOT EXISTS vocal_form VARCHAR(32) NOT NULL DEFAULT '未知',
    ADD COLUMN IF NOT EXISTS metadata_locks TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS metadata_provenance JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS needs_ai_optimization BOOLEAN NOT NULL DEFAULT false;

-- The old default did not represent evidence. Existing rows that were never AI analyzed
-- are retained for review and must not be sent to a model as a trusted fact.
UPDATE songs
SET metadata_provenance = metadata_provenance || '{"language":{"source":"legacy_default","trusted":false}}'::jsonb,
    needs_ai_optimization = true
WHERE language = '国语' AND ai_analyzed_at IS NULL;

ALTER TABLE ai_analysis_tasks
    ALTER COLUMN song_id DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS target_type VARCHAR(32) NOT NULL DEFAULT 'SONG',
    ADD COLUMN IF NOT EXISTS target_id BIGINT,
    ADD COLUMN IF NOT EXISTS batch_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS model_role VARCHAR(32) NOT NULL DEFAULT 'BULK',
    ADD COLUMN IF NOT EXISTS field_confidence JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS evidence JSONB NOT NULL DEFAULT '{}'::jsonb;
UPDATE ai_analysis_tasks SET target_id = song_id WHERE target_id IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_tasks_batch ON ai_analysis_tasks(batch_id, status, created_at);

ALTER TABLE media_import_records
    ADD COLUMN IF NOT EXISTS delete_source_requested BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS cleanup_status VARCHAR(32) NOT NULL DEFAULT 'NOT_REQUESTED',
    ADD COLUMN IF NOT EXISTS cleanup_error TEXT,
    ADD COLUMN IF NOT EXISTS cleanup_attempted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS companion_files JSONB NOT NULL DEFAULT '[]'::jsonb;
