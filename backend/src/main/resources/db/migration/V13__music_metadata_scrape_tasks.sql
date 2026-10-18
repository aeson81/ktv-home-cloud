-- Persistent KTV-library metadata scraping batches and review items.

CREATE TABLE music_metadata_scrape_batches (
    id                   VARCHAR(36) PRIMARY KEY,
    mode                 VARCHAR(16) NOT NULL,
    status               VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    auto_apply_threshold DOUBLE PRECISION NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at           TIMESTAMPTZ,
    finished_at          TIMESTAMPTZ,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_metadata_scrape_batches_created ON music_metadata_scrape_batches(created_at DESC);

CREATE TABLE music_metadata_scrape_items (
    id             BIGSERIAL PRIMARY KEY,
    batch_id       VARCHAR(36) NOT NULL REFERENCES music_metadata_scrape_batches(id) ON DELETE CASCADE,
    song_id        BIGINT REFERENCES songs(id) ON DELETE SET NULL,
    original_title VARCHAR(500) NOT NULL,
    original_artist VARCHAR(500) NOT NULL,
    status         VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    provider       VARCHAR(24),
    external_id    VARCHAR(160),
    match_score    DOUBLE PRECISION,
    result_json    JSONB,
    error_message  VARCHAR(1000),
    started_at     TIMESTAMPTZ,
    finished_at    TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_metadata_scrape_batch_song UNIQUE (batch_id, song_id)
);
CREATE INDEX idx_metadata_scrape_items_batch_status ON music_metadata_scrape_items(batch_id,status,id);
